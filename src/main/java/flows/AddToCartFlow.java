package flows;

import base.BaseFlow;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.InventoryPage;

public class AddToCartFlow extends BaseFlow {

    private InventoryPage inventoryPage;
    public AddToCartFlow(WebDriver driver) {
        super(driver);
        inventoryPage = new InventoryPage(driver);
    }

    public InventoryPage addToCart(){
        inventoryPage.addRandomProductToCart();
        inventoryPage.addRandomProductToCart();
        inventoryPage.clickCartButton();
        return inventoryPage;
    }
}
