package org.vaadin.elements;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ElementsTest {
    private Element root;
    private Element child;

    @Before
    public void init() {
        root = Elements.create("div");
        child = Elements.create("div");
    }

    @Test
    public void testAddNode() {
        root.appendChild(child);

        List<? extends Node> children = root.getChildren();

        assertSame(root, child.getParent());
        assertEquals(1, children.size());
        assertSame(child, children.get(0));
    }

    @Test
    public void testRemoveNode() {
        root.appendChild(child);

        child.remove();

        List<? extends Node> children = root.getChildren();

        assertEquals(0, children.size());
        assertNull(child.getParent());
    }
}
