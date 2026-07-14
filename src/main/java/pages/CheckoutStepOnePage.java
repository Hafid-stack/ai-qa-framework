package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.CustomerDetail;

public class CheckoutStepOnePage extends BasePage {
    private final By continueBtn= By.cssSelector("[data-test='continue']");
    private final By firstNameField = By.cssSelector("[data-test='firstName']");
    private final By lastNameField = By.cssSelector("[data-test='lastName']");
    private final By zipCodeField = By.cssSelector("[data-test='postalCode']");
    private final By checkoutContainer=By.id("checkout_info_container");
    public CheckoutStepOnePage(WebDriver driver){
        super(driver);
    }

    public boolean isCheckoutFormDisplayed(){
        return isDisplayed(checkoutContainer) && isDisplayed(firstNameField);
    }

    public void addCustomerDetails(CustomerDetail customerDetail){

        type(firstNameField, customerDetail.getFirstName());
        type(lastNameField, customerDetail.getLastName());
        type(zipCodeField, customerDetail.getZipCode());
    }
    public void clickContinue(){
        click(continueBtn);
    }
}
