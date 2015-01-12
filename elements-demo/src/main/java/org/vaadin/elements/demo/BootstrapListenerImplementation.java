package org.vaadin.elements.demo;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;

public class BootstrapListenerImplementation implements BootstrapListener {
    @Override
    public void modifyBootstrapPage(BootstrapPageResponse response) {
        response.getDocument()
                .head()
                .appendElement("script")
                .attr("src",
                        "VAADIN/bower_components/webcomponentsjs/webcomponents.js");
    }

    @Override
    public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
        // nothing to do
    }
}
