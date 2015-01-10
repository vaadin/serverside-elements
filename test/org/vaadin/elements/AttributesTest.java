package org.vaadin.elements;

import org.junit.Assert;
import org.junit.Test;

public class AttributesTest {
    @Test
    public void testSetAttribute() {
        Element element = Elements.create("div");
        element.setAttribute("name", "value");
        element.setAttribute("blank", "");

        Assert.assertEquals("value", element.getAttribute("name"));
        Assert.assertEquals("", element.getAttribute("blank"));
        Assert.assertNull(element.getAttribute("notThere"));
    }

    @Test
    public void testBooleanAttributes() {
        Element element = Elements.create("div");
        element.setAttribute("true", true);
        element.setAttribute("false", false);

        Assert.assertEquals("", element.getAttribute("true"));
        Assert.assertNull(element.getAttribute("false"));
    }
}
