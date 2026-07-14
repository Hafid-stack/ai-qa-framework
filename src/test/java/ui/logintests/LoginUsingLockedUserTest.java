package ui.logintests;

import base.BaseTest;
import flows.LoginFlow;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ConfigReader;

public class LoginUsingLockedUserTest extends BaseTest {

    @Test
    public void loginUsingLockedUserTest(){


        LoginFlow loginFlow = new LoginFlow(driver);

        LoginPage loginPage= loginFlow.loginWithLockedUser(ConfigReader.get("locked.username"),ConfigReader.get("valid.password"));

        Assert.assertTrue(loginPage.getErrorMessage().contains(ConfigReader.get("error.message.locked")));
    }
}
