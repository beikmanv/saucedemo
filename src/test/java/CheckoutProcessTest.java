import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CheckoutProcessTest extends BeforeAllTests {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutProcessTest.class);

    @Test
    public void testCheckoutProcess() {
        logger.info("Test started");

        Playwright playwright = Playwright.create();
        Browser browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setRecordVideoDir(Paths.get("videos")).setRecordVideoSize(1280, 720)
        );

        context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
        Page page = context.newPage();

        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        assertTrue(page.url().contains("inventory"));

        page.locator(".inventory_item .btn_inventory").first().click();
        page.locator(".shopping_cart_link").click();
        assertTrue(page.url().contains("cart"));

        page.locator("text=Checkout").click();
        assertTrue(page.locator("text=Checkout: Your Information").isVisible());

        page.fill("input[name='firstName']", "John");
        page.fill("input[name='lastName']", "Doe");
        page.fill("input[name='postalCode']", "90210");
        page.click("text=Continue");

        Locator summaryHeader = page.locator("text=Checkout: Overview");
        assertTrue(summaryHeader.isVisible());

        page.click("text=Finish");
        logger.info("Moving to Checkout Complete page...");

        Locator backHome = page.locator("text=Back Home");
        assertTrue(backHome.isVisible());

        context.tracing().stop(new Tracing.StopOptions().setPath(Paths.get("trace.zip")));
        browser.close();
        playwright.close();

        logger.info("Test finished");
    }
}
