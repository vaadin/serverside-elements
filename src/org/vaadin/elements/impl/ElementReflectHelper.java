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

public class ElementReflectHelper {

    public static NodeImpl wrap(org.jsoup.nodes.Node soupNode) {
        if (soupNode instanceof org.jsoup.nodes.TextNode) {
            return new TextNodeImpl((org.jsoup.nodes.TextNode) soupNode);
            // } else if (soupNode instanceof org.jsoup.nodes.DataNode) {
            // return Elements.createText(((org.jsoup.nodes.DataNode) soupNode)
            // .getWholeData());
        } else if (soupNode instanceof org.jsoup.nodes.Element) {
            org.jsoup.nodes.Element soupElement = (org.jsoup.nodes.Element) soupNode;
            String tag = soupElement.tagName();

            Class<? extends Element> elementType = Elements
                    .getRegisteredClass(tag);
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

                if ((name.startsWith("get") || name.startsWith("is"))
                        && method.getParameterCount() == 0) {
                    name = ElementReflectHelper.getPropertyName(name);

                    if (method.getReturnType() == String.class) {
                        return element.getAttribute(name);
                    } else if (method.getReturnType() == boolean.class) {
                        return element.hasAttribute(name);
                    }
                } else if (name.startsWith("set")
                        && method.getParameterCount() == 1) {
                    name = ElementReflectHelper.getPropertyName(name);
                    Class<?> type = method.getParameterTypes()[0];

                    if (type == String.class) {
                        element.setAttribute(name, (String) args[0]);
                        return null;
                    } else if (type == boolean.class) {
                        element.setAttribute(name,
                                ((Boolean) args[0]).booleanValue());
                        return null;
                    }
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

        Object instance = enhancer.create(
                new Class[] { org.jsoup.nodes.Element.class },
                new Object[] { soupElement });

        return type.cast(instance);
    }

    public static String getPropertyName(String name) {
        name = name.replaceAll("^(get|set|is)", "");
        // TODO locale for lower case
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return name;
    }

}
