package ui.logintests;

import base.BaseTest;
import flows.LoginFlow;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.InventoryPage;
import utils.ConfigReader;

public class LoginUsingValidCredentialsTest extends BaseTest {

    @Test
    public void loginUsingValidCredentialsTest() {
        LoginFlow loginFlow = new LoginFlow(driver);
        InventoryPage inventoryPage = loginFlow.loginAsValidUser(ConfigReader.get("valid.username"), ConfigReader.get("valid.password"));

        Assert.assertTrue(driver.getCurrentUrl().contains(ConfigReader.get("inventory.url")),"Url does not contain the " + ConfigReader.get("inventory.url"));
        Assert.assertTrue(inventoryPage.isLoaded(),"Inventory Page is not loaded");
    }
}
