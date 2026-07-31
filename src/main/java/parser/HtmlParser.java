package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.ConfigReader;

import java.util.ArrayList;
import java.util.List;


public class HtmlParser {

    // Existing behaviour, unchanged: flat extraction over the whole document.
    public List<ExtractedElement> extractInteractiveElements(String html) {
        Document doc = Jsoup.parse(html);
        return extractFrom(doc);
    }

    // Sectioned extraction: splits the page into semantic regions (header, footer, body)
    // so that generated Page Objects mirror how the page is actually structured,
    // instead of producing one flat class containing every element on the page.
    public List<PageSection> extractSections(String html) {
        Document doc = Jsoup.parse(html);
        List<PageSection> sections = new ArrayList<>();

        Element header = findRegion(doc, "header", "banner");
        Element footer = findRegion(doc, "footer", "contentinfo");
        Element main = findRegion(doc, "main", "main");

        if (header != null) {
            sections.add(new PageSection("Header", extractFrom(header)));
            header.remove();
        }
        if (footer != null) {
            sections.add(new PageSection("Footer", extractFrom(footer)));
            footer.remove();
        }

        // Whatever remains after removing header/footer is the page's own content.
        // Repeated cards (e.g. a product grid) are detected and stripped out of bodyScope
        // FIRST, so the flat extraction below never sees their elements — no separate
        // filtering step needed, and there is no risk of the two extractions disagreeing.
        Element bodyScope = (main != null) ? main : doc;
        RepeatedComponentDetector detector = new RepeatedComponentDetector();
        List<RepeatedComponentGroup> componentGroups = detector.detectAndStrip(bodyScope);

        PageSection body = new PageSection("Body", extractFrom(bodyScope), componentGroups);
        sections.add(body);

        return sections;
    }

    // Looks for a semantic HTML5 tag first, then falls back to the equivalent ARIA landmark role.
    private Element findRegion(Document doc, String tagName, String ariaRole) {
        Element byTag = doc.selectFirst(tagName);
        if (byTag != null) return byTag;
        return doc.selectFirst("[role=" + ariaRole + "]");
    }

    private List<ExtractedElement> extractFrom(Element scope) {
        List<ExtractedElement> elements = new ArrayList<>();

        Elements inputs = scope.select("input");
        for (Element input : inputs) {
            if (input.attr("type").equalsIgnoreCase("hidden")) {
                continue;
            }
            elements.add(new ExtractedElement(
                    "input",
                    input.attr("type"),
                    getAutomationAttribute(input),
                    input.attr("id"),
                    input.attr("name"),
                    input.attr("value")
            ));
        }

        Elements buttons = scope.select("button");
        for (Element button : buttons) {
            elements.add(new ExtractedElement(
                    "button",
                    "button",
                    getAutomationAttribute(button),
                    button.attr("id"),
                    button.attr("name"),
                    button.text()
            ));
        }

        Elements links = scope.select("a");
        for (Element link : links) {
            elements.add(new ExtractedElement(
                    "a",
                    "link",
                    getAutomationAttribute(link),
                    link.attr("id"),
                    link.attr("name"),
                    link.text()
            ));
        }

        return elements;
    }

    private String getAutomationAttribute(Element el) {
        String configuredAttributes = ConfigReader.get("automation.attributes");
        String[] attributeNames = configuredAttributes.split(",");
        for (String attr : attributeNames) {
            String value = el.attr(attr.trim());
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }
    // Same as extractFrom, but also captures each element's own class attribute, needed
    // by RepeatedComponentDetector to build a selector relative to a card's root element.
    // Package-visible: used by RepeatedComponentDetector to extract the interactive
    // elements found inside one candidate "card" root.
    java.util.List<ExtractedElement> extractStructuredFrom(Element scope) {
        List<ExtractedElement> elements = new ArrayList<>();

        Elements links = scope.select("a");
        for (Element link : links) {
            elements.add(buildStructuredElement(link, "a", "link", link.text()));
        }

        Elements buttons = scope.select("button");
        for (Element button : buttons) {
            elements.add(buildStructuredElement(button, "button", "button", button.text()));
        }

        Elements inputs = scope.select("input");
        for (Element input : inputs) {
            if (input.attr("type").equalsIgnoreCase("hidden")) continue;
            elements.add(buildStructuredElement(input, "input", input.attr("type"), input.attr("value")));
        }

        return elements;
    }

    private ExtractedElement buildStructuredElement(Element el, String tagName, String type, String text) {
        return new ExtractedElement(
                tagName, type, getAutomationAttribute(el), el.attr("id"), el.attr("name"), text,
                el.attr("class")
        );
    }
}