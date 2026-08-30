package parser;

public class ExtractedElement {

    private String tagName;
    private String type;
    private String dataTest;
    private String id;
    private String name;
    private String text;

    // The NAME of the automation attribute that supplied dataTest, e.g. "data-test" or
    // "data-qa". Recorded separately from the value because the emitted selector has to
    // name the attribute that is actually on the element: a page using data-qa must
    // produce [data-qa='...'], not [data-test='...']. Emitting the wrong attribute name
    // yields a locator that is syntactically valid and can never match anything.
    private String automationAttributeName;

    // Used only by repeated-component detection, to build a selector for this element
    // relative to its card's root WebElement (e.g. "a.add-to-cart").
    private String cssClass;

    public ExtractedElement(String tagName, String type, String dataTest, String id, String name, String text) {
        this(tagName, type, dataTest, id, name, text, "", "data-test");
    }

    public ExtractedElement(String tagName, String type, String dataTest, String id, String name, String text,
                            String cssClass) {
        this(tagName, type, dataTest, id, name, text, cssClass, "data-test");
    }

    public ExtractedElement(String tagName, String type, String dataTest, String id, String name, String text,
                            String cssClass, String automationAttributeName) {
        this.tagName = tagName;
        this.type = type;
        this.dataTest = dataTest;
        this.id = id;
        this.name = name;
        this.text = text;
        this.cssClass = cssClass;
        this.automationAttributeName =
                (automationAttributeName == null || automationAttributeName.isEmpty())
                        ? "data-test" : automationAttributeName;
    }

    public String getTagName() { return tagName; }
    public String getType() { return type; }
    public String getDataTest() { return dataTest; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getText() { return text; }
    public String getCssClass() { return cssClass; }
    public String getAutomationAttributeName() { return automationAttributeName; }

    @Override
    public String toString() {
        return String.format("<%s> type=%s %s=%s id=%s name=%s text=\"%s\"",
                tagName, type, automationAttributeName, dataTest, id, name, text);
    }
}
