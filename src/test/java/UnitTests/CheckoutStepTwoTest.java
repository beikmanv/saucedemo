package UnitTests;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CheckoutStepTwoTest extends BeforeAllTestsUnit {

    @Test
    public void testOverviewAndPriceCalculation() {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();

        // Login and go to overview
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        page.waitForTimeout(2000);
        page.locator(".inventory_item .btn_inventory").first().click();
        page.locator(".shopping_cart_link").click();
        page.waitForTimeout(2000);
        page.click("text=Checkout");
        page.waitForTimeout(2000);
        page.fill("[data-test='firstName']", "Valdis");
        page.fill("[data-test='lastName']", "Beikmanis");
        page.fill("[data-test='postalCode']", "CV61DH");
        page.click("[data-test='continue']");
        page.waitForTimeout(2000);

        // Assertions
        assertTrue(page.url().contains("checkout-step-two"));
        assertTrue(page.locator("text=Payment Information").isVisible());
        assertTrue(page.locator("text=Price Total").isVisible());

        browser.close();
        playwright.close();
    }
}
