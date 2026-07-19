package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.ConfigReader;

import java.util.ArrayList;
import java.util.List;

public class HtmlParser {

    public List<ExtractedElement> extractInteractiveElements(String html) {
        Document doc = Jsoup.parse(html);
        List<ExtractedElement> elements = new ArrayList<>();

        Elements inputs = doc.select("input");
        for (Element input : inputs) {
            if (input.attr("type").equalsIgnoreCase("hidden")) {
                continue; // Hidden fields (like CSRF tokens) aren't real interactive elements — skip them
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

        Elements buttons = doc.select("button");
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

        Elements links = doc.select("a");
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

    // Checks each known automation-friendly attribute (data-test, data-qa, etc.)
    // in priority order, from config.properties, and returns the first one found.
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
}