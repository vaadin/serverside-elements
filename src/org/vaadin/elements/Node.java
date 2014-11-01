package org.vaadin.elements;

import java.util.List;

public interface Node {

    public Node getParent();

    public List<? extends Node> getChildren();

    public void removeAllChildren();

    public void remove();

    public Root getRoot();

    public String asHtml();
}