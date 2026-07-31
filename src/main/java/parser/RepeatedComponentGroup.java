package parser;

import java.util.List;

// Represents one detected repeating pattern (e.g. "product card"), and the
// elements found inside each of its N repetitions.
public class RepeatedComponentGroup {

    private final String containerPath;      // the repeating card's own tag+class, e.g. "div.col-sm-4" (usable directly as a CSS selector)
    private final List<List<ExtractedElement>> instances; // one inner list per repetition
    private final String parentSelector;      // the shared parent's tag+class, e.g. "div.features_items" — narrows the runtime
                                               // selector to "parentSelector > containerPath" so it can't match an unrelated
                                               // element elsewhere on the page that happens to reuse the same class

    public RepeatedComponentGroup(String containerPath, List<List<ExtractedElement>> instances, String parentSelector) {
        this.containerPath = containerPath;
        this.instances = instances;
        this.parentSelector = parentSelector;
    }

    public String getContainerPath() { return containerPath; }
    public List<List<ExtractedElement>> getInstances() { return instances; }
    public int getRepeatCount() { return instances.size(); }
    public String getParentSelector() { return parentSelector; }

    // The selector to actually find all instances of this card at runtime, scoped to the
    // one parent they were detected under (e.g. "div.features_items > div.col-sm-4").
    public String getScopedSelector() {
        return parentSelector.isEmpty() ? containerPath : parentSelector + " > " + containerPath;
    }

    // A representative single instance, used to know what element types/roles this component has.
    public List<ExtractedElement> getSampleInstance() {
        return instances.isEmpty() ? List.of() : instances.get(0);
    }
}
