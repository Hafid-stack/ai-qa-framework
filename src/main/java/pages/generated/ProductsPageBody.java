package pages.generated;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import java.util.List;
import java.util.stream.Collectors;

public class ProductsPageBody extends BasePage {

    private final By bySearchProduct = By.cssSelector("#search_product");
    private final By bySubmitSearch = By.cssSelector("#submit_search");
    private final By byContinueShopping = By.xpath("//button[contains(normalize-space(.), 'Continue Shopping')]");
    private final By by6Polo = By.xpath("//a[contains(normalize-space(.), '(6)Polo')]");
    private final By by5HM = By.xpath("//a[contains(normalize-space(.), '(5)H&M')]");
    private final By by5Madame = By.xpath("//a[contains(normalize-space(.), '(5)Madame')]");
    private final By by3MastHarbour = By.xpath("//a[contains(normalize-space(.), '(3)Mast & Harbour')]");
    private final By by4Babyhug = By.xpath("//a[contains(normalize-space(.), '(4)Babyhug')]");
    private final By by3AllenSollyJunior = By.xpath("//a[contains(normalize-space(.), '(3)Allen Solly Junior')]");
    private final By by3KookieKids = By.xpath("//a[contains(normalize-space(.), '(3)Kookie Kids')]");
    private final By by5Biba = By.xpath("//a[contains(normalize-space(.), '(5)Biba')]");
    private final By byViewCart = By.xpath("//a[contains(normalize-space(.), 'View Cart')]");
    private final By byScrollUp = By.cssSelector("#scrollUp");

    public ProductsPageBody(WebDriver driver) {
        super(driver);
    }

    public void typeSearchProduct(String value) {
        type(bySearchProduct, value);
    }

    public void clickSubmitSearch() {
        click(bySubmitSearch);
    }

    public void clickContinueShopping() {
        click(byContinueShopping);
    }

    public void click6Polo() {
        click(by6Polo);
    }

    public String getText6Polo() {
        return getText(by6Polo);
    }

    public void click5HM() {
        click(by5HM);
    }

    public String getText5HM() {
        return getText(by5HM);
    }

    public void click5Madame() {
        click(by5Madame);
    }

    public String getText5Madame() {
        return getText(by5Madame);
    }

    public void click3MastHarbour() {
        click(by3MastHarbour);
    }

    public String getText3MastHarbour() {
        return getText(by3MastHarbour);
    }

    public void click4Babyhug() {
        click(by4Babyhug);
    }

    public String getText4Babyhug() {
        return getText(by4Babyhug);
    }

    public void click3AllenSollyJunior() {
        click(by3AllenSollyJunior);
    }

    public String getText3AllenSollyJunior() {
        return getText(by3AllenSollyJunior);
    }

    public void click3KookieKids() {
        click(by3KookieKids);
    }

    public String getText3KookieKids() {
        return getText(by3KookieKids);
    }

    public void click5Biba() {
        click(by5Biba);
    }

    public String getText5Biba() {
        return getText(by5Biba);
    }

    public void clickViewCart() {
        click(byViewCart);
    }

    public String getTextViewCart() {
        return getText(byViewCart);
    }

    public void clickScrollUp() {
        click(byScrollUp);
    }

    // "div.panel" repeated 3 times on the page this was generated from.
    public List<PanelItem> getPanelItemList() {
        return driver.findElements(By.cssSelector("div.panel-group > div.panel")).stream().map(PanelItem::new).collect(Collectors.toList());
    }

    // "div.col-sm-4" repeated 34 times on the page this was generated from.
    public List<ColSm4Item> getColSm4ItemList() {
        return driver.findElements(By.cssSelector("div.features_items > div.col-sm-4")).stream().map(ColSm4Item::new).collect(Collectors.toList());
    }

    // One instance of the repeated "div.panel" pattern.
    // Usage: getPanelItemList().get(0).clickWomen();
    public class PanelItem {
        private final WebElement root;

        public PanelItem(WebElement root) {
            this.root = root;
        }

        public WebElement getRoot() {
            return root;
        }

        public void clickWomen() {
            WebElement el = root.findElement(By.xpath(".//a[contains(normalize-space(.), 'Women')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

        public void clickDress() {
            WebElement el = root.findElement(By.xpath(".//a[contains(normalize-space(.), 'Dress')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

        public void clickTops() {
            WebElement el = root.findElement(By.xpath(".//a[contains(normalize-space(.), 'Tops')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

        public void clickSaree() {
            WebElement el = root.findElement(By.xpath(".//a[contains(normalize-space(.), 'Saree')]"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

    }

    // One instance of the repeated "div.col-sm-4" pattern.
    // Usage: getColSm4ItemList().get(0).clickAddToCart();
    public class ColSm4Item {
        private final WebElement root;

        public ColSm4Item(WebElement root) {
            this.root = root;
        }

        public WebElement getRoot() {
            return root;
        }

        public void clickAddToCart() {
            WebElement el = root.findElement(By.cssSelector("a.btn"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

        public void clickViewProduct() {
            WebElement el = root.findElement(By.cssSelector("a:not([class])"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

    }

}
