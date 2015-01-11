package org.vaadin.elements.demo;

import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.vaadin.elements.Element;
import org.vaadin.elements.ElementIntegration;
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
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("valo")
public class Demo extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = Demo.class)
    public static class Servlet extends VaadinServlet {
        @Override
        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            // XXX Workaround until #9045 has been merged
            getService().addSessionInitListener(
                    e -> e.getSession().addBootstrapListener(
                            new BootstrapListenerImplementation()));
        }
    }

    @Override
    protected void init(VaadinRequest request) {
        Root root = ElementIntegration.getRoot(this);

        // demoNewElements(root);

        // demoExistingElements(root);

        demoPaperComponents(root);
    }

    private void demoPaperComponents(Root root) {
        PaperButton basicButton = PaperButton.create("Basic button");
        basicButton.setRaised(true);
        basicButton.addEventListener("click", args -> {
            Notification.show("Clicked");
        });

        PaperButton notRaisedButton = PaperButton.create("Not raised");
        notRaisedButton.setRaised(false);

        PaperButton noInkButton = PaperButton.create("No ink");
        noInkButton.setRaised(true);
        noInkButton.setNoink(true);

        PaperButton disabledButton = PaperButton.create("Disabled");
        disabledButton.setDisabled(true);

        Layout horizontal = Layout.horizontal();
        horizontal.setJustified(true);
        horizontal.setAttribute("style", "width: 600px");

        horizontal.appendChild(basicButton);
        horizontal.appendChild(notRaisedButton);
        horizontal.appendChild(noInkButton);
        horizontal.appendChild(disabledButton);

        PaperSlider slider = PaperSlider.create();
        slider.setValue(50);
        slider.addEventListener("change", arguments -> {
            Notification.show("Value changed to " + slider.getValue());
        });

        Layout vertical = Layout.vertical();
        vertical.appendChild(slider);
        vertical.appendChild(horizontal);

        root.appendChild(vertical);

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