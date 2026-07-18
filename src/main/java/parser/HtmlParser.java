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

        Elements inputs = doc.select("input");
        System.out.println("Inputs found: " + inputs.size());
        for (Element input : inputs) {
            elements.add(new ExtractedElement(
                    "input", input.attr("type"), input.attr("data-test"),
                    input.attr("id"), input.attr("name"), input.attr("value")
            ));
        }

        Elements buttons = doc.select("button");
        System.out.println("Buttons found: " + buttons.size());
        for (Element button : buttons) {
            elements.add(new ExtractedElement(
                    "button", "button", button.attr("data-test"),
                    button.attr("id"), button.attr("name"), button.text()
            ));
        }

        Elements links = doc.select("a");
        System.out.println("Links found: " + links.size());
        for (Element link : links) {
            elements.add(new ExtractedElement(
                    "a", "link", link.attr("data-test"),
                    link.attr("id"), link.attr("name"), link.text()
            ));
        }

        System.out.println("Total elements: " + elements.size());
        return elements;
    }
}