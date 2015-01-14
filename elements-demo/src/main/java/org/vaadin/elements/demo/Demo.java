package org.vaadin.elements.demo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
@Theme("valo")
public class Demo extends UI {

    @WebServlet(value = "/*", asyncSupported = true, loadOnStartup = 1)
    @VaadinServletConfiguration(productionMode = false, ui = Demo.class)
    public static class Servlet extends VaadinServlet {
        @Override
        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            // XXX Workaround until we can use beta2
            getService().addSessionInitListener(
                    e -> e.getSession().addBootstrapListener(
                            new BootstrapListenerImplementation()));
        }
    }

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        addDemo(layout, new Html5InputDemo(), "HTML5 inputs");
        // Does not work in Firefox
        // tabSheet.addTab(new GoogleMapDemo(), "Web components");
        addDemo(layout, new ExistingElementsDemo(), "Existing elements");
        addDemo(layout, new PaperElementsDemo(), "Paper compoments");

        layout.setMargin(true);
        // layout.setSpacing(true);
        setContent(layout);
    }

    private void addDemo(VerticalLayout layout, AbstractElementsDemo demo,
            String caption) {
        Label label = new Label(caption);
        label.addStyleName(ValoTheme.LABEL_H1);
        // label.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        layout.addComponent(label);

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setMargin(new MarginInfo(false, false, false, true));
        wrapper.setSpacing(true);
        Label descriptionLabel = new Label(demo.getDemoDescription());
        descriptionLabel.setWidth("600px");
        wrapper.addComponent(descriptionLabel);
        wrapper.addComponent(demo.getDemoView());
        layout.addComponent(wrapper);
    }
}