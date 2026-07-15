package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CheckoutStepTwoPage extends BasePage {

    private By inventoryItemLocator = By.cssSelector("[data-test='inventory-item']");
    private By finishBtn = By.cssSelector("[data-test='finish']");
    private By checkoutStepTwoContainer=By.id("checkout_summary_container");
    public CheckoutStepTwoPage(WebDriver driver){
        super(driver);
    }

    public int getProductsInOverViewCount(){
        return countElements(inventoryItemLocator);
    }
    public boolean isCheckoutStepTwoPageDisplayed(){
        return isDisplayed(checkoutStepTwoContainer);
    }
    public void clickContinue(){
        click(finishBtn);
    }


}
