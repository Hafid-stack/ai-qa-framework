package pages.generated;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import java.util.List;
import java.util.stream.Collectors;

public class SauceDemoInventoryPagePIPBBodyRun3Body extends BasePage {

    private final By byReactBurgerMenuBtn = By.cssSelector("#react-burger-menu-btn");
    private final By byReactBurgerCrossBtn = By.cssSelector("#react-burger-cross-btn");
    private final By byShoppingCartLink = By.cssSelector("[data-test='shopping-cart-link']");

    public SauceDemoInventoryPagePIPBBodyRun3Body(WebDriver driver) {
        super(driver);
    }

    public void clickReactBurgerMenuBtn() {
        click(byReactBurgerMenuBtn);
    }

    public void clickReactBurgerCrossBtn() {
        click(byReactBurgerCrossBtn);
    }

    public void clickShoppingCartLink() {
        click(byShoppingCartLink);
    }

    // "a.bm-item" repeated 4 times on the page this was generated from.
    public List<BmItemItem> getBmItemItemList() {
        return driver.findElements(By.cssSelector("nav.bm-item-list > a.bm-item")).stream().map(BmItemItem::new).collect(Collectors.toList());
    }

    // "div.inventory_item" repeated 6 times on the page this was generated from.
    public List<InventoryItemItem> getInventoryItemItemList() {
        return driver.findElements(By.cssSelector("div.inventory_list > div.inventory_item")).stream().map(InventoryItemItem::new).collect(Collectors.toList());
    }

    // One instance of the repeated "a.bm-item" pattern.
    // Usage: getBmItemItemList().get(0).clickAllItems();
    public class BmItemItem {
        private final WebElement root;

        public BmItemItem(WebElement root) {
            this.root = root;
        }

        public WebElement getRoot() {
            return root;
        }

        public void clickAllItems() {
            WebElement el = root.findElement(By.cssSelector("a.bm-item"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

    }

    // One instance of the repeated "div.inventory_item" pattern.
    // Usage: getInventoryItemItemList().get(0).clickLink();
    public class InventoryItemItem {
        private final WebElement root;

        public InventoryItemItem(WebElement root) {
            this.root = root;
        }

        public WebElement getRoot() {
            return root;
        }

        public void clickLink() {
            WebElement el = root.findElement(By.cssSelector("a:not([class])"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

        public void clickSauceLabsBackpack() {
            WebElement el = root.findElement(By.xpath(".//a[contains(normalize-space(.), 'Sauce Labs Backpack')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

        public void clickAddToCart() {
            WebElement el = root.findElement(By.cssSelector("button.btn"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

    }

}
