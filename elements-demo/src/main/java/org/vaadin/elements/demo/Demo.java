package org.vaadin.elements.demo;

import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.vaadin.elements.Element;
import org.vaadin.elements.Elements;
import org.vaadin.elements.Root;
import org.vaadin.elements.TextNode;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
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
        // Root root = ElementIntegration.getRoot(this);

        // demoNewElements(root);

        // demoExistingElements(root);

        // demoPaperComponents(root);
    }

    private void demoExistingElements(Root root) {
        Button button = new Button("Test", c -> {
            root.fetchDom(() -> Notification.show(root.asHtml()));
        });

        Label label = new Label("Test");
        VerticalLayout layout = new VerticalLayout(button, label);

        setContent(layout);

        root.fetchDom(() -> {
            Optional<Element> span = root.querySelector("span > span");
            span.ifPresent(s -> s.setAttribute("style", "color: blue"));
        }, layout, button, label);
    }

    private void demoNewElements(Root root) {
        root.setInnerHtml("<h1>Hello world</h1>");
        TextNode text = Elements.createText("Hello");
        root.appendChild(text);

        Element h1 = root.querySelector("h1").get();

        h1.addEventListener("click", arguments -> {
            h1.setAttribute("style", "color: blue");
        });
    }
}