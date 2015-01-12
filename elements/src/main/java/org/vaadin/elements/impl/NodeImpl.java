package org.vaadin.elements.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import org.vaadin.elements.Node;

public abstract class NodeImpl implements Node {

    protected final org.jsoup.nodes.Node node;
    protected Context context;

    protected NodeImpl(org.jsoup.nodes.Node node) {
        this.node = node;
        this.context = new Context();
        context.adopt(this);
    }

    @Override
    public Node getParent() {
        return context.resolve(node.parent());
    }

    @Override
    public List<NodeImpl> getChildren() {
        return new AbstractList<NodeImpl>() {
            @Override
            public NodeImpl get(int index) {
                return context.resolve(node.childNode(index));
            }

            @Override
            public int size() {
                return node.childNodeSize();
            }
        };
    }

    @Override
    public void removeAllChildren() {
        // TODO send all as one job to the client instead of individually
        new ArrayList<>(getChildren()).forEach(Node::remove);
    }

    @Override
    public void remove() {
        NodeImpl parent = (NodeImpl) getParent();
        if (parent == null) {
            return;
        }

        // For this node and all descendants: find children using old context,
        // then move to new context
        Context newContext = new Context();
        Context oldContext = context;

        ArrayList<NodeImpl> stack = new ArrayList<>();
        stack.add(this);

        while (!stack.isEmpty()) {
            NodeImpl node = stack.remove(stack.size() - 1);
            stack.addAll(node.getChildren());

            newContext.adopt(node);
        }

        // Remove all nodes from the old context
        // oldContext.removeAll(newContext);

        node.remove();
    }

    @Override
    public RootImpl getRoot() {
        return context.getRoot();
    }

    protected Context getContext() {
        return context;
    }

    @Override
    public String asHtml() {
        return node.outerHtml();
    }
}