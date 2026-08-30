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
        // The attribute NAME is taken from the element, not from a constant: a site that
        // uses data-qa must produce [data-qa='...']. Hard-coding "data-test" here emitted a
        // locator that was syntactically valid and could never match on any such site —
        // the same silent-failure class as the tag-agnostic XPath fallback fixed earlier.
        String attributeName = element.getAutomationAttributeName();
        return new WebElementSelector(buildVariableName(element),
                "[" + attributeName + "='" + element.getDataTest() + "']",
                "css", element.getTagName(), elementHasText(element), "dataTest");
    }

    private WebElementSelector tryId(ExtractedElement element) {
        if (element.getId() == null || element.getId().isEmpty()) return null;
        // Attribute form rather than "#id": an id may legally contain "." or "(" — SauceDemo
        // has id="add-to-cart-test.allthethings()-t-shirt-(red)" — which is not a valid CSS
        // id fragment. [id='...'] is the equivalent form that is always valid.
        return new WebElementSelector(buildVariableName(element), "[id='" + element.getId() + "']",
                "css", element.getTagName(), elementHasText(element), "id");
    }

    private WebElementSelector tryName(ExtractedElement element) {
        if (element.getName() == null || element.getName().isEmpty()) return null;
        return new WebElementSelector(buildVariableName(element), "[name='" + element.getName() + "']",
                "css", element.getTagName(), elementHasText(element), "name");
    }

    private WebElementSelector tryLinkText(ExtractedElement element) {
        if (element.getText() == null || element.getText().isEmpty()) return null;

        // Raw page text often carries leading/trailing whitespace and icon-font glyphs,
        // so an exact By.linkText match fails. normalize-space() collapses whitespace,
        // and matching on contains() tolerates leading icon characters.
        String cleanText = element.getText().replaceAll("[^\\p{Print}]", "").trim();

        return new WebElementSelector(buildVariableName(element), cleanText,
                "xpath", element.getTagName(), elementHasText(element), "text");
    }

    private boolean elementHasText(ExtractedElement element) {
        return element.getText() != null && !element.getText().isEmpty();
    }

    private String buildVariableName(ExtractedElement element) {
        String source;
        if (element.getDataTest() != null && !element.getDataTest().isEmpty()) {
            source = element.getDataTest();
        } else if (element.getId() != null && !element.getId().isEmpty()) {
            source = element.getId();
        } else if (element.getName() != null && !element.getName().isEmpty()) {
            source = element.getName();
        } else if (element.getText() != null && !element.getText().isEmpty()) {
            source = element.getText();
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