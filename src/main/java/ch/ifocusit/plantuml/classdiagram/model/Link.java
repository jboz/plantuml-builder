package ch.ifocusit.plantuml.classdiagram.model;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
public class Link {
    private String url;
    private String label;
    private String tooltip;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String render(LinkContext context) {
        if (LinkContext.CLASS.equals(context)) {
            if (StringUtils.isNotBlank(tooltip) && StringUtils.isNotBlank(url)) {
                return String.format("[[%s{%s}]]", url, tooltip);
            }
            if (StringUtils.isNotBlank(tooltip) && StringUtils.isBlank(url)) {
                return String.format("[[{%s}]]", tooltip);
            }
            if (StringUtils.isNotBlank(url)) {
                return String.format("[[%s]]", url);
            }
            return StringUtils.EMPTY;
        }
        if (StringUtils.isNotBlank(tooltip) && StringUtils.isNotBlank(url) && StringUtils.isNotBlank(label)) {
            return String.format("[[[%s{%s} %s]]]", url, tooltip, label);
        }
        if (StringUtils.isNotBlank(tooltip) && StringUtils.isNotBlank(url) && StringUtils.isBlank(label)) {
            return String.format("[[[%s{%s}]]]", url, tooltip);
        }
        if (StringUtils.isBlank(tooltip) && StringUtils.isNotBlank(url) && StringUtils.isNotBlank(label)) {
            return String.format("[[[%s %s]]]", url, label);
        }
        if (StringUtils.isNotBlank(tooltip) && StringUtils.isBlank(url) && StringUtils.isBlank(label)) {
            return String.format("[[[{%s}]]]", tooltip);
        }
        if (StringUtils.isNotBlank(url) && StringUtils.isBlank(tooltip) && StringUtils.isBlank(label)) {
            return String.format("[[[%s]]]", url);
        }
        if (StringUtils.isNotBlank(url) && StringUtils.isNotBlank(label) && StringUtils.isBlank(tooltip)) {
            return String.format("[[[%s %s]]]", url, label);
        }
        if (StringUtils.isNotBlank(tooltip) && StringUtils.isBlank(url)) {
            return String.format("[[[{%s}]]]", tooltip);
        }
        return StringUtils.EMPTY;
    }

    public enum LinkContext {
        CLASS, FIELD, METHOD
    }
}
