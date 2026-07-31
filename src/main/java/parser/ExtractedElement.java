package parser;

public class ExtractedElement {

    private String tagName;
    private String type;
    private String dataTest;
    private String id;
    private String name;
    private String text;

    // Used only by repeated-component detection, to build a selector for this element
    // relative to its card's root WebElement (e.g. "a.add-to-cart").
    private String cssClass;

    public ExtractedElement(String tagName, String type, String dataTest, String id, String name, String text) {
        this(tagName, type, dataTest, id, name, text, "");
    }

    public ExtractedElement(String tagName, String type, String dataTest, String id, String name, String text,
                            String cssClass) {
        this.tagName = tagName;
        this.type = type;
        this.dataTest = dataTest;
        this.id = id;
        this.name = name;
        this.text = text;
        this.cssClass = cssClass;
    }

    public String getTagName() { return tagName; }
    public String getType() { return type; }
    public String getDataTest() { return dataTest; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getText() { return text; }
    public String getCssClass() { return cssClass; }

    @Override
    public String toString() {
        return String.format("<%s> type=%s data-test=%s id=%s name=%s text=\"%s\"",
                tagName, type, dataTest, id, name, text);
    }
}
