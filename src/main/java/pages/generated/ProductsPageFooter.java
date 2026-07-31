package pages.generated;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductsPageFooter extends BasePage {

    private final By bySusbscribeEmail = By.cssSelector("#susbscribe_email");
    private final By bySubscribe = By.cssSelector("#subscribe");

    public ProductsPageFooter(WebDriver driver) {
        super(driver);
    }

    public void typeSusbscribeEmail(String value) {
        type(bySusbscribeEmail, value);
    }

    public void clickSubscribe() {
        click(bySubscribe);
    }

}
