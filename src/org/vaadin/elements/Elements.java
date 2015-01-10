package org.vaadin.elements;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.vaadin.elements.impl.ElementReflectHelper;

public class Elements {

    private static final Map<String, Class<? extends Element>> registeredElements = new ConcurrentHashMap<>();

    public static <T extends Element> void registerElement(Class<T> type) {
        registeredElements.put(getElementTag(type), type);
    }

    public static Class<? extends Element> getRegisteredClass(String name) {
        return registeredElements.get(name);
    }

    public static Element create(String tag) {
        org.jsoup.nodes.Element soupElement = createSoupElement(tag);
        return (Element) ElementReflectHelper.wrap(soupElement);
    }

    private static org.jsoup.nodes.Element createSoupElement(String tag) {
        return new org.jsoup.nodes.Element(org.jsoup.parser.Tag.valueOf(tag),
                "");
    }

    public static <T extends Element> T create(final Class<T> type) {
        String tagName = getElementTag(type);
        registerElement(type);
        org.jsoup.nodes.Element soupElement = createSoupElement(tagName);
        return type.cast(ElementReflectHelper.wrap(soupElement));
    }

    private static String getElementTag(final Class<?> type) {
        Tag tag = type.getAnnotation(Tag.class);
        String value = tag.value();
        return value;
    }

    public static TextNode createText(String text) {
        return (TextNode) ElementReflectHelper
                .wrap(new org.jsoup.nodes.TextNode(text, ""));
    }
}
