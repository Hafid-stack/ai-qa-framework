package parser;

public class WebElementSelector {
    private String selectorName;
    private String selectorValue;
    private String selectorType;
    private String elementCategory;
    private boolean hasVisibleText;
    private String matchedVia; // "dataTest", "id", "name", or "text" — tells us how confident this selector is

    public WebElementSelector(String selectorName, String selectorValue, String selectorType,
                              String elementCategory, boolean hasVisibleText, String matchedVia) {
        this.selectorName = selectorName;
        this.selectorValue = selectorValue;
        this.selectorType = selectorType;
        this.elementCategory = elementCategory;
        this.hasVisibleText = hasVisibleText;
        this.matchedVia = matchedVia;
    }

    public String getSelectorName() { return selectorName; }
    public String getSelectorValue() { return selectorValue; }
    public String getSelectorType() { return selectorType; }
    public String getElementCategory() { return elementCategory; }
    public boolean hasVisibleText() { return hasVisibleText; }
    public String getMatchedVia() { return matchedVia; }

    // Low confidence = the selector fell back to matching on visible text,
    // the least stable option (breaks on copy changes, translations, etc.)
    public boolean isLowConfidence() {
        return "text".equals(matchedVia);
    }
}