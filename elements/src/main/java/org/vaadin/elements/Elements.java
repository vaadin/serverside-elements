package org.vaadin.elements;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.vaadin.elements.impl.ElementReflectHelper;

public class Elements {

    private static final Map<String, Class<? extends Element>> registeredElements = new ConcurrentHashMap<>();

    static void clearRegistration() {
        // For testing
        registeredElements.clear();
    }

    public static <T extends Element> void registerElement(Class<T> type) {
        String elementTag = getElementTag(type, true);
        if (elementTag != null) {
            registeredElements.put(elementTag, type);
        }
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
        String tagName = getElementTag(type, false);
        registerElement(type);
        org.jsoup.nodes.Element soupElement = createSoupElement(tagName);
        return type.cast(ElementReflectHelper.wrap(soupElement, type));
    }

    private static String getElementTag(final Class<?> type,
            boolean onlyExclusive) {
        Tag tag = type.getAnnotation(Tag.class);
        if (onlyExclusive && !tag.exclusive()) {
            return null;
        }
        String value = tag.value();
        return value;
    }

    public static TextNode createText(String text) {
        return (TextNode) ElementReflectHelper
                .wrap(new org.jsoup.nodes.TextNode(text));
    }
}
