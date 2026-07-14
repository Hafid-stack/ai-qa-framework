package flows;

import base.BaseFlow;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.CartPage;
import pages.InventoryPage;

public class AddToCartFlow extends BaseFlow {

    private InventoryPage inventoryPage;
    private CartPage cartPage;
    public AddToCartFlow(WebDriver driver) {
        super(driver);
        inventoryPage = new InventoryPage(driver);
        cartPage = new CartPage(driver);
    }

    public CartPage addToCart(){
        inventoryPage.addRandomProductToCart();
        inventoryPage.addRandomProductToCart();
        inventoryPage.clickCartButton();
        return cartPage;
    }
}
