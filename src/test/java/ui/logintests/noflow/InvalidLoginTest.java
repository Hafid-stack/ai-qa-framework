package ui.logintests.noflow;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;

public class InvalidLoginTest extends BaseTest {


    @Test(description = "Test case N2, Login User with Incorrect credentials")
    public void invalidLoginTest() {

        LoginPage loginPage = new LoginPage(driver);

        loginPage.open();
        Assert.assertEquals(driver.getCurrentUrl(), ConfigReader.get("loginUrl"), "Base Url does not match");


        loginPage.typeUsername(ConfigReader.get("invalid.username"));
        loginPage.typePassword(ConfigReader.get("invalid.password"));
        loginPage.clickLogin();
        Assert.assertEquals(loginPage.getErrorMessage(),ConfigReader.get("error.message.invalid"), "Invalid username or password");

        log("Test case N2, Login User with Incorrect credentials, Passed");
    }
}
