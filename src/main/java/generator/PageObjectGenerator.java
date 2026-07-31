package generator;

import parser.ExtractedElement;
import parser.RepeatedComponentGroup;
import parser.WebElementSelector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PageObjectGenerator {

    public String generateClassSource(String className, List<WebElementSelector> selectors) {
        return generateClassSource(className, selectors, List.of());
    }

    // componentGroups: repeated structural patterns found on this page (e.g. a product
    // card repeated once per item in a grid). Each becomes a nested inner class right
    // inside this same file — get a list of them via get<Name>List(), then call methods
    // directly on each one — rather than a separate generated file per pattern that a
    // tester would have no obvious way to discover or use.
    public String generateClassSource(String className, List<WebElementSelector> selectors,
                                       List<RepeatedComponentGroup> componentGroups) {
        StringBuilder sb = new StringBuilder();

        // Package + imports
        sb.append("package pages.generated;\n\n");
        sb.append("import base.BasePage;\n");
        sb.append("import org.openqa.selenium.By;\n");
        sb.append("import org.openqa.selenium.WebDriver;\n");
        if (!componentGroups.isEmpty()) {
            sb.append("import org.openqa.selenium.WebElement;\n");
            sb.append("import org.openqa.selenium.JavascriptExecutor;\n");
            sb.append("import java.util.List;\n");
            sb.append("import java.util.stream.Collectors;\n");
        }
        sb.append("\n");

        // Class declaration
        sb.append("public class ").append(className).append(" extends BasePage {\n\n");

        // Field declarations (the By locators)
        for (WebElementSelector selector : selectors) {
            sb.append("    ").append(buildFieldDeclaration(selector)).append("\n");
        }
        sb.append("\n");

        // Constructor
        sb.append("    public ").append(className).append("(WebDriver driver) {\n");
        sb.append("        super(driver);\n");
        sb.append("    }\n\n");

        // Action methods, one or two per element depending on category
        for (WebElementSelector selector : selectors) {
            sb.append(buildMethods(selector));
        }

        // Two different repeated patterns can share the same tag+class (e.g. two unrelated
        // "col-sm-4" grids), which would otherwise produce duplicate class/method names that
        // don't compile — resolve each group to a unique name up front.
        List<String> componentNames = resolveComponentNames(componentGroups);

        // One getter + one nested component class per repeated pattern, e.g.:
        //   for (ProductsPageBody.ColSm4Item item : page.getColSm4ItemList()) item.clickAddToCart();
        for (int i = 0; i < componentGroups.size(); i++) {
            sb.append(buildComponentGetter(componentGroups.get(i), componentNames.get(i)));
        }
        for (int i = 0; i < componentGroups.size(); i++) {
            sb.append(buildNestedComponentClass(componentGroups.get(i), componentNames.get(i)));
        }

        sb.append("}\n");
        return sb.toString();
    }

    private List<String> resolveComponentNames(List<RepeatedComponentGroup> componentGroups) {
        List<String> names = new ArrayList<>();
        Set<String> used = new LinkedHashSet<>();
        for (RepeatedComponentGroup group : componentGroups) {
            String base = componentClassName(group);
            String candidate = base;
            int suffix = 2;
            while (!used.add(candidate)) {
                candidate = base + suffix;
                suffix++;
            }
            names.add(candidate);
        }
        return names;
    }

    private String buildFieldDeclaration(WebElementSelector selector) {
        String fieldName = "by" + selector.getSelectorName();
        String byExpression;

        if (selector.getSelectorType().equals("xpath")) {
            // The text-match fallback fires for any tag (button, a, ...) — match the
            // element's own tag, not a hardcoded "a", or a text-matched <button> like
            // "Continue Shopping" silently generates a locator that can never find it.
            byExpression = "By.xpath(\"//" + selector.getElementCategory() + "[contains(normalize-space(.), '"
                    + selector.getSelectorValue() + "')]\")";
        } else {
            byExpression = "By.cssSelector(\"" + selector.getSelectorValue() + "\")";
        }

        return "private final By " + fieldName + " = " + byExpression + ";";
    }

    private String buildMethods(WebElementSelector selector) {
        String fieldName = "by" + selector.getSelectorName();
        String methodSuffix = selector.getSelectorName();
        StringBuilder methods = new StringBuilder();

        switch (selector.getElementCategory()) {
            case "input":
                methods.append("    public void type").append(methodSuffix).append("(String value) {\n");
                methods.append("        type(").append(fieldName).append(", value);\n");
                methods.append("    }\n\n");
                break;

            case "button":
                methods.append("    public void click").append(methodSuffix).append("() {\n");
                methods.append("        click(").append(fieldName).append(");\n");
                methods.append("    }\n\n");
                break;

            case "a":
                methods.append("    public void click").append(methodSuffix).append("() {\n");
                methods.append("        click(").append(fieldName).append(");\n");
                methods.append("    }\n\n");

                if (selector.hasVisibleText()) {
                    methods.append("    public String getText").append(methodSuffix).append("() {\n");
                    methods.append("        return getText(").append(fieldName).append(");\n");
                    methods.append("    }\n\n");
                }
                break;

            default:
                // Unknown category — generate a basic isDisplayed check as a safe fallback
                methods.append("    public boolean is").append(methodSuffix).append("Displayed() {\n");
                methods.append("        return isDisplayed(").append(fieldName).append(");\n");
                methods.append("    }\n\n");
        }

        return methods.toString();
    }

    public void writeToFile(String className, String sourceCode) throws java.io.IOException {
        String path = "src/main/java/pages/generated/" + className + ".java";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("src/main/java/pages/generated"));
        java.nio.file.Files.writeString(java.nio.file.Paths.get(path), sourceCode);
    }

    private String buildComponentGetter(RepeatedComponentGroup group, String name) {
        StringBuilder m = new StringBuilder();

        m.append("    // \"").append(group.getContainerPath()).append("\" repeated ")
                .append(group.getRepeatCount()).append(" times on the page this was generated from.\n");
        m.append("    public List<").append(name).append("> get").append(name).append("List() {\n");
        m.append("        return driver.findElements(By.cssSelector(\"").append(group.getScopedSelector())
                .append("\")).stream().map(").append(name).append("::new).collect(Collectors.toList());\n");
        m.append("    }\n\n");

        return m.toString();
    }

    // One inner class per repeated pattern, scoped to a single instance's root WebElement.
    // It's a non-static inner class (not a separate top-level class) purely so its methods
    // can reuse the enclosing page's `driver` field for the same JS-click fallback the
    // regular field methods use — construction still only needs a WebElement.
    private String buildNestedComponentClass(RepeatedComponentGroup group, String name) {
        List<ExtractedElement> sample = group.getSampleInstance();

        // Dedupe by role name first (e.g. two identical "Add to cart" buttons in one card
        // — a visible one and a hover-overlay clone — collapse to one clickAddToCart()).
        Set<String> seenRoles = new LinkedHashSet<>();
        List<ExtractedElement> roleElements = new ArrayList<>();
        for (ExtractedElement e : sample) {
            if (seenRoles.add(componentRoleName(e))) {
                roleElements.add(e);
            }
        }

        // Some cards contain several DIFFERENTLY-named roles that nonetheless share the same
        // tag/class (e.g. a category accordion with four plain, unclassed <a> links: Women,
        // Dress, Tops, Saree). A tag+class selector can't tell those apart, so any role whose
        // fragment isn't unique within this card falls back to a text-based match instead.
        Map<String, Long> fragmentCounts = new LinkedHashMap<>();
        for (ExtractedElement e : roleElements) {
            fragmentCounts.merge(relativeSelectorFragment(e), 1L, Long::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("    // One instance of the repeated \"").append(group.getContainerPath()).append("\" pattern.\n");
        sb.append("    // Usage: get").append(name).append("List().get(0).click")
                .append(componentRoleName(roleElements.get(0))).append("();\n");
        sb.append("    public class ").append(name).append(" {\n");
        sb.append("        private final WebElement root;\n\n");
        sb.append("        public ").append(name).append("(WebElement root) {\n");
        sb.append("            this.root = root;\n");
        sb.append("        }\n\n");
        // Escape hatch: for anything the generator didn't anticipate — hovering this
        // card before clicking, reading its text, checking visibility, etc.
        sb.append("        public WebElement getRoot() {\n");
        sb.append("            return root;\n");
        sb.append("        }\n\n");

        for (ExtractedElement element : roleElements) {
            String role = componentRoleName(element);
            boolean ambiguous = fragmentCounts.get(relativeSelectorFragment(element)) > 1;
            sb.append(buildNestedComponentMethod(role, element, ambiguous));
        }

        sb.append("    }\n\n");
        return sb.toString();
    }

    // Names a role from an element's own text/type, e.g. "AddToCart", "ViewProduct".
    private String componentRoleName(ExtractedElement element) {
        String source = !element.getText().isEmpty() ? element.getText() : element.getType();
        String cleaned = source.replaceAll("[^\\p{Print}]", "").trim();
        String[] parts = cleaned.split("[^a-zA-Z0-9]+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.length() > 0 ? result.toString() : "Element";
    }

    // Names the inner class from the card's own tag+class, e.g. "div.col-sm-4" -> "ColSm4Item".
    private String componentClassName(RepeatedComponentGroup group) {
        String path = group.getContainerPath();
        String token = path.contains(".") ? path.substring(path.indexOf('.') + 1) : path;
        String[] parts = token.split("[^a-zA-Z0-9]+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return (result.length() > 0 ? result.toString() : "Repeated") + "Item";
    }

    private String buildNestedComponentMethod(String role, ExtractedElement element, boolean ambiguous) {
        StringBuilder m = new StringBuilder();
        m.append("        public void click").append(role).append("() {\n");

        if (ambiguous && !element.getText().isEmpty()) {
            String cleanText = element.getText().replaceAll("[^\\p{Print}]", "").trim();
            m.append("            WebElement el = root.findElement(By.xpath(\".//")
                    .append(element.getTagName())
                    .append("[contains(normalize-space(.), '").append(cleanText).append("')]\"));\n");
        } else {
            m.append("            WebElement el = root.findElement(By.cssSelector(\"")
                    .append(relativeSelectorFragment(element)).append("\"));\n");
        }

        m.append("            ((JavascriptExecutor) driver).executeScript(\"arguments[0].click();\", el);\n");
        m.append("        }\n\n");

        return m.toString();
    }

    // Selector for this element relative to its card's root WebElement (root.findElement(...)),
    // so it only ever matches within that one card instance — no page-wide id needed.
    private String relativeSelectorFragment(ExtractedElement element) {
        String cls = element.getCssClass();
        if (!cls.isEmpty()) {
            return element.getTagName() + "." + cls.trim().split("\\s+")[0];
        }
        // No class of its own — a bare tag could also match a classed sibling of the same
        // tag within the card (e.g. an unclassed "View Product" link next to a classed
        // "Add to cart" link, both <a>), so exclude classed elements explicitly.
        return element.getTagName() + ":not([class])";
    }
}
