package E2ETests;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.example.pages.LoginPage;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;

public class MercatorTest extends BeforeAllTestsE2E {
    private static final Logger logger = LoggerFactory.getLogger(MercatorTest.class);

    @Test
    public void testMercator() {
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

        // ASSERT: Check if we're on the inventory page after login
        assertTrue(page.url().contains("inventory.html"), "User should land on the inventory page after login");

        // Get the list of inventory item prices
        Locator priceLocator = page.locator(".inventory_item_price");
        List<ElementHandle> priceElements = priceLocator.elementHandles();
        List<Double> prices = new ArrayList<>();

        // Extract and store the prices
        for (ElementHandle priceElement : priceElements) {
            String priceText = priceElement.textContent().replace("$", "").trim();
            try {
                double price = Double.parseDouble(priceText);
                prices.add(price);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing price: " + priceText); // ASSERT failure on parse
            }
        }

        // ASSERT: Verify that items are present
        assertFalse(prices.isEmpty(), "There should be at least one item listed");

        // Count the total number of prices
        int totalItems = prices.size();
        System.out.println("Total items: " + totalItems);
        logger.info("Adding highest priced item to the cart...");

        // Find the highest price
        if (prices.size() > 0) {
            double highestPrice = prices.stream().max(Double::compare).get();
            System.out.println("Highest price: $" + highestPrice);
            String highestPriceText = String.format("$%.2f", highestPrice);
            Locator inventoryItems = page.locator(".inventory_item");

            boolean itemClicked = false;
            for (int i = 0; i < inventoryItems.count(); i++) {
                Locator price = inventoryItems.nth(i).locator(".inventory_item_price");

                if (price.textContent().equals(highestPriceText)) {
                    // Locate the 'Add to Cart' button for the current inventory item and click it
                    Locator addToCartButton = inventoryItems.nth(i).locator("button:has-text('Add to cart')");
                    addToCartButton.click();
                    System.out.println("Clicked 'Add to cart' for the item with price: " + highestPriceText);
                    itemClicked = true;
                    break;
                }
            }

            // ASSERT: Check if the item was actually clicked
            assertTrue(itemClicked, "Expected item with the highest price should be added to cart");

            // ASSERT: Verify that the cart badge is showing 1
            Locator cartBadge = page.locator(".shopping_cart_badge");
            assertTrue(cartBadge.isVisible(), "Cart badge should be visible after adding an item");
            assertEquals("1", cartBadge.textContent(), "Cart should show 1 item");
        }

        // Check if all items are correctly processed (by printing the prices)
        System.out.println("List of all item prices:");
        for (Double price : prices) {
            System.out.println("$" + price);
        }
        page.waitForTimeout(1000);

        // Stop tracing and save to file
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip"))
        );

        // Close the browser and finish the test
        browser.close();
        System.out.println("Video saved in the 'videos' directory.");
        playwright.close();
        logger.info("Test finished successfully");
    }
}
