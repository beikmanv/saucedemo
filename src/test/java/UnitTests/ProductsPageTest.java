package UnitTests;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductsPageTest extends BeforeAllTestsUnit {
    static Playwright playwright;
    static Browser browser;
    static BrowserContext context;
    static Page page;

    @BeforeAll
    static void setUpOnce() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();

        // Login just once for all tests
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        assertTrue(page.url().contains("inventory.html"), "Login failed or did not redirect to inventory");
    }

    @AfterAll
    static void tearDownOnce() {
        browser.close();
        playwright.close();
    }

    @Test
    void testProductCount() {
        Locator items = page.locator(".inventory_item");
        assertEquals(6, items.count(), "Expected 6 items on inventory page");
        page.waitForTimeout(2000);
    }

    @Test
    void testDropdownSorting() {
        Locator dropdown = page.locator("[data-test='product-sort-container']");
        dropdown.selectOption("lohi");
        assertEquals("lohi", dropdown.inputValue());
        page.waitForTimeout(2000);
    }
}
