package org.vaadin.elements.demo;

import org.vaadin.elements.Element;
import org.vaadin.elements.Elements;
import org.vaadin.elements.Import;
import org.vaadin.elements.Tag;

@Tag("paper-slider")
@Import("VAADIN/bower_components/paper-slider/paper-slider.html")
public interface PaperSlider extends Element {
    public static PaperSlider create() {
        return Elements.create(PaperSlider.class);
    }

    public double setValue(double value);

    public double getValue();

}
