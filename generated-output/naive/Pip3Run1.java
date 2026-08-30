package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Pip3Run1 extends BasePage {

    public Pip3Run1(WebDriver driver) {
        super(driver);
    }

    private final By openMenuBtn = By.id("react-burger-menu-btn");
    private final By inventorySidebarLink = By.dataTest("inventory-sidebar-link");
    private final By aboutSidebarLink = By.dataTest("about-sidebar-link");
    private final By logoutSidebarLink = By.dataTest("logout-sidebar-link");
    private final By resetSidebarLink = By.dataTest("reset-sidebar-link");
    private final By closeMenuBtn = By.id("react-burger-cross-btn");
    private final By shoppingCartLink = By.dataTest("shopping-cart-link");
    private final By productSortDropdown = By.dataTest("product-sort-container");
    private final By twitterLink = By.dataTest("social-twitter");
    private final By facebookLink = By.dataTest("social-facebook");
    private final By linkedInLink = By.dataTest("social-linkedin");

    public void openMenu() {
        click(openMenuBtn);
    }

    public void clickInventorySidebar() {
        click(inventorySidebarLink);
    }

    public void clickAboutSidebar() {
        click(aboutSidebarLink);
    }

    public void clickLogoutSidebar() {
        click(logoutSidebarLink);
    }

    public void clickResetSidebar() {
        click(resetSidebarLink);
    }

    public void closeMenu() {
        click(closeMenuBtn);
    }

    public void clickShoppingCart() {
        click(shoppingCartLink);
    }

    public void selectProductSort(String value) {
        type(productSortDropdown, value);
    }

    public void clickTwitter() {
        click(twitterLink);
    }

    public void clickFacebook() {
        click(facebookLink);
    }

    public void clickLinkedIn() {
        click(linkedInLink);
    }

    public void addToCart(String itemId) {
        click(By.dataTest("add-to-cart-" + itemId));
    }

    public String getProductPrice(String itemId) {
        // Assuming price is accessed via container or specific element within item
        return getText(By.xpath("//div[@data-test='inventory-item-" + itemId + "']//div[@data-test='inventory-item-price']"));
    }

    public String getTitle() {
        return getText(By.dataTest("title"));
    }
}