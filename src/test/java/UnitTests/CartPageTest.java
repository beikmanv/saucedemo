package UnitTests;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;

public class CartPageTest extends BeforeAllTestsUnit {
    private static final Logger logger = LoggerFactory.getLogger(CartPageTest.class);

    @Test
    public void testAddToCartAndCartPage() {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();
        page.waitForTimeout(2000);

        // Login and add item
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        page.locator(".inventory_item .btn_inventory").first().click();
        page.waitForTimeout(2000);

        // Go to cart
        page.locator(".shopping_cart_link").click();
        assertTrue(page.url().contains("cart.html"));
        page.waitForTimeout(2000);

        // Check cart item is visible
        Locator cartItem = page.locator(".cart_item");
        assertEquals(1, cartItem.count(), "Cart should have 1 item");

        browser.close();
        playwright.close();
    }
}
