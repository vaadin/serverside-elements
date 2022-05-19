package org.vaadin.elements;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

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

    @Test
    public void testArrayAttribute() {
        Element element = Elements.create("div");

        String[] items = {"A", "B", "C"};
        JsonArray jsonArray = Json.createArray();

        for (int i = 0; i < items.length; i++) {
            JsonObject object = Json.createObject();
            object.put("id", i);
            object.put("name", items[i]);

            jsonArray.set(i, object);
        }

        String jsonArrayString = jsonArray.toString();
        element.setAttribute("items", jsonArrayString);

        Assert.assertEquals(jsonArrayString, element.getAttribute("items"));
    }
}
