package org.vaadin.elements;

import java.util.Optional;

import org.vaadin.elements.impl.RootImpl;

import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.server.Extension;
import com.vaadin.ui.AbstractComponent;

import elemental.json.JsonArray;
import elemental.json.JsonValue;
import elemental.json.impl.JsonUtil;

@JavaScript("elementui.js")
public class ElementIntegration extends AbstractJavaScriptExtension {
    private final RootImpl root = new RootImpl(this);

    private ElementIntegration(AbstractComponent target) {
        super.extend(target);

        addFunction("callback", arguments -> root.handleCallback(arguments));
        addFunction("getDom", this::getDom);
    }

    private void getDom(JsonArray arguments) {
        String html = arguments.getString(0);

        root.init(html);
    }

    public Element getRoot() {
        return root;
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        JsonValue payload = root.flushPendingCommands();

        String query = getUI().getPage().getLocation().getQuery();
        if (query != null && query.contains("debug")) {
            System.out.println(JsonUtil.stringify(payload));
            System.out.println(root.asHtml());
        }

        callFunction("run", payload);

        super.beforeClientResponse(initial);
    }

    public static Root getRoot(AbstractComponent target) {
        Optional<Extension> existing = target.getExtensions().stream()
                .filter(e -> e.getClass() == ElementIntegration.class)
                .findFirst();
        ElementIntegration integration = existing.isPresent() ? (ElementIntegration) existing
                .get() : new ElementIntegration(target);

        return integration.root;
    }

}
