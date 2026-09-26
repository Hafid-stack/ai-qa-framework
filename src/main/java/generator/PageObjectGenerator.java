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
            byExpression = "By.xpath(" + javaLiteral(textContainsXPath("//" + selector.getElementCategory(),
                    selector.getSelectorValue())) + ")";
        } else {
            byExpression = "By.cssSelector(" + javaLiteral(selector.getSelectorValue()) + ")";
        }

        return "private final By " + fieldName + " = " + byExpression + ";";
    }

    // ---------------------------------------------------------------------------------
    // Emission safety. Page text is arbitrary user-facing content and ends up inside two
    // nested languages at once: a Java string literal and an XPath expression. A product
    // called Women's Dress or a 12" Monitor would otherwise produce, respectively, an XPath
    // the browser rejects at runtime and a Java file that does not compile.
    // ---------------------------------------------------------------------------------

    // Wraps text as a Java string literal, escaping backslash, quote and the characters
    // that would otherwise terminate or corrupt the literal.
    private String javaLiteral(String raw) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    // XPath 1.0 has no escape mechanism inside string literals, so the delimiter is chosen
    // to suit the text. When the text contains both kinds of quote, concat() is the only
    // portable construction.
    private String xpathLiteral(String raw) {
        if (!raw.contains("'")) return "'" + raw + "'";
        if (!raw.contains("\"")) return "\"" + raw + "\"";

        StringBuilder sb = new StringBuilder("concat(");
        String[] segments = raw.split("'", -1);
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) sb.append(", \"'\", ");
            sb.append('\'').append(segments[i]).append('\'');
        }
        return sb.append(')').toString();
    }

    private String textContainsXPath(String prefix, String text) {
        return prefix + "[contains(normalize-space(.), " + xpathLiteral(text) + ")]";
    }

    private String buildMethods(WebElementSelector selector) {
        String fieldName = "by" + selector.getSelectorName();
        String methodSuffix = selector.getSelectorName();
        StringBuilder methods = new StringBuilder();

        switch (selector.getElementCategory()) {
            case "input":
                // <input type="submit"> (SauceDemo's login button) and the other non-text
                // input types are clicked, not typed into — give them the button method.
                String inputType = selector.getInputType() == null ? "" : selector.getInputType().toLowerCase();
                if (Set.of("submit", "button", "reset", "image", "checkbox", "radio").contains(inputType)) {
                    methods.append("    public void click").append(methodSuffix).append("() {\n");
                    methods.append("        click(").append(fieldName).append(");\n");
                    methods.append("    }\n\n");
                    break;
                }
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
        m.append("        return driver.findElements(By.cssSelector(").append(javaLiteral(group.getScopedSelector()))
                .append(")).stream().map(").append(name).append("::new).collect(Collectors.toList());\n");
        m.append("    }\n\n");

        return m.toString();
    }

    // One inner class per repeated pattern, scoped to a single instance's root WebElement.
    // It's a non-static inner class (not a separate top-level class) purely so its methods
    // can reuse the enclosing page's `driver` field for the same JS-click fallback the
    // regular field methods use — construction still only needs a WebElement.
    private String buildNestedComponentClass(RepeatedComponentGroup group, String name) {
        List<ComponentRole> roles = resolveRoles(group);

        StringBuilder sb = new StringBuilder();
        sb.append("    // One instance of the repeated \"").append(group.getContainerPath()).append("\" pattern.\n");
        sb.append("    // Usage: get").append(name).append("List().get(0).click")
                .append(roles.get(0).name).append("();\n");
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

        for (ComponentRole role : roles) {
            sb.append(buildNestedComponentMethod(role, group.getContainerPath()));
        }

        sb.append("    }\n\n");
        return sb.toString();
    }

    // ---------------------------------------------------------------------------------
    // Role resolution.
    //
    // A component's methods describe ROLES — what a thing is for — and a role must hold for
    // every instance of the pattern. The text of a product-title link is not a role: it is
    // data, different in every card. Naming a method and its locator from it produced
    // clickSauceLabsBackpack(), which worked on card 1 and threw NoSuchElementException on
    // the other five.
    //
    // The detector retains every instance of a pattern, so invariance can simply be checked:
    // text that is identical across all instances ("Add to cart") is a role and may name the
    // method; text that varies is data and the role falls back to the element's type, with
    // the element addressed by position within the card rather than by its words.
    // ---------------------------------------------------------------------------------

    private static final class ComponentRole {
        final ExtractedElement element;
        final String name;
        final boolean textIsInvariant;
        final boolean fragmentIsAmbiguous;
        final int positionAmongSameFragment;

        ComponentRole(ExtractedElement element, String name, boolean textIsInvariant,
                      boolean fragmentIsAmbiguous, int positionAmongSameFragment) {
            this.element = element;
            this.name = name;
            this.textIsInvariant = textIsInvariant;
            this.fragmentIsAmbiguous = fragmentIsAmbiguous;
            this.positionAmongSameFragment = positionAmongSameFragment;
        }
    }

    private List<ComponentRole> resolveRoles(RepeatedComponentGroup group) {
        List<ExtractedElement> sample = group.getSampleInstance();
        String cardSelector = group.getContainerPath();

        Map<String, Long> fragmentCounts = new LinkedHashMap<>();
        for (ExtractedElement e : sample) {
            fragmentCounts.merge(relativeSelectorFragment(e), 1L, Long::sum);
        }

        Map<String, Integer> fragmentPosition = new LinkedHashMap<>();
        Set<String> takenNames = new LinkedHashSet<>();
        Map<String, ComponentRole> collapsedByInvariantRole = new LinkedHashMap<>();
        List<ComponentRole> roles = new ArrayList<>();

        for (int i = 0; i < sample.size(); i++) {
            ExtractedElement element = sample.get(i);
            String fragment = relativeSelectorFragment(element);
            boolean invariant = textIsInvariantAcrossInstances(group, i);
            boolean usableAsRole = invariant && !element.getText().isEmpty();

            // Positions are assigned before anything is collapsed, because at run time
            // root.findElements(fragment) returns every matching descendant in document
            // order — including the ones collapsed away here. Skipping them would shift
            // every subsequent index by one. Only the root itself is excluded, since
            // WebElement.findElements() never returns the element it was called on.
            int position = -1;
            if (!isCardRootItself(element, cardSelector)) {
                position = fragmentPosition.merge(fragment, 0, (oldValue, ignored) -> oldValue + 1);
            }

            // A visible control and its hover-overlay clone are the same role, and must
            // collapse or the generated class declares the same method twice.
            String preferred = usableAsRole
                    ? pascalCase(element.getText())
                    : pascalCase(element.getType());
            if (usableAsRole && collapsedByInvariantRole.containsKey(preferred + "|" + fragment)) {
                continue;
            }

            String unique = preferred.isEmpty() ? "Element" : preferred;
            int suffix = 2;
            while (!takenNames.add(unique)) {
                unique = preferred + suffix++;
            }

            ComponentRole role = new ComponentRole(element, unique, usableAsRole,
                    fragmentCounts.get(fragment) > 1, position);
            roles.add(role);
            if (usableAsRole) {
                collapsedByInvariantRole.put(preferred + "|" + fragment, role);
            }
        }
        return roles;
    }

    // True when this element's visible text is the same in every detected instance of the
    // pattern — the test that distinguishes a role from a value.
    private boolean textIsInvariantAcrossInstances(RepeatedComponentGroup group, int elementIndex) {
        List<List<ExtractedElement>> instances = group.getInstances();
        if (instances.size() < 2) return true;

        String reference = null;
        for (List<ExtractedElement> instance : instances) {
            // Instances of different shape cannot be compared positionally; treat the text
            // as varying, which is the conservative answer.
            if (instance.size() != instances.get(0).size() || elementIndex >= instance.size()) return false;
            String text = instance.get(elementIndex).getText();
            if (reference == null) reference = text;
            else if (!reference.equals(text)) return false;
        }
        return true;
    }

    private String pascalCase(String source) {
        String cleaned = source == null ? "" : source.replaceAll("[^\\p{Print}]", "").trim();
        StringBuilder result = new StringBuilder();
        for (String part : cleaned.split("[^a-zA-Z0-9]+")) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
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

    private String buildNestedComponentMethod(ComponentRole role, String cardSelector) {
        ExtractedElement element = role.element;
        StringBuilder m = new StringBuilder();
        m.append("        public void click").append(role.name).append("() {\n");

        // The card can BE the interactive element: a repeating menu of <a class="bm-item">
        // is detected as the card, and the same <a> is then extracted as its only content.
        // jsoup's Element.select() includes the element itself, but Selenium's
        // WebElement.findElement() searches descendants only — so searching for the card's
        // own selector inside the card matches nothing and every call throws
        // NoSuchElementException. When the element is the root, act on the root directly.
        if (isCardRootItself(element, cardSelector)) {
            m.append("            // This component's root element IS the interactive element.\n");
            m.append("            WebElement el = root;\n");

        } else if (!role.fragmentIsAmbiguous) {
            m.append("            WebElement el = root.findElement(By.cssSelector(")
                    .append(javaLiteral(relativeSelectorFragment(element))).append("));\n");

        } else if (role.textIsInvariant) {
            // Several roles share this tag+class, but this one's text is the same in every
            // instance, so it identifies the role rather than the instance.
            String cleanText = element.getText().replaceAll("[^\\p{Print}]", "").trim();
            m.append("            WebElement el = root.findElement(By.xpath(")
                    .append(javaLiteral(textContainsXPath(".//" + element.getTagName(), cleanText)))
                    .append("));\n");

        } else {
            // Shares a selector with a sibling AND its text differs between instances, so
            // neither can address it. Position within the card is structure, not data, and
            // is the same in every instance.
            m.append("            // Text varies across instances, so it names the instance, not the role;\n");
            m.append("            // position within the card is the stable discriminator.\n");
            m.append("            WebElement el = root.findElements(By.cssSelector(")
                    .append(javaLiteral(relativeSelectorFragment(element))).append(")).get(")
                    .append(role.positionAmongSameFragment).append(");\n");
        }

        m.append("            ((JavascriptExecutor) driver).executeScript(\"arguments[0].click();\", el);\n");
        m.append("        }\n\n");

        return m.toString();
    }

    // True when the element's own tag+class selector is the card's selector — i.e. the
    // extraction re-captured the card root rather than something nested inside it.
    private boolean isCardRootItself(ExtractedElement element, String cardSelector) {
        return cardSelector != null && cardSelector.equals(relativeSelectorFragment(element));
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
