package pageobjects;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Pip3Run2 extends BasePage {

    public Pip3Run2(WebDriver driver) {
        super(driver);
    }

    private final By openMenuButton = By.id("react-burger-menu-btn");
    private final By inventorySidebarLink = By.dataTest("inventory-sidebar-link");
    private final By aboutSidebarLink = By.dataTest("about-sidebar-link");
    private final By logoutSidebarLink = By.dataTest("logout-sidebar-link");
    private final By resetSidebarLink = By.dataTest("reset-sidebar-link");
    private final By closeMenuButton = By.id("react-burger-cross-btn");
    private final By shoppingCartLink = By.dataTest("shopping-cart-link");
    private final By productSortDropdown = By.dataTest("product-sort-container");
    
    private final By addToCartBackpack = By.dataTest("add-to-cart-sauce-labs-backpack");
    private final By addToCartBikeLight = By.dataTest("add-to-cart-sauce-labs-bike-light");
    private final By addToCartBoltTShirt = By.dataTest("add-to-cart-sauce-labs-bolt-t-shirt");
    private final By addToCartFleeceJacket = By.dataTest("add-to-cart-sauce-labs-fleece-jacket");
    private final By addToCartOnesie = By.dataTest("add-to-cart-sauce-labs-onesie");
    private final By addToCartRedTShirt = By.dataTest("add-to-cart-test.allthethings()-t-shirt-(red)");

    private final By twitterLink = By.dataTest("social-twitter");
    private final By facebookLink = By.dataTest("social-facebook");
    private final By linkedinLink = By.dataTest("social-linkedin");

    public void clickOpenMenu() { click(openMenuButton); }
    public void clickInventorySidebar() { click(inventorySidebarLink); }
    public void clickAboutSidebar() { click(aboutSidebarLink); }
    public void clickLogoutSidebar() { click(logoutSidebarLink); }
    public void clickResetSidebar() { click(resetSidebarLink); }
    public void clickCloseMenu() { click(closeMenuButton); }
    public void clickShoppingCart() { click(shoppingCartLink); }
    
    public void selectProductSort(String value) { type(productSortDropdown, value); }
    
    public void addBackpackToCart() { click(addToCartBackpack); }
    public void addBikeLightToCart() { click(addToCartBikeLight); }
    public void addBoltTShirtToCart() { click(addToCartBoltTShirt); }
    public void addFleeceJacketToCart() { click(addToCartFleeceJacket); }
    public void addOnesieToCart() { click(addToCartOnesie); }
    public void addRedTShirtToCart() { click(addToCartRedTShirt); }
    
    public void clickTwitter() { click(twitterLink); }
    public void clickFacebook() { click(facebookLink); }
    public void clickLinkedin() { click(linkedinLink); }
}