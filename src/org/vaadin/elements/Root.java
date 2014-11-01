package org.vaadin.elements;

import com.vaadin.ui.Component;

public interface Root extends Element {

    void fetchDom(Runnable runnable, Component... connectorsToInlcude);

}
