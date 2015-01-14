package org.vaadin.elements.demo;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public abstract class AbstractElementsDemo extends CustomComponent {

    private final VerticalLayout layout = new VerticalLayout();

    public AbstractElementsDemo() {
        setSizeFull();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(true);
        setCompositionRoot(layout);
    }

    @Override
    public void attach() {
        super.attach();
        if (layout.getComponentCount() == 0) {
            Component demoView = getDemoView();
            layout.addComponents(new Label(getDemoDescription()), demoView);
            layout.setExpandRatio(demoView, 1);
        }
    }

    protected abstract String getDemoDescription();

    protected abstract Component getDemoView();
}
