import com.microsoft.playwright.*;
import org.example.pages.LoginPage;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MercatorTest {
    private static final Logger logger = LoggerFactory.getLogger(MercatorTest.class);

    public static void main(String[] args) {
        logger.info("Test started");

        // Set up Playwright
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
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
                System.out.println("Error parsing price: " + priceText);
            }
        }

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

            for (int i = 0; i < inventoryItems.count(); i++) {
                Locator price = inventoryItems.nth(i).locator(".inventory_item_price");

                if (price.textContent().equals(highestPriceText)) {
                    // Locate the 'Add to Cart' button for the current inventory item and click it
                    Locator addToCartButton = inventoryItems.nth(i).locator("button:has-text('Add to cart')");
                    addToCartButton.click();
                    System.out.println("Clicked 'Add to cart' for the item with price: " + highestPriceText);
                    break;
                }
            }
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
