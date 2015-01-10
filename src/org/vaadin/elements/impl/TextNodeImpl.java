package org.vaadin.elements.impl;

import org.vaadin.elements.TextNode;

public class TextNodeImpl extends NodeImpl implements TextNode {

    TextNodeImpl(org.jsoup.nodes.TextNode soupNode) {
        super(soupNode);
    }

    @Override
    public String getText() {
        return getNode().text();
    }

    private org.jsoup.nodes.TextNode getNode() {
        return (org.jsoup.nodes.TextNode) node;
    }

    @Override
    public void setText(String text) {
        assert text != null;
        getNode().text(text);

        RootImpl document = getRoot();
        if (document != null) {
            document.setTextChange(this, text);
        }

    }

}
