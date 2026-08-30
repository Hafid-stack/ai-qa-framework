package org.openqa.selenium;
import java.util.List;
/** Faithful stub of org.openqa.selenium.By: exactly the eight public static factory
 *  methods declared by the Selenium 4 Java API (selenium.dev javadoc, By). */
public abstract class By {
    public static By id(String id) { return null; }
    public static By linkText(String linkText) { return null; }
    public static By partialLinkText(String partialLinkText) { return null; }
    public static By name(String name) { return null; }
    public static By tagName(String tagName) { return null; }
    public static By xpath(String xpathExpression) { return null; }
    public static By className(String className) { return null; }
    public static By cssSelector(String cssSelector) { return null; }
    public WebElement findElement(SearchContext context) { return null; }
    public List<WebElement> findElements(SearchContext context) { return null; }
}
