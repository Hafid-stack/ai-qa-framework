package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlParser {

    public List<ExtractedElement> extractInteractiveElements(String html) {
        Document doc = Jsoup.parse(html);
        List<ExtractedElement> elements = new ArrayList<>();

        // Inputs (text fields, passwords, submit buttons, etc.)
        Elements inputs = doc.select("input");
        for (Element input : inputs) {
            elements.add(new ExtractedElement(
                    "input",
                    input.attr("type"),
                    input.attr("data-test"),
                    input.attr("id"),
                    input.attr("name"),
                    input.attr("value")
            ));
        }

        // Buttons
        Elements buttons = doc.select("button");
        for (Element button : buttons) {
            elements.add(new ExtractedElement(
                    "button",
                    "button",
                    button.attr("data-test"),
                    button.attr("id"),
                    button.attr("name"),
                    button.text()
            ));
        }

        // Links
        Elements links = doc.select("a");
        for (Element link : links) {
            elements.add(new ExtractedElement(
                    "a",
                    "link",
                    link.attr("data-test"),
                    link.attr("id"),
                    link.attr("name"),
                    link.text()
            ));
        }

        return elements;
    }
}