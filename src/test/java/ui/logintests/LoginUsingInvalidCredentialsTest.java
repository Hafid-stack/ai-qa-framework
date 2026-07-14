package ui.logintests;

import base.BaseTest;
import flows.LoginFlow;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;

public class LoginUsingInvalidCredentialsTest extends BaseTest {

    @Test
    public void loginUsingInvalidCredentialsTest(){

        LoginFlow loginFlow = new LoginFlow(driver);

        LoginPage loginPage= loginFlow.loginWithInvalidUser(ConfigReader.get("invalid.username"), ConfigReader.get("invalid.password"));
        System.out.println(loginPage.getErrorMessage());
        Assert.assertTrue(loginPage.getErrorMessage().contains(ConfigReader.get("error.message.invalid")),"Invalid username or password");

    }
}
