package UnitTests;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckoutStepOneTest extends BeforeAllTestsUnit {

    @Test
    public void testCheckoutStepOneFormValidation() {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        // Login & add item
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        page.locator(".inventory_item .btn_inventory").first().click();
        page.locator(".shopping_cart_link").click();
        page.waitForTimeout(2000);

        // Begin checkout
        page.click("text=Checkout");
        assertTrue(page.url().contains("checkout-step-one"));
        page.waitForTimeout(2000);

        // Submit without filling
        page.click("[data-test='continue']");
        Locator error = page.locator("[data-test='error']");
        assertTrue(error.isVisible(), "Should show validation error");
        page.waitForTimeout(2000);

        // Fill correctly
        page.fill("[data-test='firstName']", "Valdis");
        page.fill("[data-test='lastName']", "Beikmanis");
        page.fill("[data-test='postalCode']", "CV61DH");
        page.click("[data-test='continue']");
        page.waitForTimeout(2000);

        assertTrue(page.url().contains("checkout-step-two"));

        browser.close();
        playwright.close();
    }
}
