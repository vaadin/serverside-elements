package org.vaadin.elements;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.elements.impl.RootImpl;

import com.vaadin.ui.Label;

import elemental.json.JsonArray;

public class RootTest {

    private RootImpl root;
    private Element child;

    @Before
    public void init() {
        child = Elements.create("div");
        root = (RootImpl) ElementIntegration.getRoot(new Label());
    }

    @Test
    public void testAppendChild() {
        root.appendChild(child);

        JsonArray pendingCommands = root.flushPendingCommands();

        Assert.assertEquals(
                "[[\"createElement\",2,\"div\"],[\"appendChild\",0,2]]",
                pendingCommands.toJson());
    }

    @Test
    public void testAppendChildOrder() {
        child.appendChild(Elements.create("first"));
        child.appendChild(Elements.create("second"));

        root.appendChild(child);

        JsonArray pendingCommands = root.flushPendingCommands();

        Assert.assertEquals(
                "[[\"createElement\",2,\"div\"],[\"appendChild\",0,2],"
                        + "[\"createElement\",4,\"first\"],[\"appendChild\",2,4],"
                        + "[\"createElement\",6,\"second\"],[\"appendChild\",2,6]]",
                pendingCommands.toJson());

    }

    @Test
    public void testRemoveChild() {
        root.appendChild(child);
        root.flushPendingCommands();

        child.remove();

        JsonArray pendingCommands = root.flushPendingCommands();

        Assert.assertEquals("[[\"remove\",2]]", pendingCommands.toJson());
    }

}
