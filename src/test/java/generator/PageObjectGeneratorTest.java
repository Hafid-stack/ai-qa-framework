package generator;

import org.testng.Assert;
import org.testng.annotations.Test;
import parser.ExtractedElement;
import parser.RepeatedComponentGroup;
import parser.WebElementSelector;

import java.util.List;

/**
 * Unit tests for the code-emission stage.
 *
 * The generator's output is source code, so these assertions are made on the emitted text.
 * Every case below corresponds to a defect that was actually observed, or to a property the
 * report claims the pipeline has and which would otherwise be unverified.
 */
public class PageObjectGeneratorTest {

    private final PageObjectGenerator generator = new PageObjectGenerator();

    private WebElementSelector selector(String name, String value, String type,
                                        String category, boolean hasText, String matchedVia) {
        return new WebElementSelector(name, value, type, category, hasText, matchedVia);
    }

    // ---------------------------------------------------------------------------
    // Skeleton.
    // ---------------------------------------------------------------------------

    @Test
    public void emitsACompleteCompilationUnitInTheGeneratedPackage() {
        String source = generator.generateClassSource("LoginPage", List.of(
                selector("Username", "[data-test='username']", "css", "input", false, "dataTest")));

        Assert.assertTrue(source.startsWith("package pages.generated;"),
                "generated code must land in the machine-produced package, never beside reviewed code");
        Assert.assertTrue(source.contains("import base.BasePage;"));
        Assert.assertTrue(source.contains("public class LoginPage extends BasePage {"));
        Assert.assertTrue(source.contains("public LoginPage(WebDriver driver) {"));
        Assert.assertTrue(source.contains("super(driver);"));
    }

    @Test
    public void generationIsDeterministicForIdenticalInput() {
        List<WebElementSelector> selectors = List.of(
                selector("Username", "[data-test='username']", "css", "input", false, "dataTest"),
                selector("Login", "[data-test='login']", "css", "button", true, "dataTest"));

        Assert.assertEquals(generator.generateClassSource("P", selectors),
                generator.generateClassSource("P", selectors),
                "NFR-1: identical input must yield identical output");
    }

    // ---------------------------------------------------------------------------
    // Method selection per element category.
    // ---------------------------------------------------------------------------

    @Test
    public void inputsGetATypeMethodAndButtonsGetAClickMethod() {
        String source = generator.generateClassSource("P", List.of(
                selector("Username", "[data-test='username']", "css", "input", false, "dataTest"),
                selector("Login", "[data-test='login']", "css", "button", true, "dataTest")));

        Assert.assertTrue(source.contains("public void typeUsername(String value) {"));
        Assert.assertTrue(source.contains("public void clickLogin() {"));
        Assert.assertFalse(source.contains("public void typeLogin"));
    }

    @Test
    public void aTextAccessorIsEmittedOnlyForLinksThatActuallyCarryText() {
        String withText = generator.generateClassSource("P", List.of(
                selector("Cart", "[data-test='cart']", "css", "a", true, "dataTest")));
        String withoutText = generator.generateClassSource("P", List.of(
                selector("Logo", "[data-test='logo']", "css", "a", false, "dataTest")));

        Assert.assertTrue(withText.contains("public String getTextCart()"));
        Assert.assertFalse(withoutText.contains("getTextLogo"),
                "an image-only link has no text to read; emitting an accessor for it is noise");
    }

    // ---------------------------------------------------------------------------
    // The text fallback must match the element's own tag.
    // ---------------------------------------------------------------------------

    @Test
    public void textFallbackUsesTheElementsOwnTagNotAHardcodedAnchor() {
        String source = generator.generateClassSource("P", List.of(
                selector("ContinueShopping", "Continue Shopping", "xpath", "button", true, "text")));

        Assert.assertTrue(source.contains("By.xpath(\"//button[contains(normalize-space(.), 'Continue Shopping')]\")"),
                "a text-matched <button> emitted as //a[...] is valid syntax that can never match");
    }

    // ---------------------------------------------------------------------------
    // Regression: page text is arbitrary and ends up inside both a Java string literal
    // and an XPath expression. Neither language has been escaped for, historically.
    // ---------------------------------------------------------------------------

    @Test
    public void apostropheInPageTextDoesNotProduceABrokenXPathLiteral() {
        String source = generator.generateClassSource("P", List.of(
                selector("WomensDresses", "Women's Dresses", "xpath", "a", true, "text")));

        Assert.assertFalse(source.contains("'Women's Dresses'"),
                "a bare apostrophe terminates the XPath string literal and the browser rejects the locator");
        Assert.assertTrue(source.contains("\\\"Women's Dresses\\\""),
                "the XPath literal should switch to double quotes: " + source);
    }

    @Test
    public void doubleQuoteInPageTextIsEscapedForTheJavaLiteral() {
        String source = generator.generateClassSource("P", List.of(
                selector("Monitor", "12\" Monitor", "xpath", "a", true, "text")));

        Assert.assertTrue(source.contains("\\\""), "an unescaped quote closes the Java string and breaks the build");
        Assert.assertFalse(source.contains("(.), '12\" Monitor')"),
                "the raw quote must not reach the emitted source unescaped");
    }

    @Test
    public void textContainingBothQuoteKindsFallsBackToXPathConcat() {
        String source = generator.generateClassSource("P", List.of(
                selector("Odd", "12\" Women's", "xpath", "a", true, "text")));

        Assert.assertTrue(source.contains("concat("),
                "XPath 1.0 has no escape mechanism; concat() is the only portable construction");
    }

    // ---------------------------------------------------------------------------
    // Repeated components.
    // ---------------------------------------------------------------------------

    private RepeatedComponentGroup group(String cardSelector, String parentSelector,
                                         List<ExtractedElement> instance, int repeats) {
        List<List<ExtractedElement>> instances = new java.util.ArrayList<>();
        for (int i = 0; i < repeats; i++) instances.add(instance);
        return new RepeatedComponentGroup(cardSelector, instances, parentSelector);
    }

    @Test
    public void aRepeatedCardBecomesOneNestedClassWithAListAccessor() {
        RepeatedComponentGroup productCard = group("div.inventory_item", "div.inventory_list",
                List.of(new ExtractedElement("button", "button", "", "", "", "Add to cart", "btn btn_primary")), 6);

        String source = generator.generateClassSource("P", List.of(), List.of(productCard));

        Assert.assertTrue(source.contains("public class InventoryItemItem {"));
        Assert.assertTrue(source.contains("public List<InventoryItemItem> getInventoryItemItemList()"));
        Assert.assertTrue(source.contains("By.cssSelector(\"div.inventory_list > div.inventory_item\")"),
                "the runtime selector must be scoped to the parent the cards were detected under");
        Assert.assertTrue(source.contains("public void clickAddToCart() {"));
        Assert.assertTrue(source.contains("root.findElement(By.cssSelector(\"button.btn\"))"));
    }

    /**
     * Regression for the highest-severity defect found in Pipeline A's own output.
     *
     * jsoup's Element.select() is self-inclusive; Selenium's WebElement.findElement() searches
     * descendants only. When the repeating card IS the interactive element — a menu of
     * &lt;a class="bm-item"&gt; — extraction re-captures the card itself, and the emitted
     * root.findElement("a.bm-item") searches inside the anchor for another anchor. It matches
     * nothing, on every instance, and every call throws NoSuchElementException.
     */
    @Test
    public void whenTheCardIsItselfTheInteractiveElementTheRootIsUsedDirectly() {
        RepeatedComponentGroup menuItems = group("a.bm-item", "nav.bm-item-list",
                List.of(new ExtractedElement("a", "link", "", "", "", "All Items", "bm-item menu-item")), 4);

        String source = generator.generateClassSource("P", List.of(), List.of(menuItems));

        Assert.assertTrue(source.contains("WebElement el = root;"),
                "the root element must be acted on directly: " + source);
        Assert.assertFalse(source.contains("root.findElement(By.cssSelector(\"a.bm-item\"))"),
                "searching a card for its own selector can never match");
    }

    @Test
    public void rolesSharingASelectorFragmentFallBackToATextMatchWhenThatTextIsInvariant() {
        // Two unclassed <a> in one card cannot be told apart by tag+class alone. Here both
        // labels are the same in every card, so the text identifies the role, not the card.
        RepeatedComponentGroup card = group("div.card", "div.grid", List.of(
                new ExtractedElement("a", "link", "", "", "", "View Product", ""),
                new ExtractedElement("a", "link", "", "", "", "Add to Wishlist", "")), 3);

        String source = generator.generateClassSource("P", List.of(), List.of(card));

        Assert.assertTrue(source.contains("contains(normalize-space(.), 'View Product')"));
        Assert.assertTrue(source.contains("contains(normalize-space(.), 'Add to Wishlist')"));
    }

    /**
     * Regression for the defect Pipeline A produced in its own output during the evaluation.
     *
     * A product-title link's text is data, not a role: it differs in every card. Naming the
     * method and its locator from the first card's text gave clickSauceLabsBackpack(), which
     * worked on card 1 and threw NoSuchElementException on all the others. Text may name a
     * role only when it is invariant across every detected instance.
     */
    @Test
    public void textThatVariesBetweenInstancesNamesNeitherTheMethodNorTheLocator() {
        List<List<ExtractedElement>> instances = List.of(
                List.of(new ExtractedElement("a", "link", "", "", "", "Sauce Labs Backpack", ""),
                        new ExtractedElement("button", "button", "", "", "", "Add to cart", "btn")),
                List.of(new ExtractedElement("a", "link", "", "", "", "Sauce Labs Bike Light", ""),
                        new ExtractedElement("button", "button", "", "", "", "Add to cart", "btn")),
                List.of(new ExtractedElement("a", "link", "", "", "", "Sauce Labs Onesie", ""),
                        new ExtractedElement("button", "button", "", "", "", "Add to cart", "btn")));
        RepeatedComponentGroup products =
                new RepeatedComponentGroup("div.inventory_item", instances, "div.inventory_list");

        String source = generator.generateClassSource("P", List.of(), List.of(products));

        Assert.assertFalse(source.contains("clickSauceLabsBackpack"),
                "a role must not be named after one instance's data: " + source);
        Assert.assertFalse(source.contains("Sauce Labs Backpack"),
                "and the locator must not match on it either");
        Assert.assertTrue(source.contains("public void clickAddToCart() {"),
                "\"Add to cart\" is invariant across every card, so it IS a role");
    }

    @Test
    public void aRoleWhoseTextIsInvariantStillNamesTheMethod() {
        List<List<ExtractedElement>> instances = List.of(
                List.of(new ExtractedElement("a", "link", "", "", "", "View Product", "view")),
                List.of(new ExtractedElement("a", "link", "", "", "", "View Product", "view")),
                List.of(new ExtractedElement("a", "link", "", "", "", "View Product", "view")));
        RepeatedComponentGroup card = new RepeatedComponentGroup("div.card", instances, "div.grid");

        String source = generator.generateClassSource("P", List.of(), List.of(card));

        Assert.assertTrue(source.contains("public void clickViewProduct() {"));
    }

    @Test
    public void identicalRolesInOneCardCollapseToASingleMethod() {
        // A visible control and its hover-overlay clone are the same role.
        RepeatedComponentGroup card = group("div.card", "div.grid", List.of(
                new ExtractedElement("a", "link", "", "", "", "Add to cart", "add"),
                new ExtractedElement("a", "link", "", "", "", "Add to cart", "add")), 3);

        String source = generator.generateClassSource("P", List.of(), List.of(card));

        int occurrences = source.split("public void clickAddToCart\\(\\)", -1).length - 1;
        Assert.assertEquals(occurrences, 1, "duplicate roles must collapse, or the class will not compile");
    }

    @Test
    public void twoPatternsSharingATagAndClassGetDistinctClassNames() {
        RepeatedComponentGroup first = group("div.col-sm-4", "div.features_items",
                List.of(new ExtractedElement("a", "link", "", "", "", "Add", "add")), 3);
        RepeatedComponentGroup second = group("div.col-sm-4", "div.recommended_items",
                List.of(new ExtractedElement("a", "link", "", "", "", "Add", "add")), 3);

        String source = generator.generateClassSource("P", List.of(), List.of(first, second));

        Assert.assertTrue(source.contains("public class ColSm4Item {"));
        Assert.assertTrue(source.contains("public class ColSm4Item2 {"),
                "two groups with the same tag+class would otherwise emit the same class name twice");
    }

    @Test
    public void everyComponentExposesAnEscapeHatchToItsRootElement() {
        RepeatedComponentGroup card = group("div.card", "div.grid",
                List.of(new ExtractedElement("a", "link", "", "", "", "Add", "add")), 3);

        String source = generator.generateClassSource("P", List.of(), List.of(card));

        Assert.assertTrue(source.contains("public WebElement getRoot() {"),
                "the validation test hovers a card before clicking, which needs the root element");
    }
}
