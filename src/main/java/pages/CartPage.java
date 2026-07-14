package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class CartPage extends BasePage {

    private final By productInCartName=By.cssSelector("[data-test='inventory-item-name']");
    private final By cartContainer = By.cssSelector("[data-test='cart-contents-container']");
    public CartPage(WebDriver driver){
        super(driver);
    }


    public Boolean isCartDisplayed(){
        return isDisplayed(cartContainer);
    }
    public Boolean isProductInCart(String productName){

        List<WebElement> productsInCart=driver.findElements(productInCartName);
        return productsInCart.stream()
                .anyMatch(el -> el.getText().equals(productName));
    }
    public int getProductsInCartCount(){
        return driver.findElements(productInCartName).size();
//         List<WebElement> products= driver.findElements(productInCartName);
//         return products.size();
    }

}
