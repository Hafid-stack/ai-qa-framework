package parser;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for the locator-selection stage.
 *
 * These run offline against constructed ExtractedElement instances: no browser, no network,
 * no live site. That is deliberate — the pipeline's own correctness must be checkable in CI
 * on every push, independently of whether any target site happens to be reachable.
 */
public class SelectorPriorityFinderTest {

    private final SelectorPriorityFinder finder = new SelectorPriorityFinder();

    private WebElementSelector only(ExtractedElement element) {
        List<WebElementSelector> selectors = finder.getOrder(List.of(element));
        Assert.assertEquals(selectors.size(), 1, "expected exactly one selector for one element");
        return selectors.get(0);
    }

    // ---------------------------------------------------------------------------
    // Priority ordering: dedicated test attribute > id > name > visible text.
    // ---------------------------------------------------------------------------

    @Test
    public void dedicatedTestAttributeWinsOverEverythingElse() {
        WebElementSelector selector = only(new ExtractedElement(
                "button", "button", "login-button", "someId", "someName", "Login", "", "data-test"));

        Assert.assertEquals(selector.getMatchedVia(), "dataTest");
        Assert.assertEquals(selector.getSelectorValue(), "[data-test='login-button']");
        Assert.assertEquals(selector.getSelectorType(), "css");
        Assert.assertFalse(selector.isLowConfidence());
    }

    @Test
    public void idIsUsedWhenNoAutomationAttributeIsPresent() {
        WebElementSelector selector = only(new ExtractedElement(
                "input", "text", "", "user-name", "username", "", "", "data-test"));

        Assert.assertEquals(selector.getMatchedVia(), "id");
        Assert.assertEquals(selector.getSelectorValue(), "[id='user-name']");
    }

    @Test
    public void nameIsUsedWhenThereIsNoAttributeAndNoId() {
        WebElementSelector selector = only(new ExtractedElement(
                "input", "text", "", "", "username", "", "", "data-test"));

        Assert.assertEquals(selector.getMatchedVia(), "name");
        Assert.assertEquals(selector.getSelectorValue(), "[name='username']");
    }

    @Test
    public void visibleTextIsTheLastResortAndIsMarkedLowConfidence() {
        WebElementSelector selector = only(new ExtractedElement(
                "a", "link", "", "", "", "Contact us", "", "data-test"));

        Assert.assertEquals(selector.getMatchedVia(), "text");
        Assert.assertEquals(selector.getSelectorType(), "xpath");
        Assert.assertTrue(selector.isLowConfidence(),
                "a text-matched selector is the escalation trigger; if this stops being "
                        + "low-confidence the whole confidence-gating mechanism silently stops firing");
    }

    @Test
    public void elementWithNoUsableHandleProducesNoSelector() {
        List<WebElementSelector> selectors = finder.getOrder(List.of(
                new ExtractedElement("input", "text", "", "", "", "", "", "data-test")));

        Assert.assertTrue(selectors.isEmpty(),
                "an element with no attribute, id, name or text cannot be addressed at all");
    }

    // ---------------------------------------------------------------------------
    // Regression: the emitted selector must name the attribute that is actually on the
    // element. Emitting [data-test='...'] for an element carrying data-qa produces a
    // locator that is syntactically valid and can never match anything.
    // ---------------------------------------------------------------------------

    @Test
    public void dataQaElementProducesADataQaSelectorNotADataTestOne() {
        WebElementSelector selector = only(new ExtractedElement(
                "input", "email", "login-email", "", "", "", "", "data-qa"));

        Assert.assertEquals(selector.getSelectorValue(), "[data-qa='login-email']");
        Assert.assertFalse(selector.getSelectorValue().contains("data-test"),
                "the attribute name must come from the element, never from a constant");
    }

    @Test
    public void anIdContainingCssMetacharactersUsesTheAttributeForm() {
        // SauceDemo really does ship id="add-to-cart-test.allthethings()-t-shirt-(red)".
        // "#" + that id is not a valid CSS id fragment and the browser rejects it.
        WebElementSelector selector = only(new ExtractedElement(
                "button", "button", "", "add-to-cart-test.allthethings()-t-shirt-(red)", "", "Add to cart",
                "", "data-test"));

        Assert.assertEquals(selector.getSelectorValue(),
                "[id='add-to-cart-test.allthethings()-t-shirt-(red)']");
        Assert.assertFalse(selector.getSelectorValue().startsWith("#"));
    }

    // ---------------------------------------------------------------------------
    // Variable naming.
    // ---------------------------------------------------------------------------

    @Test
    public void variableNameIsAValidJavaIdentifierEvenForHostileProductNames() {
        WebElementSelector selector = only(new ExtractedElement(
                "a", "link", "", "", "", "Test.allTheThings() T-Shirt (Red)", "", "data-test"));

        String name = selector.getSelectorName();
        Assert.assertTrue(Character.isJavaIdentifierStart(name.charAt(0)), "bad start char in: " + name);
        for (char c : name.toCharArray()) {
            Assert.assertTrue(Character.isJavaIdentifierPart(c), "illegal identifier char '" + c + "' in: " + name);
        }
    }

    @Test
    public void namingFollowsTheSamePriorityOrderAsMatching() {
        // Two type="text" inputs on one form used to generate the same variable name.
        WebElementSelector first = only(new ExtractedElement(
                "input", "text", "", "first-name", "", "", "", "data-test"));
        WebElementSelector second = only(new ExtractedElement(
                "input", "text", "", "last-name", "", "", "", "data-test"));

        Assert.assertNotEquals(first.getSelectorName(), second.getSelectorName());
    }

    @Test
    public void nonPrintableCharactersAreStrippedFromTextMatches() {
        // Icon fonts and non-breaking spaces put non-ASCII characters into link text, which
        // defeat an exact match: \uE001 is a private-use icon glyph, \u00A0 a non-breaking space.
        WebElementSelector selector = only(new ExtractedElement(
                "a", "link", "", "", "", "\uE001 Delete\u00A0", "", "data-test"));

        Assert.assertEquals(selector.getSelectorValue(), "Delete");
    }
}
