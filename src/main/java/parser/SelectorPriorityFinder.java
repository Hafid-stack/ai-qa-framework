package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SelectorPriorityFinder {

    // Each strategy: given an element, try to build a selector. Return null if this attribute isn't usable.
    private final List<Function<ExtractedElement, WebElementSelector>> strategies = List.of(
            this::tryDataTest,
            this::tryId,
            this::tryName,
            this::tryLinkText
    );

    public List<WebElementSelector> getOrder(List<ExtractedElement> elements) {
        List<WebElementSelector> selectors = new ArrayList<>();

        for (ExtractedElement element : elements) {
            WebElementSelector selector = findBestSelector(element);
            if (selector != null) {
                selectors.add(selector);
            }
        }
        return selectors;
    }

    private WebElementSelector findBestSelector(ExtractedElement element) {
        for (Function<ExtractedElement, WebElementSelector> strategy : strategies) {
            WebElementSelector result = strategy.apply(element);
            if (result != null) {
                return result; // first match wins — this IS the priority order
            }
        }
        return null; // no usable attribute found at all
    }

    private WebElementSelector tryDataTest(ExtractedElement element) {
        if (element.getDataTest() == null || element.getDataTest().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), "[data-test='" + element.getDataTest() + "']", "css");
    }

    private WebElementSelector tryId(ExtractedElement element) {
        if (element.getId() == null || element.getId().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), "#" + element.getId(), "css");
    }

    private WebElementSelector tryName(ExtractedElement element) {
        if (element.getName() == null || element.getName().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), "[name='" + element.getName() + "']", "css");
    }

    private WebElementSelector tryLinkText(ExtractedElement element) {
        if (element.getText() == null || element.getText().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), element.getText(), "linkText");
    }

    private String buildVariableName(ExtractedElement element) {
        // Prefer the specific identifier over the generic type, to avoid name collisions
        // (e.g. two type="text" inputs would otherwise both generate the same variable name)
        String source;
        if (element.getDataTest() != null && !element.getDataTest().isEmpty()) {
            source = element.getDataTest();
        } else if (element.getId() != null && !element.getId().isEmpty()) {
            source = element.getId();
        } else if (element.getName() != null && !element.getName().isEmpty()) {
            source = element.getName();
        } else if (element.getType() != null && !element.getType().isEmpty()) {
            source = element.getType();
        } else {
            source = "element";
        }

        return source.substring(0, 1).toUpperCase() + source.substring(1);
    }
}