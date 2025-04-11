import com.microsoft.playwright.*;
import org.example.pages.LoginPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BurgerMenuTest {
    private static final Logger logger = LoggerFactory.getLogger(MercatorTest.class);

    public static void main(String[] args) {
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

        // Navigate to Login page
        LoginPage loginPage = new LoginPage(page);
        loginPage.navigate();
        page.waitForTimeout(2000);
        loginPage.login("standard_user", "secret_sauce");

        // Click on any item to test All Items in Burger Menu
        Locator sauceLabsBackpack = page.locator(".inventory_item >> text=Sauce Labs Backpack");
        Locator backToProducts = page.locator("#back-to-products");
        sauceLabsBackpack.click();
        assertTrue(backToProducts.isVisible(), "Back To Products button should be visible and clickable");

        // Burger Menu by ID
        Locator burgerMenuButton1 = page.locator("#react-burger-menu-btn");
        burgerMenuButton1.click();
        page.waitForTimeout(2000);
        assertTrue(page.locator("[data-test='inventory-sidebar-link']").isVisible(), "All Items link should be visible after opening burger menu");

        // Click on All Items
        Locator allItemsBurger = page.locator("[data-test='inventory-sidebar-link'] >> text=All Items");
        allItemsBurger.click();

        // Burger Menu by Button Text
        Locator burgerMenuButton2 = page.locator("button:has-text('Open Menu')");
        burgerMenuButton2.click();
        page.waitForTimeout(2000);
        assertTrue(page.locator("[data-test='about-sidebar-link']").isVisible(), "About link should be visible in burger menu");

        // Click on About
        Locator aboutBurger = page.locator("[data-test='about-sidebar-link'] >> text=About");
        aboutBurger.click();
        page.waitForTimeout(2000);

        // Navigate back to Inventory page
        page.navigate("https://www.saucedemo.com/inventory.html");

        // Burger Menu by XPath
        Locator burgerMenuButton3 = page.locator("xpath=//*[@id='react-burger-menu-btn']");
        burgerMenuButton3.click();
        page.waitForTimeout(2000);

        // Click on Logout
        Locator logoutBurger = page.locator("[data-test='logout-sidebar-link'] >> text=Logout");
        logoutBurger.click();
        page.waitForTimeout(2000);
        assertTrue(page.url().contains("saucedemo.com"), "Should be redirected to login after logout");

        // Log in again
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        page.waitForTimeout(2000);

        // Reset App State
        Locator burgerMenuButton4 = page.locator("button#react-burger-menu-btn");
        burgerMenuButton4.click();
        page.waitForTimeout(2000);
        Locator resetBurger = page.locator("[data-test='reset-sidebar-link'] >> text=Reset App State");
        resetBurger.click();
        assertTrue(resetBurger.isVisible(), "Reset App State should be visible and clickable");

        // Close Burger Menu
        Locator closeMenuButton = page.locator("#react-burger-cross-btn");
        closeMenuButton.click();
        page.waitForTimeout(2000);


        // Burger Menu by Style or Position (Not Recommended but Possible)
        Locator burgerMenuButton5 = page.locator("button#react-burger-menu-btn:has-text('Open Menu')");
        burgerMenuButton5.click();
        page.waitForTimeout(2000);

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
