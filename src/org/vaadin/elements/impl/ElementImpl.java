package org.vaadin.elements.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.vaadin.elements.Element;
import org.vaadin.elements.Elements;
import org.vaadin.elements.EventParam;
import org.vaadin.elements.Node;

import com.google.gwt.thirdparty.guava.common.base.Objects;
import com.vaadin.server.JsonCodec;
import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.ui.JavaScriptFunction;

import elemental.json.JsonArray;

public class ElementImpl extends NodeImpl implements Element {
    public ElementImpl(org.jsoup.nodes.Element node) {
        super(node);
    }

    @Override
    public void appendChild(Node child) {
        NodeImpl nodeImpl = (NodeImpl) child;
        appendSoupNode(nodeImpl);

        context.adoptAll(nodeImpl);
    }

    void appendSoupNode(NodeImpl child) {
        getElement().appendChild(child.node);
    }

    private org.jsoup.nodes.Element getElement() {
        return (org.jsoup.nodes.Element) node;
    }

    @Override
    public String getTag() {
        return getElement().tagName();
    }

    // TODO create a wrapper class instead of using two lists
    private List<String> evalQueue = new ArrayList<>();
    private List<Object[]> evalParamQueue = new ArrayList<>();

    private Map<Integer, JavaScriptFunction> callbacks = new HashMap<>();

    @Override
    public void setAttribute(String name, String value) {
        org.jsoup.nodes.Element element = getElement();
        if (Objects.equal(value, getAttribute(name))) {
            return;
        }

        if (value == null) {
            element.removeAttr(name);
        } else {
            element.attr(name, value);
        }

        RootImpl document = getRoot();
        if (document != null) {
            document.setAttributeChange(this, name);
        }
    }

    @Override
    public Collection<String> getAttributeNames() {
        List<String> list = new ArrayList<>();
        getElement().attributes().forEach(a -> list.add(a.getKey()));
        return list;
    }

    @Override
    public String getAttribute(String name) {
        if (!getElement().hasAttr(name)) {
            return null;
        }
        return getElement().attr(name);
    }

    @Override
    public void setInnerText(String text) {
        removeAllChildren();

        appendChild(Elements.createText(text));
    }

    @Override
    public void setAttribute(String name, boolean value) {
        if (value) {
            setAttribute(name, "");
        } else {
            removeAttribute(name);
        }
    }

    @Override
    public boolean hasAttribute(String name) {
        return getElement().hasAttr(name);
    }

    @Override
    public void removeAttribute(String name) {
        setAttribute(name, null);
    }

    @Override
    public void eval(String script, Object... arguments) {
        RootImpl document = getRoot();
        if (document != null) {
            document.eval(this, script, arguments);
        } else {
            evalQueue.add(script);
            evalParamQueue.add(arguments);
        }
    }

    void flushEvals() {
        if (!evalQueue.isEmpty()) {
            RootImpl document = getRoot();
            assert document != null;

            if (document != null) {
                for (int i = 0; i < evalQueue.size(); i++) {
                    eval(evalQueue.get(i), evalParamQueue.get(i));
                }
                evalQueue.clear();
                evalParamQueue.clear();
            }
        }
    }

    void setCallback(int cid, JavaScriptFunction callback) {
        callbacks.put(Integer.valueOf(cid), callback);
    }

    JavaScriptFunction getCallback(int cid) {
        return callbacks.get(Integer.valueOf(cid));
    }

    @Override
    public void bindAttribute(String attributeName, String eventName) {
        addEventListener(eventName, arguments -> {
            getElement().attr(attributeName, arguments.getString(0));
        }, "''+e." + attributeName);
    }

    @Override
    public void addEventListener(String eventName, JavaScriptFunction listener,
            String... arguments) {
        String argumentBuilder = String.join(",", arguments);
        eval("e.addEventListener('" + eventName
                + "', function (event) { param[0](" + argumentBuilder + ") })",
                listener);
    }

    @Override
    public void addEventListener(EventListener listener) {
        List<Method> listenerMethods = findInterfaceMethods(listener.getClass());

        for (Method method : listenerMethods) {
            if (method.getDeclaringClass() == Object.class) {
                // Ignore
                continue;
            }

            String name = method.getName();
            if (!name.startsWith("on")) {
                throw new RuntimeException(method.toString());
            }

            name = name.substring(2).toLowerCase();

            if (method.getParameterCount() != 1) {
                throw new RuntimeException();
            }

            if (method.getReturnType() != void.class) {
                throw new RuntimeException();
            }

            Map<String, Integer> methodOrder = new HashMap<>();
            Class<?> eventType = method.getParameterTypes()[0];

            Method[] eventGetters = eventType.getDeclaredMethods();
            String[] argumentBuilders = new String[eventGetters.length];

            for (int i = 0; i < eventGetters.length; i++) {
                Method getter = eventGetters[i];
                if (getter.getParameterCount() != 0) {
                    throw new RuntimeException(getter.toString());
                }

                String paramName = ElementReflectHelper.getPropertyName(getter
                        .getName());

                methodOrder.put(getter.getName(), Integer.valueOf(i));
                argumentBuilders[i] = "event." + paramName;
            }

            addEventListener(name, new JavaScriptFunction() {
                @Override
                public void call(final JsonArray arguments) {
                    InvocationHandler invocationHandler = (proxy, calledMethod,
                            args) -> {
                        if (calledMethod.getDeclaringClass() == Object.class) {
                            // Standard object methods
                            return calledMethod.invoke(proxy, args);
                        } else {
                            String methodName = calledMethod.getName();
                            int indexOf = methodOrder.get(methodName)
                                    .intValue();
                            return JsonCodec.decodeInternalOrCustomType(
                                    calledMethod.getGenericReturnType(),
                                    arguments.get(indexOf), null);
                        }
                    };

                    Object event = Proxy.newProxyInstance(
                            eventType.getClassLoader(),
                            new Class[] { eventType }, invocationHandler);

                    try {
                        method.invoke(listener, event);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, argumentBuilders);
        }
    }

    private List<Method> findInterfaceMethods(Class<?> type) {
        return Arrays.asList(type.getInterfaces()).stream()
                .flatMap(iface -> Arrays.asList(iface.getMethods()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void addEventListener(ServerRpc rpc) {
        List<Method> interfaceMethods = findInterfaceMethods(rpc.getClass());

        for (Method method : interfaceMethods) {
            String eventName = method.getName().toLowerCase();

            String[] arguments = new String[method.getParameterCount()];

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                EventParam eventParam = parameters[i]
                        .getAnnotation(EventParam.class);
                arguments[i] = "event." + eventParam.value();
            }

            addEventListener(eventName, new JavaScriptFunction() {
                @Override
                public void call(JsonArray arguments) {
                    Object[] args = new Object[parameters.length];
                    for (int i = 0; i < args.length; i++) {
                        // TODO handle null for primitive return types
                        args[i] = JsonCodec.decodeInternalOrCustomType(
                                parameters[i].getParameterizedType(),
                                arguments.get(i), null);
                    }

                    try {
                        method.invoke(rpc, args);
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, arguments);
        }
    }

    @Override
    public void setInnerHtml(String html) {
        removeAllChildren();
        getElement().html(html);

        context.wrapChildren(this);
    }

    @Override
    public Optional<Element> querySelector(String query) {
        org.jsoup.nodes.Element element = getElement().select(query).first();
        if (element == null) {
            return Optional.empty();
        } else {
            return Optional.of((Element) context.resolve(element));
        }
    }

    @Override
    public List<Element> querySelectorAll(String query) {
        org.jsoup.select.Elements elements = getElement().select(query);

        return new AbstractList<Element>() {
            @Override
            public Element get(int index) {
                return (Element) context.resolve(elements.get(index));
            }

            @Override
            public int size() {
                return elements.size();
            }
        };
    }

    void resetChildren(ArrayList<NodeImpl> newChildren) {
        HashSet<NodeImpl> oldChildren = new HashSet<>(getChildren());
        oldChildren.removeAll(newChildren);
        oldChildren.forEach(child -> child.node.remove());

        getElement().children().remove();

        for (NodeImpl child : newChildren) {
            appendSoupNode(child);
        }
    }

    @Override
    public void setDisabled(boolean disabled) {
        setAttribute("disabled", disabled);
    }

    @Override
    public boolean isDisabled() {
        return hasAttribute("disabled");
    }
}
