package ui.shoppingtests;

import base.BaseTest;
import flows.AddToCartFlow;
import flows.LoginFlow;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.InventoryPage;
import utils.ConfigReader;

public class AddingItemsToCartTest extends BaseTest {

    @Test
    public void addingItemsToCartTest(){

        LoginFlow loginFlow = new LoginFlow(driver);
        AddToCartFlow addToCartFlow = new AddToCartFlow(driver);
        InventoryPage inventoryPage = loginFlow.loginAsValidUser(ConfigReader.get("valid.username"), ConfigReader.get("valid.password"));
        CartPage cartPage = addToCartFlow.addToCart();
        Assert.assertTrue(cartPage.getProductsInCartCount()>0,"Cart is empty");
        Assert.assertTrue(cartPage.isCartDisplayed(),"Cart is not displayed");
    }
}
