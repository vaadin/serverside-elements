package org.vaadin.elements.impl;

import org.vaadin.elements.TextNode;

public class TextNodeImpl extends NodeImpl implements TextNode {

    TextNodeImpl(org.jsoup.nodes.TextNode soupNode) {
        super(soupNode);
    }

    @Override
    public String getText() {
        return ((org.jsoup.nodes.TextNode) node).text();
    }

}
