import com.microsoft.playwright.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CheckoutProcessTest {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutProcessTest.class);

    public static void main(String[] args) {
        logger.info("Test started");
        logger.debug("This is a debug log - should show if configured correctly");

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setRecordVideoDir(Paths.get("videos")).setRecordVideoSize(1280, 720)
        );

        // Start tracing
        System.setProperty("PLAYWRIGHT_JAVA_SRC", "src/test/java");
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
        );

        Page page = context.newPage();
        page.waitForTimeout(2000);

        // Log in to the application
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        page.waitForTimeout(2000);
        assertTrue(page.url().contains("inventory"), "Login failed or did not navigate to inventory page");

        // Add item to the cart
        page.locator(".inventory_item .btn_inventory").first().click();
        page.locator(".shopping_cart_link").click();
        page.waitForTimeout(2000);
        assertTrue(page.url().contains("cart"), "Cart page not reached after clicking cart icon");

        // Proceed to checkout
        page.locator("text=Checkout").click();
        assertTrue(page.locator("text=Checkout: Your Information").isVisible(), "Checkout Your Information page is not visible");

        // Fill out shipping information
        page.fill("input[name='firstName']", "John");
        page.fill("input[name='lastName']", "Doe");
        page.fill("input[name='postalCode']", "90210");
        page.click("text=Continue");
        page.waitForTimeout(2000);

        // Verify Checkout Overview page
        Locator summaryHeader = page.locator("text=Checkout: Overview");
        assertTrue(summaryHeader.isVisible(), "Checkout Overview page was not displayed");

        // Finish checkout
        page.click("text=Finish");
        logger.info("Moving to Checkout Complete page...");

        // Verify Checkout Complete page
        Locator backHome = page.locator("text=Back Home");
        assertTrue(backHome.isVisible(), "Checkout Complete page was not reached");
        if (!backHome.isVisible()) {
            logger.error("Failed to reach the Checkout Complete page.");
        }
        page.waitForTimeout(2000);

        // Stop tracing and save to file
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip"))
        );

        // Close the browser and finish the test
        browser.close();
        playwright.close();
        logger.info("Test finished");
    }
}
