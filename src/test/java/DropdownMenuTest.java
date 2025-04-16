import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import org.example.pages.LoginPage;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DropdownMenuTest extends BeforeAllTests {
    // This tells SLF4J to create a logger that tags all log messages with the class name RandomTest. It helps you identify which class the log message came from.
    private static final Logger logger = LoggerFactory.getLogger(DropdownMenuTest.class);

    @Test
    public void testDropdownMenu() {
        logger.info("Test started");

        // Set up Playwright
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setRecordVideoDir(Paths.get("videos"))
                        .setRecordVideoSize(1280, 720)
        );

        // Start tracing
        System.setProperty("PLAYWRIGHT_JAVA_SRC", "src/test/java");
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
        );

        // Create a new page
        Page page = context.newPage();
        logger.info("Logging in...");

        // Navigate to Login page
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();
        page.waitForTimeout(2000);
        loginPage.login("standard_user", "secret_sauce");

        // Test logger ERROR
        if (page.url().contains("inventory")) {
            logger.error("Nothing to worry about. We're in the right place: {}", page.url());
        } else {
            logger.error("Login did not redirect to inventory. Current URL: {}", page.url());
        }

        // Best practise: Locate by Data-Test Attributes (which are added specifically for testing)
        Locator sauceLabsBikeLight = page.locator("[data-test='inventory-item-name']:has-text('Sauce Labs Bike Light')");
        sauceLabsBikeLight.click();
        page.waitForTimeout(2000);

        // Locate by Tag name (<button>, <div>, etc.)
        Locator addToCart = page.locator("button.btn:has-text('Add to cart')");
        Locator bikeLightPrice = page.locator("div.inventory_details_price:has-text('$9.99')");
        assertTrue(addToCart.isVisible(), "Add To Cart button should be visible and clickable");
        assertTrue(bikeLightPrice.isVisible(), "Bike price should be visible");

        // Locate by ID (<id>)
        Locator bikeLightContainer = page.locator("#inventory_item_container");
        bikeLightContainer.waitFor();
        assertThat(bikeLightContainer).hasId("inventory_item_container");

        // Locate by Class value
        Locator backToProductsBtn = page.locator(".inventory_details_back_button");
        assertThat(backToProductsBtn).hasClass("btn btn_secondary back btn_large inventory_details_back_button");
        backToProductsBtn.click();

        // Select by Attribute (<data-test>)
        Locator dropdownMenu = page.locator("select[data-test='product-sort-container']");
        dropdownMenu.selectOption("za"); // Select the option by its value
        page.waitForTimeout(2000);
        assertEquals("za", dropdownMenu.inputValue());
        dropdownMenu.selectOption(new SelectOption().setLabel("Price (low to high)")); // Select the option by its visible label
        page.waitForTimeout(2000);
        assertEquals("lohi", dropdownMenu.inputValue());

        // Select by Class and Index
        Locator dropdownMenu2 = page.locator("select.product_sort_container");
        dropdownMenu2.selectOption(new SelectOption().setIndex(3)); // Selects Price (high to low)
        assertEquals("hilo", dropdownMenu.inputValue());

        // Locate by Class (full)
        Locator facebookLink = page.locator("[class='social_facebook']");
        Page facebookTab = page.waitForPopup(() -> {
            facebookLink.click();
        });
        facebookTab.waitForLoadState();
        assertEquals("https://www.facebook.com/saucelabs", facebookTab.url());

        // Locate by combining different selectors
        Locator twitterLink = page.locator("a[data-test='social-twitter'][href='https://twitter.com/saucelabs']");
        Page twitterTab = page.waitForPopup(() -> {twitterLink.click();});
        twitterTab.waitForLoadState();
        assertEquals("https://x.com/saucelabs", twitterTab.url());

        // Locate by combining different selectors (on Twitter (X) page)
        page.navigate("https://x.com/saucelabs");
        Locator followButton = page.locator("button[aria-label='Follow @saucelabs'][data-testid='16672130-follow']");
        followButton.click();
        page.waitForTimeout(2000);
        Locator followText = page.locator("div[dir='ltr']:has-text('Follow Sauce Labs to see what they share on X.')");
        assertTrue(followText.isVisible(), "Follow message should be visible after following @saucelabs");

        // Stop tracing and save to file
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip"))
        );

        // Close the browser and finish the test
        browser.close();
        playwright.close();
        logger.info("Test finished successfully");
    }
}
