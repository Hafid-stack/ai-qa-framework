package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class sauceDemoInventoryPage extends BasePage {

    private final By menuButton = By.id("react-burger-menu-btn");
    private final By inventorySidebarLink = By.dataTest("inventory-sidebar-link");
    private final By dynamicCatalogSidebarLink = By.dataTest("dynamic-catalog-sidebar-link");
    private final By aboutSidebarLink = By.dataTest("about-sidebar-link");
    private final By logoutSidebarLink = By.dataTest("logout-sidebar-link");
    private final By resetSidebarLink = By.dataTest("reset-sidebar-link");
    private final By closeMenuButton = By.id("react-burger-cross-btn");
    private final By shoppingCartLink = By.dataTest("shopping-cart-link");
    private final By productSortDropdown = By.dataTest("product-sort-container");
    
    // Add to cart buttons
    private final By addToCartBackpack = By.dataTest("add-to-cart-sauce-labs-backpack");
    private final By addToCartBikeLight = By.dataTest("add-to-cart-sauce-labs-bike-light");
    private final By addToCartBoltTShirt = By.dataTest("add-to-cart-sauce-labs-bolt-t-shirt");
    private final By addToCartFleeceJacket = By.dataTest("add-to-cart-sauce-labs-fleece-jacket");
    private final By addToCartOnesie = By.dataTest("add-to-cart-sauce-labs-onesie");
    private final By addToCartTestTShirt = By.dataTest("add-to-cart-test.allthethings()-t-shirt-(red)");

    public sauceDemoInventoryPage(WebDriver driver) {
        super(driver);
    }

    public void openMenu() {
        click(menuButton);
    }

    public void clickAllItems() {
        click(inventorySidebarLink);
    }

    public void clickDynamicCatalog() {
        click(dynamicCatalogSidebarLink);
    }

    public void clickAbout() {
        click(aboutSidebarLink);
    }

    public void clickLogout() {
        click(logoutSidebarLink);
    }

    public void clickResetAppState() {
        click(resetSidebarLink);
    }

    public void closeMenu() {
        click(closeMenuButton);
    }

    public void goToCart() {
        click(shoppingCartLink);
    }

    public void sortProducts(String value) {
        type(productSortDropdown, value);
    }

    public void addBackpackToCart() {
        click(addToCartBackpack);
    }

    public void addBikeLightToCart() {
        click(addToCartBikeLight);
    }

    public void addBoltTShirtToCart() {
        click(addToCartBoltTShirt);
    }

    public void addFleeceJacketToCart() {
        click(addToCartFleeceJacket);
    }

    public void addOnesieToCart() {
        click(addToCartOnesie);
    }

    public void addTestTShirtToCart() {
        click(addToCartTestTShirt);
    }
}