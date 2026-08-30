package parser;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Unit tests for extraction, sectioning and repeated-component detection.
 *
 * The fixtures are small hand-written HTML strings rather than saved pages, so each test
 * isolates one rule. They run offline: no browser, no network, no live site.
 */
public class HtmlParserTest {

    private final HtmlParser parser = new HtmlParser();

    private PageSection sectionNamed(List<PageSection> sections, String name) {
        return sections.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }

    private ExtractedElement firstWithText(List<ExtractedElement> elements, String text) {
        return elements.stream().filter(e -> text.equals(e.getText())).findFirst().orElse(null);
    }

    private int countOfTag(List<ExtractedElement> elements, String tagName) {
        return (int) elements.stream().filter(e -> e.getTagName().equals(tagName)).count();
    }

    // ---------------------------------------------------------------------------
    // Extraction.
    // ---------------------------------------------------------------------------

    @Test
    public void extractsInputsButtonsAndLinks() {
        List<ExtractedElement> elements = parser.extractInteractiveElements(
                "<html><body>"
                        + "<input type='text' id='user'>"
                        + "<button id='go'>Go</button>"
                        + "<a href='/x'>Next</a>"
                        + "<span>not interactive</span>"
                        + "</body></html>");

        Assert.assertEquals(elements.size(), 3);
        Assert.assertEquals(countOfTag(elements, "input"), 1);
        Assert.assertEquals(countOfTag(elements, "button"), 1);
        Assert.assertEquals(countOfTag(elements, "a"), 1);
    }

    @Test
    public void hiddenInputsAreExcluded() {
        // Real sites emit one hidden CSRF token per form. These are server-issued security
        // values that no user ever interacts with; extracting them produced genuine duplicates.
        List<ExtractedElement> elements = parser.extractInteractiveElements(
                "<html><body><form>"
                        + "<input type='hidden' name='csrfmiddlewaretoken' value='abc'>"
                        + "<input type='email' name='email'>"
                        + "</form></body></html>");

        Assert.assertEquals(elements.size(), 1);
        Assert.assertEquals(elements.get(0).getName(), "email");
    }

    // ---------------------------------------------------------------------------
    // Regression: the automation-attribute NAME must travel with its value, or a
    // data-qa site silently produces unmatchable [data-test='...'] locators.
    // ---------------------------------------------------------------------------

    @Test
    public void theMatchedAutomationAttributeNameIsRecorded() {
        List<ExtractedElement> elements = parser.extractInteractiveElements(
                "<html><body>"
                        + "<input type='email' data-qa='login-email'>"
                        + "<button data-test='login-button'>Login</button>"
                        + "</body></html>");

        ExtractedElement email = elements.stream()
                .filter(e -> "login-email".equals(e.getDataTest())).findFirst().orElse(null);
        ExtractedElement login = elements.stream()
                .filter(e -> "login-button".equals(e.getDataTest())).findFirst().orElse(null);

        Assert.assertNotNull(email, "the data-qa element should have been extracted");
        Assert.assertNotNull(login, "the data-test element should have been extracted");
        Assert.assertEquals(email.getAutomationAttributeName(), "data-qa");
        Assert.assertEquals(login.getAutomationAttributeName(), "data-test");
    }

    @Test
    public void anElementWithNoAutomationAttributeReportsNoValue() {
        List<ExtractedElement> elements = parser.extractInteractiveElements(
                "<html><body><button id='plain'>Plain</button></body></html>");

        Assert.assertEquals(elements.get(0).getDataTest(), "");
    }

    // ---------------------------------------------------------------------------
    // Sectioning.
    // ---------------------------------------------------------------------------

    @Test
    public void semanticRegionsAreSplitAndTheirElementsDoNotLeakIntoTheBody() {
        List<PageSection> sections = parser.extractSections(
                "<html><body>"
                        + "<header><a href='/'>Home</a></header>"
                        + "<main><button id='buy'>Buy</button></main>"
                        + "<footer><a href='/tw'>Twitter</a></footer>"
                        + "</body></html>");

        PageSection header = sectionNamed(sections, "Header");
        PageSection footer = sectionNamed(sections, "Footer");
        PageSection body = sectionNamed(sections, "Body");

        Assert.assertNotNull(header);
        Assert.assertNotNull(footer);
        Assert.assertNotNull(body);
        Assert.assertNotNull(firstWithText(header.getElements(), "Home"));
        Assert.assertNotNull(firstWithText(footer.getElements(), "Twitter"));
        Assert.assertNull(firstWithText(body.getElements(), "Home"),
                "the navigation must be generated once, not redeclared in every page object");
        Assert.assertNull(firstWithText(body.getElements(), "Twitter"));
        Assert.assertEquals(body.getElements().size(), 1);
    }

    @Test
    public void ariaLandmarkRolesAreUsedWhenSemanticTagsAreAbsent() {
        List<PageSection> sections = parser.extractSections(
                "<html><body>"
                        + "<div role='banner'><a href='/'>Home</a></div>"
                        + "<div><button id='buy'>Buy</button></div>"
                        + "</body></html>");

        PageSection header = sectionNamed(sections, "Header");
        Assert.assertNotNull(header, "semantic markup first, accessibility metadata second");
        Assert.assertNotNull(firstWithText(header.getElements(), "Home"));
    }

    // ---------------------------------------------------------------------------
    // Repeated-component detection.
    // ---------------------------------------------------------------------------

    private String grid(int cards) {
        StringBuilder sb = new StringBuilder("<html><body><div class='inventory_list'>");
        for (int i = 0; i < cards; i++) {
            sb.append("<div class='inventory_item'>")
              .append("<a href='/i").append(i).append("'>Item ").append(i).append("</a>")
              .append("<button class='btn'>Add to cart</button>")
              .append("</div>");
        }
        return sb.append("</div></body></html>").toString();
    }

    @Test
    public void threeOrMoreSiblingsUnderOneParentAreAPattern() {
        PageSection body = sectionNamed(parser.extractSections(grid(3)), "Body");

        Assert.assertEquals(body.getComponentGroups().size(), 1);
        RepeatedComponentGroup group = body.getComponentGroups().get(0);
        Assert.assertEquals(group.getContainerPath(), "div.inventory_item");
        Assert.assertEquals(group.getRepeatCount(), 3);
        Assert.assertEquals(group.getScopedSelector(), "div.inventory_list > div.inventory_item");
    }

    @Test
    public void twoSimilarSiblingsAreACoincidenceNotAStructure() {
        PageSection body = sectionNamed(parser.extractSections(grid(2)), "Body");

        Assert.assertTrue(body.getComponentGroups().isEmpty(),
                "two is a coincidence; three is a structure");
    }

    @Test
    public void detectedCardsAreStrippedSoTheirElementsAreNotAlsoExtractedFlat() {
        PageSection body = sectionNamed(parser.extractSections(grid(6)), "Body");

        Assert.assertEquals(body.getComponentGroups().size(), 1);
        Assert.assertEquals(body.getComponentGroups().get(0).getRepeatCount(), 6);
        Assert.assertTrue(body.getElements().isEmpty(),
                "a product grid must not also produce six near-identical flat fields");
    }

    @Test
    public void onlyTheOutermostBoundaryOfANestedPatternIsKept() {
        // Each card contains three links, so the links qualify as a repeating pattern in
        // their own right, inside a card that also repeats. Both boundaries are candidates.
        StringBuilder sb = new StringBuilder("<html><body><div class='features_items'>");
        for (int i = 0; i < 4; i++) {
            sb.append("<div class='col-sm-4'>")
              .append("<a class='opt' href='/a'>One</a>")
              .append("<a class='opt' href='/b'>Two</a>")
              .append("<a class='opt' href='/c'>Three</a>")
              .append("</div>");
        }
        sb.append("</div></body></html>");

        PageSection body = sectionNamed(parser.extractSections(sb.toString()), "Body");

        Assert.assertEquals(body.getComponentGroups().size(), 1,
                "the outer element is the real card and already contains everything the inner one holds");
        Assert.assertEquals(body.getComponentGroups().get(0).getContainerPath(), "div.col-sm-4");
    }

    @Test
    public void aSharedClassNameAcrossUnrelatedParentsIsNotAPattern() {
        // Framework utility classes such as "row" recur across entirely unrelated regions.
        // Grouping by class alone would call that a repeated component; grouping by
        // (parent, tag, first class) does not.
        String html = "<html><body>"
                + "<div class='a'><div class='row'><button class='btn'>One</button></div></div>"
                + "<div class='b'><div class='row'><button class='btn'>Two</button></div></div>"
                + "<div class='c'><div class='row'><button class='btn'>Three</button></div></div>"
                + "</body></html>";

        PageSection body = sectionNamed(parser.extractSections(html), "Body");

        Assert.assertTrue(body.getComponentGroups().isEmpty(),
                "what makes something a card is a shared parent, not a shared class name");
        Assert.assertEquals(body.getElements().size(), 3, "the three buttons remain as flat elements");
    }
}
