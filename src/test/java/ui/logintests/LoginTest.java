package ui.logintests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;
import utils.ConfigReader;

public class LoginTest extends BaseTest {
    //Might turn it into a flow in the future or create a new one (add to the to do list)

    @Test(description = "Test case N1, Login User with correct credentials")
    public void loginTest(){
        LoginPage loginPage = new LoginPage(driver);
        //Opening the Urls
        loginPage.open();

        //Url assertion (might need to add page title as well for 100% certainty)
        Assert.assertEquals(driver.getCurrentUrl(), ConfigReader.get("loginUrl"), "Base Url does not match");

        //type username and password
        loginPage.typeUsername(ConfigReader.get("valid.username"));
        loginPage.typePassword(ConfigReader.get("valid.password"));

        //click login
        loginPage.clickLogin();

        //Assert login is valid: URL changed to inventory page AND the products container is actually visible
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "URL did not redirect to inventory page after login");

        InventoryPage inventoryPage = new InventoryPage(driver);
        Assert.assertTrue(inventoryPage.isLoaded(), "Inventory page container not visible after login");
        //Logs will be removed later on
        log("Test case N1, Login User with correct credentials, Passed");
    }
}