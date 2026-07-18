package parser;

public class WebElementSelector {
    private String selectorName;
    private String selectorValue;
    private String selectorType; // "css" or "linkText"
    private String elementCategory; // "input", "button", "link" — drives which methods get generated

    public WebElementSelector(String selectorName, String selectorValue, String selectorType, String elementCategory) {
        this.selectorName = selectorName;
        this.selectorValue = selectorValue;
        this.selectorType = selectorType;
        this.elementCategory = elementCategory;
    }

    public String getSelectorName() { return selectorName; }
    public String getSelectorValue() { return selectorValue; }
    public String getSelectorType() { return selectorType; }
    public String getElementCategory() { return elementCategory; }
}