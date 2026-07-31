package ui.generated;

import base.BaseTest;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.generated.ProductsPageBody;
import pages.generated.ProductsPageHeader;

import java.time.Duration;
import java.util.List;

// Demonstrates that pages.generated.* — produced by the parser/generator/RepeatedComponentDetector
// pipeline (cli.Main, Pipeline A) against the live site, no selector typed by hand — is enough to
// drive a real end-to-end flow: add two products to the cart and verify the cart contents.
//
// To regenerate these page objects from scratch:
//   mvn -q compile exec:java -Dexec.mainClass=cli.Main
//   -> choose "1", URL "automationexercise.com/products", class name "ProductsPage", sections "a"
public class AutomationExerciseGeneratedFlowTest extends BaseTest {

    @Test
    public void addTwoProductsAndVerifyCart() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get("https://automationexercise.com/");
        Assert.assertTrue(driver.getTitle().contains("Automation Exercise"), "Home page did not load");

        ProductsPageHeader header = new ProductsPageHeader(driver);
        header.clickProducts();
        wait.until(ExpectedConditions.urlContains("/products"));

        ProductsPageBody body = new ProductsPageBody(driver);
        Actions actions = new Actions(driver);

        List<ProductsPageBody.ColSm4Item> products = body.getColSm4ItemList();
        Assert.assertTrue(products.size() >= 2, "Expected at least 2 products on the page");

        ProductsPageBody.ColSm4Item firstProduct = products.get(0);
        actions.moveToElement(firstProduct.getRoot()).perform();
        firstProduct.clickAddToCart();
        body.clickContinueShopping();

        // Re-fetch: the first call's WebElements can go stale once the "added to cart" modal
        // has opened and closed, so grab a fresh list before touching the second product.
        products = body.getColSm4ItemList();
        ProductsPageBody.ColSm4Item secondProduct = products.get(1);
        actions.moveToElement(secondProduct.getRoot()).perform();
        secondProduct.clickAddToCart();
        body.clickViewCart();

        wait.until(ExpectedConditions.urlContains("/view_cart"));

        List<WebElement> cartRows = wait.until(
                ExpectedConditions.numberOfElementsToBe(By.cssSelector("#cart_info tbody tr"), 2));

        for (WebElement row : cartRows) {
            String price = row.findElement(By.cssSelector("td.cart_price p")).getText().replace("Rs. ", "").trim();
            String quantity = row.findElement(By.cssSelector("td.cart_quantity button")).getText().trim();
            String total = row.findElement(By.cssSelector("td.cart_total p.cart_total_price")).getText().replace("Rs. ", "").trim();

            int expectedTotal = Integer.parseInt(price) * Integer.parseInt(quantity);
            Assert.assertEquals(Integer.parseInt(total), expectedTotal,
                    "Price x quantity did not match the displayed total for row: " + row.getText());
        }
    }
}
