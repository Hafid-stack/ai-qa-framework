package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SelectorPriorityFinder {

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
                return result;
            }
        }
        return null;
    }

    private WebElementSelector tryDataTest(ExtractedElement element) {
        if (element.getDataTest() == null || element.getDataTest().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), "[data-test='" + element.getDataTest() + "']", "css", element.getTagName());
    }

    private WebElementSelector tryId(ExtractedElement element) {
        if (element.getId() == null || element.getId().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), "#" + element.getId(), "css", element.getTagName());
    }

    private WebElementSelector tryName(ExtractedElement element) {
        if (element.getName() == null || element.getName().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), "[name='" + element.getName() + "']", "css", element.getTagName());
    }

    private WebElementSelector tryLinkText(ExtractedElement element) {
        if (element.getText() == null || element.getText().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), element.getText(), "linkText", element.getTagName());
    }

    private String buildVariableName(ExtractedElement element) {
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
        return toValidJavaIdentifier(source);
    }

    private String toValidJavaIdentifier(String raw) {
        String[] parts = raw.split("[^a-zA-Z0-9]+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        return result.length() > 0 ? result.toString() : "Element";
    }
}