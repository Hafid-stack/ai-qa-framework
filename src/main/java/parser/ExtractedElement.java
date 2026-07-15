package parser;

public class ExtractedElement {

    private String tagName;
    private String type;       // e.g. "text", "password", "submit" (for inputs), or "link", "button"
    private String dataTest;
    private String id;
    private String name;
    private String text;       // visible text, useful for buttons/links

    public ExtractedElement(String tagName, String type, String dataTest, String id, String name, String text) {
        this.tagName = tagName;
        this.type = type;
        this.dataTest = dataTest;
        this.id = id;
        this.name = name;
        this.text = text;
    }

    public String getTagName() { return tagName; }
    public String getType() { return type; }
    public String getDataTest() { return dataTest; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getText() { return text; }

    @Override
    public String toString() {
        return String.format("<%s> type=%s data-test=%s id=%s name=%s text=\"%s\"",
                tagName, type, dataTest, id, name, text);
    }
}
