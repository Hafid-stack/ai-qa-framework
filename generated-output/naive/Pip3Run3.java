package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Pip3Run3 extends BasePage {

    private final By openMenuButton = By.id("react-burger-menu-btn");
    private final By inventorySidebarLink = By.cssSelector("[data-test='inventory-sidebar-link']");
    private final By aboutSidebarLink = By.cssSelector("[data-test='about-sidebar-link']");
    private final By logoutSidebarLink = By.cssSelector("[data-test='logout-sidebar-link']");
    private final By resetSidebarLink = By.cssSelector("[data-test='reset-sidebar-link']");
    private final By closeMenuButton = By.id("react-burger-cross-btn");
    private final By shoppingCartLink = By.cssSelector("[data-test='shopping-cart-link']");
    private final By productSortDropdown = By.cssSelector("[data-test='product-sort-container']");
    private final By addToCartBackpack = By.id("add-to-cart-sauce-labs-backpack");
    private final By addToCartBikeLight = By.id("add-to-cart-sauce-labs-bike-light");
    private final By addToCartBoltTShirt = By.id("add-to-cart-sauce-labs-bolt-t-shirt");
    private final By addToCartFleeceJacket = By.id("add-to-cart-sauce-labs-fleece-jacket");
    private final By addToCartOnesie = By.id("add-to-cart-sauce-labs-onesie");
    private final By addToCartTestTShirt = By.id("add-to-cart-test.allthethings()-t-shirt-(red)");
    private final By twitterLink = By.cssSelector("[data-test='social-twitter']");
    private final By facebookLink = By.cssSelector("[data-test='social-facebook']");
    private final By linkedinLink = By.cssSelector("[data-test='social-linkedin']");

    public Pip3Run3(WebDriver driver) {
        super(driver);
    }

    public void clickOpenMenu() { click(openMenuButton); }
    public void clickInventorySidebar() { click(inventorySidebarLink); }
    public void clickAboutSidebar() { click(aboutSidebarLink); }
    public void clickLogoutSidebar() { click(logoutSidebarLink); }
    public void clickResetSidebar() { click(resetSidebarLink); }
    public void clickCloseMenu() { click(closeMenuButton); }
    public void clickShoppingCart() { click(shoppingCartLink); }
    public void selectProductSort(String value) { type(productSortDropdown, value); }
    public void clickAddToCartBackpack() { click(addToCartBackpack); }
    public void clickAddToCartBikeLight() { click(addToCartBikeLight); }
    public void clickAddToCartBoltTShirt() { click(addToCartBoltTShirt); }
    public void clickAddToCartFleeceJacket() { click(addToCartFleeceJacket); }
    public void clickAddToCartOnesie() { click(addToCartOnesie); }
    public void clickAddToCartTestTShirt() { click(addToCartTestTShirt); }
    public void clickTwitter() { click(twitterLink); }
    public void clickFacebook() { click(facebookLink); }
    public void clickLinkedin() { click(linkedinLink); }

    public String getTitle() { return getText(By.cssSelector("[data-test='title']")); }
    public String getActiveOption() { return getText(By.cssSelector("[data-test='active-option']")); }
}