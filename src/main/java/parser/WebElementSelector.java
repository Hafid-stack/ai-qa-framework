package parser;

public class WebElementSelector {
    private String selectorName;
    private String selectorValue;
    private String selectorType;
    private String elementCategory;
    private boolean hasVisibleText;

    public WebElementSelector(String selectorName, String selectorValue, String selectorType,
                              String elementCategory, boolean hasVisibleText) {
        this.selectorName = selectorName;
        this.selectorValue = selectorValue;
        this.selectorType = selectorType;
        this.elementCategory = elementCategory;
        this.hasVisibleText = hasVisibleText;
    }

    public String getSelectorName() { return selectorName; }
    public String getSelectorValue() { return selectorValue; }
    public String getSelectorType() { return selectorType; }
    public String getElementCategory() { return elementCategory; }
    public boolean hasVisibleText() { return hasVisibleText; }
}