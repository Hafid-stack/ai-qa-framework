package parser;

import java.util.List;

public class PageSection {

    private final String name;
    private final List<ExtractedElement> elements;
    private final List<RepeatedComponentGroup> componentGroups;

    public PageSection(String name, List<ExtractedElement> elements) {
        this(name, elements, List.of());
    }

    public PageSection(String name, List<ExtractedElement> elements, List<RepeatedComponentGroup> componentGroups) {
        this.name = name;
        this.elements = elements;
        this.componentGroups = componentGroups;
    }

    public String getName() { return name; }
    public List<ExtractedElement> getElements() { return elements; }
    public List<RepeatedComponentGroup> getComponentGroups() { return componentGroups; }
    public boolean isEmpty() { return elements.isEmpty(); }
}
