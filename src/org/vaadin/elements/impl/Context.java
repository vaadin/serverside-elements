package org.vaadin.elements.impl;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Node;

public class Context {
    private Map<org.jsoup.nodes.Node, NodeImpl> fromJsoup = new IdentityHashMap<>();

    public Context() {
    }

    public NodeImpl resolve(org.jsoup.nodes.Node soupNode) {
        if (soupNode == null) {
            return null;
        }

        NodeImpl node = fromJsoup.get(soupNode);

        if (node == null) {
            throw new IllegalStateException();
        }

        return node;
    }

    public void wrapChildren(NodeImpl node) {
        assert node.context == this;

        List<Node> childNodes = new ArrayList<>(node.node.childNodes());

        while (!childNodes.isEmpty()) {
            Node soupChild = childNodes.remove(childNodes.size() - 1);

            childNodes.addAll(soupChild.childNodes());

            NodeImpl child = ElementReflectHelper.wrap(soupChild);
            adopt(child);
        }
    }

    protected void adopt(NodeImpl node) {
        if (node.context != null) {
            node.context.remove(node);
        }

        fromJsoup.put(node.node, node);
        node.context = this;
    }

    public RootImpl getRoot() {
        return null;
    }

    public void adoptAll(NodeImpl child) {
        List<NodeImpl> stack = new ArrayList<>();
        stack.add(child);

        while (!stack.isEmpty()) {
            NodeImpl node = stack.remove(stack.size() - 1);
            // Find child nodes with old context
            stack.addAll(node.getChildren());

            adopt(node);
        }
    }

    protected void remove(NodeImpl node) {
        fromJsoup.remove(node.node);
    }
}
