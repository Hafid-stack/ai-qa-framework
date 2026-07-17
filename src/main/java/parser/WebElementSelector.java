package parser;

public class WebElementSelector {
    private String selectorName;
    private String selectorValue;
    private String selectorType; // "css" or "linkText"

    public WebElementSelector(String selectorName, String selectorValue, String selectorType) {
        this.selectorName = selectorName;
        this.selectorValue = selectorValue;
        this.selectorType = selectorType;
    }

    public String getSelectorName() { return selectorName; }
    public String getSelectorValue() { return selectorValue; }
    public String getSelectorType() { return selectorType; }
}