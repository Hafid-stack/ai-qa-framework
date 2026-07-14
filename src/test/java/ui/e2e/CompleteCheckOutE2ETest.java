package ui.e2e;

import base.BaseTest;
import flows.AddToCartFlow;
import flows.LoginFlow;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.CheckoutCompletePage;
import pages.CheckoutStepTwoPage;
import pages.InventoryPage;
import utils.ConfigReader;
import utils.Generator;

public class CompleteCheckOutE2ETest extends BaseTest {
    @Test
    public void getThankYouMessage(){

        LoginFlow loginFlow = new LoginFlow(driver);
        AddToCartFlow addToCartFlow = new AddToCartFlow(driver);
        InventoryPage inventoryPage = loginFlow.loginAsValidUser(ConfigReader.get("valid.username"), ConfigReader.get("valid.password"));
        CartPage cartPage = addToCartFlow.addToCart();
        Generator generator = new Generator();

        CheckoutCompletePage checkoutCompletePage=addToCartFlow.fillCustomerDetailAndContinue(generator.getRandomCustomerDetail());


        Assert.assertEquals(ConfigReader.get("thank.you.message"),checkoutCompletePage.getThankYouMessage(),"checkout imcomplete");

    }
}
