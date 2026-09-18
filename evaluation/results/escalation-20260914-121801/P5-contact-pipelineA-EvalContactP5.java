package pages.generated;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import java.util.List;
import java.util.stream.Collectors;

public class EvalContactP5 extends BasePage {

    private final By byFeedbackAutomationexerciseCom = By.xpath("//a[contains(normalize-space(.), 'feedback@automationexercise.com')]");
    private final By byScrollUp = By.cssSelector("[id='scrollUp']");

    public EvalContactP5(WebDriver driver) {
        super(driver);
    }

    public void clickFeedbackAutomationexerciseCom() {
        click(byFeedbackAutomationexerciseCom);
    }

    public String getTextFeedbackAutomationexerciseCom() {
        return getText(byFeedbackAutomationexerciseCom);
    }

    public void clickScrollUp() {
        click(byScrollUp);
    }

    // "div.form-group" repeated 5 times on the page this was generated from.
    public List<FormGroupItem> getFormGroupItemList() {
        return driver.findElements(By.cssSelector("form.contact-form > div.form-group")).stream().map(FormGroupItem::new).collect(Collectors.toList());
    }

    // One instance of the repeated "div.form-group" pattern.
    // Usage: getFormGroupItemList().get(0).clickText();
    public class FormGroupItem {
        private final WebElement root;

        public FormGroupItem(WebElement root) {
            this.root = root;
        }

        public WebElement getRoot() {
            return root;
        }

        public void clickText() {
            WebElement el = root.findElement(By.cssSelector("input.form-control"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

    }

}
