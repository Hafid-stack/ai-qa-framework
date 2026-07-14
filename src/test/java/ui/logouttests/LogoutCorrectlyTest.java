package ui.logouttests;

import base.BaseTest;
import flows.LoginFlow;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;
import utils.ConfigReader;

public class LogoutCorrectlyTest extends BaseTest {

    @Test
    public void logoutCorrectlyTest()
    {
        LoginFlow loginFlow = new LoginFlow(driver);
        LoginPage loginPage = loginFlow.logoutCorrectly(ConfigReader.get("valid.username"), ConfigReader.get("valid.password"));

        Assert.assertTrue(loginPage.isLoginDisplayed(),"Login page is not Displayed");

    }
}
