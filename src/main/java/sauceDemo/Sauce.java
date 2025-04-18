//package sauceDemo;
package sauceDemo;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

public class Sauce {
    WebDriver driver;

    public void Setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-extensions");

        // Disable Chrome password manager popup
        java.util.HashMap<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // Optional: headless mode (comment out if you want browser UI)
        // options.addArguments("--headless=new");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("https://www.saucedemo.com/");
    }

    public void login() throws InterruptedException {
        try {
            driver.findElement(By.id("user-name")).sendKeys("standard_user");
            driver.findElement(By.id("password")).sendKeys("secret_sauce");
            Thread.sleep(1000); // Simulate user wait
            driver.findElement(By.id("login-button")).click();
            System.out.println("Login successful.");
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
            takeScreenshot("LoginFail");
        }
    }

    public void priceCheckLowToHigh() {
        int retries = 3;
        boolean success = false;

        for (int i = 0; i < retries; i++) {
            try {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
                Select drop = new Select(dropdown);
                drop.selectByValue("lohi");
                String selectedText = drop.getFirstSelectedOption().getText();
                System.out.println("Selected sort option: " + selectedText);
                success = true;
                break;
            } catch (StaleElementReferenceException e) {
                System.out.println("Attempt " + (i + 1) + ": Element is stale, retrying...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }

        if (!success) {
            System.out.println("Failed to select sort option after retries.");
            takeScreenshot("DropdownSortFail");
        }
    }

    public void addToCart() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement shirt = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-bolt-t-shirt")));
            shirt.click();
            WebElement backpack = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-to-cart-sauce-labs-backpack")));
            backpack.click();
            System.out.println("Products added to cart successfully.");
        } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException e) {
            System.out.println("Add to Cart Error: " + e.getMessage());
            takeScreenshot("AddToCartFail");
        }
    }

    public void checkCart() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement cart = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='shopping_cart_container']/a")));
            cart.click();
            System.out.println("Cart opened successfully.");

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("cart_item")));
            List<WebElement> cartItems = driver.findElements(By.className("cart_item"));
            System.out.println("Items in Cart:");
            for (WebElement item : cartItems) {
                String name = item.findElement(By.className("inventory_item_name")).getText();
                String price = item.findElement(By.className("inventory_item_price")).getText();
                System.out.println("Name: " + name + " | Price: " + price);
            }

        } catch (TimeoutException e) {
            System.out.println("Cart items didn't load in time: " + e.getMessage());
            takeScreenshot("CartTimeout");
            driver.navigate().refresh();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            System.out.println("Cart load error: " + e.getMessage());
            takeScreenshot("CartLoadError");
        } catch (Exception e) {
            System.out.println("Unexpected error while accessing cart: " + e.getMessage());
            takeScreenshot("UnexpectedCartError");
        }
    }

    public void checkOut() {
        try {
            // Step 1: Start Checkout
            driver.findElement(By.xpath("//button[text()='Checkout']")).click();
            System.out.println("Checkout: Your Information");

            driver.findElement(By.id("first-name")).sendKeys("jaganReddy");
            driver.findElement(By.id("last-name")).sendKeys("lakkiReddy");
            driver.findElement(By.id("postal-code")).sendKeys("516360");
            driver.findElement(By.id("continue")).click();

            // Step 2: On Overview Page - print item summaries
            System.out.println("------ Checkout: Overview ------");

            List<WebElement> items = driver.findElements(By.className("cart_item"));
            for (WebElement item : items) {
                String qty = item.findElement(By.className("cart_quantity")).getText();
                String name = item.findElement(By.className("inventory_item_name")).getText();
                String desc = item.findElement(By.className("inventory_item_desc")).getText();
                String price = item.findElement(By.className("inventory_item_price")).getText();

                System.out.println("QTY: " + qty);
                System.out.println("Product: " + name);
                System.out.println("Description: " + desc);
                System.out.println("Price: " + price);
                System.out.println("--------------------------------");
            }

            // Step 3: Print Payment, Shipping, Totals
            String paymentInfo = driver.findElement(By.className("summary_value_label")).getText();
            String shippingInfo = driver.findElements(By.className("summary_value_label")).get(1).getText();
            String itemTotal = driver.findElement(By.className("summary_subtotal_label")).getText();
            String tax = driver.findElement(By.className("summary_tax_label")).getText();
            String total = driver.findElement(By.className("summary_total_label")).getText();

            System.out.println("Payment Information: " + paymentInfo);
            System.out.println("Shipping Information: " + shippingInfo);
            System.out.println(itemTotal);
            System.out.println(tax);
            System.out.println(total);

            // Step 4: Finish checkout
            driver.findElement(By.id("finish")).click();
            System.out.println("Checkout completed successfully!");

        } catch (NoSuchElementException e) {
            System.out.println("Checkout error: " + e.getMessage());
            takeScreenshot("CheckoutFail");
        } catch (Exception e) {
            System.out.println("Unexpected checkout error: " + e.getMessage());
            takeScreenshot("CheckoutUnexpected");
        }
    }

    public void takeScreenshot(String filenameSuffix) {
        int retries = 3;
        boolean success = false;

        for (int i = 0; i < retries; i++) {
            try {
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String timestamp = java.time.LocalDateTime.now().toString().replace(":", "-");
                File dest = new File("D:/Eclips_Installer_Programs/sauceDemo/src/test/resources/ScreenShot/screenshot_" + filenameSuffix + "_" + timestamp + ".png");
                FileUtils.copyFile(src, dest);
                System.out.println("Screenshot saved to: " + dest.getAbsolutePath());
                success = true;
                break;
            } catch (IOException io) {
                System.out.println("Failed to capture screenshot on attempt " + (i + 1) + ": " + io.getMessage());
            }
        }

        if (!success) {
            System.out.println("Screenshot capture failed after " + retries + " attempts.");
        }
    }

    public void tearDown() {
        driver.quit();
    }

    public static void main(String[] args) throws InterruptedException {
        Sauce sc = new Sauce();
        sc.Setup();
        sc.login();
        sc.priceCheckLowToHigh();
        sc.addToCart();
        sc.checkCart();
        sc.checkOut();
        sc.takeScreenshot("FinalState");
        sc.tearDown();
    }
}
