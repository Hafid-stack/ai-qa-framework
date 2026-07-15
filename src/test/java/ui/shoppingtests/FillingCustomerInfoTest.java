package ui.shoppingtests;

import base.BaseTest;
import flows.AddToCartFlow;
import flows.LoginFlow;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.CheckoutStepTwoPage;
import pages.InventoryPage;
import utils.ConfigReader;
import utils.Generator;

public class FillingCustomerInfoTest extends BaseTest {

    @Test
    public void fillingCustomerInfoTest() {


        LoginFlow loginFlow = new LoginFlow(driver);
        AddToCartFlow addToCartFlow = new AddToCartFlow(driver);
        InventoryPage inventoryPage = loginFlow.loginAsValidUser(ConfigReader.get("valid.username"), ConfigReader.get("valid.password"));
        // Thread.sleep(3000);
        Generator generator = new Generator();
        CheckoutStepTwoPage checkoutStepTwoPage = addToCartFlow.fillCustomerDetailAndContinueToCheckout(generator.getRandomCustomerDetail());
        //Thread.sleep(3000);

        Assert.assertTrue(checkoutStepTwoPage.isCheckoutStepTwoPageDisplayed(), "Checkout Step Two Page is not Displayed");
        Assert.assertTrue(checkoutStepTwoPage.getProductsInOverViewCount()>0,"Products in Over View Count is 0");
    }
}
