package ch.ifocusit.plantuml.classdiagram.model;

public class Link {
    private String url;
    private String label;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("[[%s{%s}]]", url, label);
    }
}
