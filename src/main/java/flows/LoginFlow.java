package flows;

import base.BaseFlow;
import org.openqa.selenium.WebDriver;
import pages.InventoryPage;
import pages.LoginPage;

public class LoginFlow extends BaseFlow {

    private LoginPage loginPage;
    private InventoryPage inventoryPage;
    public LoginFlow(WebDriver driver) {
        super(driver);
        this.loginPage = new LoginPage(driver);
        this.inventoryPage = new InventoryPage(driver);

    }
    //Happy path
    public InventoryPage loginAsValidUser(String username, String password) {
        loginPage.open();
        loginPage.typeUsername(username);
        loginPage.typePassword(password);
        loginPage.clickLogin();

        return  inventoryPage;
    }
    //Negative path
    public InventoryPage loginWithInvalidUser(String username, String password) {
        loginPage.open();
        loginPage.typeUsername(username);
        loginPage.typePassword(password);
        loginPage.clickLogin();

        return  inventoryPage;
    }
}
