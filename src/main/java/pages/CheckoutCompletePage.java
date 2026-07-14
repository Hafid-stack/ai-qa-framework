package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutCompletePage extends BasePage {

    private final By thankYouMessage= By.cssSelector("[data-test='complete-header']");
    private final By backHomeBtn= By.cssSelector("[data-test='back-to-products']");
    public CheckoutCompletePage(WebDriver driver){
        super(driver);
    }

    public void clickBackHomeBtn(){
        click(backHomeBtn);
    }
    public String getThankYouMessage(){
        return getText(thankYouMessage);
    }
}
