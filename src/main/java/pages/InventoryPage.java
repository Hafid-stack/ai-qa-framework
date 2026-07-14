package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.RandomPick;

import java.util.ArrayList;
import java.util.List;


public class InventoryPage extends BasePage {
    private final By logoutBtn=By.cssSelector("[data-test='logout-sidebar-link']");
    private final By cartBtn= By.cssSelector("[data-test='shopping-cart-link']");
    private final By inventoryContainer = By.cssSelector(".inventory_container");
    private static final String PREFIX = "add-to-cart-";
    //private final By addToCartBtn = By.cssSelector("data-test='add-to-cart-sauce-labs-backpack'");
    public InventoryPage(WebDriver driver) {
        super(driver);
    }


    public List<String> getAvailableProductIds() {
        List<WebElement> buttons = driver.findElements(By.cssSelector("[data-test^='" + PREFIX + "']"));
        List<String> productIds = new ArrayList<>();
        for (WebElement btn : buttons) {
            String fullValue = btn.getAttribute("data-test");
            String productId = fullValue.replace(PREFIX, "");
            productIds.add(productId);
        }
        return productIds;

    }
    public String addRandomProductToCart() {
        List<String> productIds = getAvailableProductIds();
        String chosenId = RandomPick.randomPick(productIds);

        String selector = "[data-test='" + PREFIX + chosenId + "']";
        click(By.cssSelector(selector));

        return chosenId; // return it so the test can assert on it later
    }
    public boolean isLoaded() {
        return isDisplayed(inventoryContainer);
    }
    public void clickCartButton() {

        click(cartBtn);

    }
    public void clickLogoutButton() {
        click(logoutBtn);
    }

}