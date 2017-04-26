/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.elements.polyfill;

import java.util.Collections;
import java.util.logging.Logger;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapHandler.BootstrapContext;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.ServiceInitEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServiceInitListener;

public class LoadPolyfill implements VaadinServiceInitListener {
    static final String SERVLET_PARAMETER_LOAD_WEBCOMPONENTS_POLYFILL = "loadWebcomponentsPolyfill";
    static final String WARNING_LOAD_WEBCOMPONENTS_POLYFILL_NOT_RECOGNIZED = "\n"
            + "===========================================================\n"
            + "WARNING: loadWebcomponentsPolyfill has been set to an unrecognized value\n"
            + "in web.xml. The permitted values are \"yes\", \"no\",\n"
            + "and \"automatic\". The default of \"automatic\" will be used.\n"
            + "===========================================================";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        WebComponentsPolyfillMode polyfillMode = checkWebComponentsPolyfillMode(
                event.getSource());

        event.getSource().addSessionInitListener(e -> {
            e.getSession().addBootstrapListener(new BootstrapListener() {
                @Override
                public void modifyBootstrapPage(
                        BootstrapPageResponse response) {
                    if (polyfillMode.shouldLoad(response.getRequest()
                            .getService().getClassLoader())) {
                        BootstrapFragmentResponse fragmentResponse = new BootstrapFragmentResponse(
                                response.getBootstrapHandler(),
                                response.getRequest(), response.getSession(),
                                response.getUiClass(), Collections.emptyList(),
                                response.getUIProvider());
                        BootstrapContext context = response
                                .getBootstrapHandler().new BootstrapContext(
                                        VaadinService.getCurrentResponse(),
                                        fragmentResponse);

                        String webcomponentsJS = "frontend://webcomponentsjs/webcomponents-lite.js";
                        webcomponentsJS = context.getUriResolver()
                                .resolveVaadinUri(webcomponentsJS);
                        Element script = new Element(Tag.valueOf("script"), "")
                                .attr("type", "text/javascript")
                                .attr("src", webcomponentsJS);
                        response.getDocument().head().insertChildren(0,
                                Collections.singletonList(script));
                    }
                }

                @Override
                public void modifyBootstrapFragment(
                        BootstrapFragmentResponse response) {

                }
            });
        });
    }

    private WebComponentsPolyfillMode checkWebComponentsPolyfillMode(
            VaadinService vaadinService) {
        String mode = vaadinService.getDeploymentConfiguration()
                .getApplicationOrSystemProperty(
                        SERVLET_PARAMETER_LOAD_WEBCOMPONENTS_POLYFILL,
                        WebComponentsPolyfillMode.AUTOMATIC.toString());
        try {
            return Enum.valueOf(WebComponentsPolyfillMode.class,
                    mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning(
                    WARNING_LOAD_WEBCOMPONENTS_POLYFILL_NOT_RECOGNIZED);
            return WebComponentsPolyfillMode.AUTOMATIC;
        }
    }

    private static Logger getLogger() {
        return Logger.getLogger(LoadPolyfill.class.getName());
    }

}
