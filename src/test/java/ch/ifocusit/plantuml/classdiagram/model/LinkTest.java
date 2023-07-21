package ch.ifocusit.plantuml.classdiagram.model;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class LinkTest {

    public static final String URL = "https://link.com";
    public static final String LABEL = "label";
    public static final String TOOLTIP = "tooltip";

    @ParameterizedTest
    @CsvSource({
            ",,,,",
            URL + ",,,[[" + URL + "]]",
            URL + "," + LABEL + ",,[[" + URL + "]]",
            URL + "," + LABEL + "," + TOOLTIP + ",[[" + URL + "{" + TOOLTIP + "}]]",
            ",," + TOOLTIP + ",[[{" + TOOLTIP + "}]]",
    })
    void render_class(String url, String label, String tooltip, String expected) {
        // given
        Link link = new Link();
        link.setUrl(url);
        link.setLabel(label);
        link.setTooltip(tooltip);
        // when
        String actual = link.render(Link.LinkContext.CLASS);
        // then
        assertThat(actual).isEqualTo(expected == null ? StringUtils.EMPTY : expected);
    }

    @ParameterizedTest
    @CsvSource({
            ",,,,",
            URL + ",,,[[[" + URL + "]]]",
            "," + LABEL + ",,",
            ",," + TOOLTIP + ",[[[{" + TOOLTIP + "}]]]",
            URL + "," + LABEL + ",,[[[" + URL + " " + LABEL + "]]]",
            URL + ",," + TOOLTIP + ",[[[" + URL + "{" + TOOLTIP + "}]]]",
            URL + "," + LABEL + "," + TOOLTIP + ",[[[" + URL + "{" + TOOLTIP + "} " + LABEL + "]]]",
            "," + LABEL + "," + TOOLTIP + ",[[[{" + TOOLTIP + "}]]]",
    })
    void render_field(String url, String label, String tooltip, String expected) {
        // given
        Link link = new Link();
        link.setUrl(url);
        link.setLabel(label);
        link.setTooltip(tooltip);
        // when
        String actual = link.render(Link.LinkContext.FIELD);
        // then
        assertThat(actual).isEqualTo(expected == null ? StringUtils.EMPTY : expected);
    }
}