package parser;

import java.util.ArrayList;
import java.util.List;

public class InputOrder {

    public List<String> getOrder(List<ExtractedElement> elements) {


        List<String> order = new ArrayList<>();
        for (ExtractedElement element : elements) {
            if (element.getDataTest() != null) {
                order.add("[data-test='" + element.getDataTest() + "']");
            } else if (element.getId() != null) {
                order.add("#" + element.getId());
            } else if (element.getName() != null) {
                order.add("[name='" + element.getName() + "']");
            } else if (element.getType() != null) {
                order.add("[type='" + element.getType() + "']");
            } else if (element.getTagName() != null) {
                order.add(element.getTagName());
            } else if (element.getText() != null) {
                order.add("//*[text()='" + element.getText() + "']"); // XPath, not CSS
            }
        }
        return order;
    }
}
