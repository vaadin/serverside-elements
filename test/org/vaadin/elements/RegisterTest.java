package org.vaadin.elements;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RegisterTest {
    @Tag("exclusive")
    public static interface ExclusiveElement extends Element {

    }

    @Tag(value = "non-exclusive", exclusive = false)
    public static interface NonExclusiveElement extends Element {

    }

    @Before
    public void init() {
        Elements.clearRegistration();
    }

    @Test
    public void testRegisterExlusive() {
        Elements.registerElement(ExclusiveElement.class);
        Element element = Elements.create("exclusive");
        Assert.assertTrue(element instanceof ExclusiveElement);
    }

    @Test
    public void testWithoutRegister() {
        Element element = Elements.create("exclusive");
        Assert.assertFalse(element instanceof ExclusiveElement);
    }

    @Test
    public void testImplicitRegister() {
        Element element = Elements.create(ExclusiveElement.class);
        Assert.assertTrue(element instanceof ExclusiveElement);
    }

    @Test
    public void testNonExclusiveCreation() {
        Element element = Elements.create(NonExclusiveElement.class);
        Assert.assertTrue(element instanceof NonExclusiveElement);
    }

    @Test
    public void testNonExclusive_notRegistered() {
        Element element = Elements.create(NonExclusiveElement.class);

        element = Elements.create(element.getTag());

        Assert.assertFalse(element instanceof NonExclusiveElement);
    }
}
