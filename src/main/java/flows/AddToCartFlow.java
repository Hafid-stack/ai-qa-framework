package flows;

import base.BaseFlow;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.*;
import utils.CustomerDetail;

public class AddToCartFlow extends BaseFlow {
    private CheckoutCompletePage checkoutCompletePage;
    private CheckoutStepOnePage checkoutStepOnePage;
    private CheckoutStepTwoPage checkoutStepTwoPage;
    private InventoryPage inventoryPage;
    private CartPage cartPage;
    public AddToCartFlow(WebDriver driver) {
        super(driver);
        inventoryPage = new InventoryPage(driver);
        cartPage = new CartPage(driver);
        checkoutStepOnePage = new CheckoutStepOnePage(driver);
        checkoutStepTwoPage = new CheckoutStepTwoPage(driver);
        checkoutCompletePage = new CheckoutCompletePage(driver);
    }

    public CartPage addToCart(){
        inventoryPage.addRandomProductToCart();

        inventoryPage.clickCartButton();
        return cartPage;
    }
    public CheckoutCompletePage fillCustomerDetailAndContinue(CustomerDetail customerDetail){
        cartPage.clickCheckoutBtn();
        checkoutStepOnePage.addCustomerDetails(customerDetail);
        checkoutStepOnePage.clickContinue();
        checkoutStepTwoPage.clickContinue();

        return checkoutCompletePage;

    }
    public CheckoutStepTwoPage fillCustomerDetailAndContinueToCheckout(CustomerDetail customerDetail){
        inventoryPage.addRandomProductToCart();
        inventoryPage.clickCartButton();
        cartPage.clickCheckoutBtn();

        checkoutStepOnePage.addCustomerDetails(customerDetail);
        checkoutStepOnePage.clickContinue();
        return checkoutStepTwoPage;
    }
}
