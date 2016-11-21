package org.vaadin.elements;

import org.junit.Test;

import junit.framework.Assert;

public class ElementInterfaceTest {
    @Tag("form")
    public interface MyElement extends Element {
        void setAcceptCharset(String acceptCharset);

        String getAcceptCharset();
    }

    @Test
    public void testFormCharset() {
        MyElement element = Elements.create(MyElement.class);

        Assert.assertEquals("form", element.getTag());

        element.setAcceptCharset("fooBar");
        Assert.assertEquals("fooBar", element.getAttribute("accept-charset"));
        Assert.assertEquals("fooBar", element.getAcceptCharset());
    }

}
