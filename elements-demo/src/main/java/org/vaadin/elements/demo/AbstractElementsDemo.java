package org.vaadin.elements.demo;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractElementsDemo extends CustomComponent {

    private final VerticalLayout layout = new VerticalLayout();

    public AbstractElementsDemo() {
        setCompositionRoot(layout);
    }

    @Override
    public void attach() {
        super.attach();
        if (layout.getComponentCount() == 0) {
            layout.addComponents(new Label(getDemoDescription()), getDemoView());
        }
    }

    protected abstract String getDemoDescription();

    protected abstract Component getDemoView();
}
