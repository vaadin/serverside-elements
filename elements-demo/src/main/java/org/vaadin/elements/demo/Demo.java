package org.vaadin.elements.demo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

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
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(new Html5InputDemo(), "HTML5 inputs");
        // Does not work in Firefox
        // tabSheet.addTab(new GoogleMapDemo(), "Web components");
        tabSheet.addTab(new ExistingElementsDemo(), "Existing elements");
        tabSheet.addTab(new PaperElementsDemo(), "Paper compoments");

        VerticalLayout layout = new VerticalLayout(tabSheet);
        layout.setSizeFull();
        layout.setMargin(true);
        setContent(layout);
    }
}