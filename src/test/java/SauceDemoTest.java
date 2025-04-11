import com.microsoft.playwright.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SauceDemoTest {
    public static void main(String[] args) {
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

        // Set up network interception
        context.onRequest(request -> {
            if (request.method().equals("POST")) {
                // This will print the POST request URL and body
                System.out.println("POST Request made to: " + request.url());

                String postData = request.postData();
                System.out.println("POST Request Body: " + postData);
            }
        });

        // Create a new page
        Page page = context.newPage();

//        // Block specific Backtrace requests
//        page.route("**/api/unique-events/submit*", route -> route.abort());
//        page.route("**/api/summed-events/submit*", route -> route.abort());

        // Navigate to SauceDemo site
        page.navigate("https://www.saucedemo.com/");

        page.waitForTimeout(3000); // Wait for 3 seconds

        // Log in with valid credentials
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.waitForTimeout(2000); // Wait for 2 seconds
        page.click("[data-test='login-button']");

        // Wait for the inventory page to load
        page.waitForSelector(".inventory_item");

        // Get the list of inventory item prices
        Locator priceLocator = page.locator(".inventory_item_price");

        // Use elementHandles() to get all the price elements
        List<ElementHandle> priceElements = priceLocator.elementHandles();

        // List to store the prices as doubles
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

        // Find the highest price
        if (prices.size() > 0) {
            double highestPrice = prices.stream().max(Double::compare).get(); // Get the max price
            System.out.println("Highest price: $" + highestPrice);

            // Format the highest price as a string to compare with the text content of price elements
            String highestPriceText = String.format("$%.2f", highestPrice);

            // Locate all the inventory items on the page
            Locator inventoryItems = page.locator(".inventory_item");

            // Loop through each inventory item
            for (int i = 0; i < inventoryItems.count(); i++) {
                // Locate the price for the current inventory item
                Locator price = inventoryItems.nth(i).locator(".inventory_item_price");

                // Check if the price matches the highest price
                if (price.textContent().equals(highestPriceText)) {
                    // Locate the 'Add to Cart' button for the current inventory item and click it
                    Locator addToCartButton = inventoryItems.nth(i).locator("button:has-text('Add to cart')");
                    addToCartButton.click();
                    System.out.println("Clicked 'Add to cart' for the item with price: " + highestPriceText);
                    break;  // Exit the loop after clicking the first match
                }
            }
        }

        // Check if all items are correctly processed (by printing the prices)
        System.out.println("List of all item prices:");
        for (Double price : prices) {
            System.out.println("$" + price);
        }

        // Take a screenshot of the products page
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshots/products_screenshot.png")));
        System.out.println("Screenshot taken of the products page.");

        page.waitForTimeout(1000); // Wait for 1 second

        // Stop tracing and save to file
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace.zip"))
        );

        // Close the browser
        browser.close();

        // Videos are automatically saved to the specified directory (e.g., "videos").
        System.out.println("Video saved in the 'videos' directory.");

        playwright.close();
    }
}
