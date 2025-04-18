package UnitTests;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckoutCompleteTest extends BeforeAllTestsUnit {

    @Test
    public void testFinishAndOrderComplete() {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        // Login and go through full flow
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        page.locator(".inventory_item .btn_inventory").first().click();
        page.locator(".shopping_cart_link").click();
        page.click("text=Checkout");
        page.fill("[data-test='firstName']", "Valdis");
        page.fill("[data-test='lastName']", "Beikmanis");
        page.fill("[data-test='postalCode']", "CV61DH");
        page.waitForTimeout(2000);
        page.click("[data-test='continue']");
        page.waitForTimeout(2000);
        page.click("[data-test='finish']");
        page.waitForTimeout(2000);

        // Verify complete screen
        assertTrue(page.url().contains("checkout-complete.html"));
        assertTrue(page.locator(".complete-header").isVisible(), "Order complete message should show");

        browser.close();
        playwright.close();
    }
}
