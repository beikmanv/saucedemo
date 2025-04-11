import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class CheckoutProcessTest {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutProcessTest.class);

    public static void main(String[] args) {
        logger.info("Test started");

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setRecordVideoDir(Paths.get("videos")).setRecordVideoSize(1280, 720)
        );
        Page page = context.newPage();

        page.waitForTimeout(2000);

        // Log in to the application
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");

        page.waitForTimeout(2000);

        // Add item to the cart
        page.locator(".inventory_item .btn_inventory").first().click();
        page.locator(".shopping_cart_link").click();

        page.waitForTimeout(2000);

        // Proceed to checkout
        page.locator("text=Checkout").click();

        // Fill out shipping information (use text-based selectors)
        page.fill("input[name='firstName']", "John");
        page.fill("input[name='lastName']", "Doe");
        page.fill("input[name='postalCode']", "90210");
        page.click("text=Continue");

        page.waitForTimeout(2000);

        logger.info("Shipping information filled out");

        // Verify Checkout Overview page
        Locator summaryHeader = page.locator("text=Checkout: Overview");
        if (!summaryHeader.isVisible()) {
            logger.error("Failed to reach the Checkout Overview page.");
        }

        page.waitForTimeout(2000);

        page.click("text=Finish");

        logger.info("Moving to Checkout Complete page...");

        // Verify Checkout Complete page
        Locator backHome = page.locator("text=Back Home");
        if (!backHome.isVisible()) {
            logger.error("Failed to reach the Checkout Complete page.");
        }

        page.waitForTimeout(2000);

        browser.close();
        playwright.close();

        logger.info("Test finished");
    }
}
