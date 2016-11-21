package org.vaadin.elements.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;

import org.vaadin.elements.Element;
import org.vaadin.elements.Elements;
import org.vaadin.elements.UpdatedBy;

public class ElementReflectHelper {

    public static NodeImpl wrap(org.jsoup.nodes.Node soupNode) {
        return wrap(soupNode, null);
    }

    public static NodeImpl wrap(org.jsoup.nodes.Node soupNode,
            Class<? extends Element> elementType) {
        if (soupNode instanceof org.jsoup.nodes.TextNode) {
            return new TextNodeImpl((org.jsoup.nodes.TextNode) soupNode);
            // } else if (soupNode instanceof org.jsoup.nodes.DataNode) {
            // return Elements.createText(((org.jsoup.nodes.DataNode) soupNode)
            // .getWholeData());
        } else if (soupNode instanceof org.jsoup.nodes.Element) {
            org.jsoup.nodes.Element soupElement = (org.jsoup.nodes.Element) soupNode;
            String tag = soupElement.tagName();

            if (elementType == null) {
                elementType = Elements.getRegisteredClass(tag);
            }
            if (elementType == null) {
                return new ElementImpl(soupElement);
            } else {
                return (NodeImpl) wrapElement(elementType, soupElement);
            }
        } else {
            throw new RuntimeException(soupNode.getClass().getName());
        }
    }

    private static <T extends Element> T wrapElement(Class<T> type,
            org.jsoup.nodes.Element soupElement) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ElementImpl.class);
        enhancer.setInterfaces(new Class[] { type });
        enhancer.setClassLoader(Elements.class.getClassLoader());

        MethodInterceptor methodInterceptor = new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args,
                    MethodProxy proxy) throws Throwable {

                ElementImpl element = (ElementImpl) obj;
                String name = method.getName();

                if (isGetter(method)) {
                    name = ElementReflectHelper.getAttributeName(name);

                    Class<?> type = method.getReturnType();
                    if (type == String.class) {
                        return element.getAttribute(name);
                    } else if (type == double.class) {
                        String value = element.getAttribute(name);
                        if (value == null || value.isEmpty()) {
                            return Double.valueOf(0);
                        } else {
                            return Double.valueOf(value);
                        }
                    } else if (type == boolean.class) {
                        return element.hasAttribute(name);
                    }
                } else if (name.startsWith("set")
                        && method.getParameterCount() == 1) {
                    name = ElementReflectHelper.getAttributeName(name);
                    Class<?> type = method.getParameterTypes()[0];

                    if (type == String.class) {
                        element.setAttribute(name, (String) args[0]);
                    } else if (type == double.class) {
                        element.setAttribute(name, args[0].toString());
                    } else if (type == boolean.class) {
                        element.setAttribute(name,
                                ((Boolean) args[0]).booleanValue());
                    } else {
                        throw new RuntimeException(
                                "Property type not supported for " + method);
                    }
                    return null;
                }

                throw new RuntimeException("Unsupported method: " + method);
            }
        };

        CallbackHelper callbackHelper = new CallbackHelper(ElementImpl.class,
                new Class[] { type }) {
            @Override
            protected Object getCallback(Method method) {
                if (method.isDefault()
                        || !Modifier.isAbstract(method.getModifiers())) {
                    return NoOp.INSTANCE;
                } else {
                    return methodInterceptor;
                }
            }
        };
        enhancer.setCallbackFilter(callbackHelper);
        enhancer.setCallbacks(callbackHelper.getCallbacks());

        T instance = type.cast(enhancer.create(
                new Class[] { org.jsoup.nodes.Element.class },
                new Object[] { soupElement }));

        for (Method method : type.getMethods()) {
            if (isGetter(method)) {
                for (UpdatedBy updatedBy : method
                        .getAnnotationsByType(UpdatedBy.class)) {
                    // Binding actually happens on the property level in the DOM
                    instance.bindAttribute(getPropertyName(method.getName()),
                            updatedBy.value());
                }
            }
        }

        return instance;
    }

    private static boolean isGetter(Method method) {
        String name = method.getName();
        return (name.startsWith("get") || name.startsWith("is"))
                && method.getParameterCount() == 0;
    }

    public static String getPropertyName(String name) {
        name = name.replaceAll("^(get|set|is)", "");
        // TODO locale for lower case
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return name;
    }

    private static String getAttributeName(String methodName) {
        String propertyName = getPropertyName(methodName);

        // Convert camelCase to dash-case
        return propertyName.replaceAll("([A-Z])", "-$1").toLowerCase();
    }

}
