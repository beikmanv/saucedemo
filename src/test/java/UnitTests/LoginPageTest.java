package UnitTests;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // To allow @BeforeAll and @AfterAll to be non-static
public class LoginPageTest extends BeforeAllTestsUnit {
    private static final Logger logger = LoggerFactory.getLogger(LoginPageTest.class);
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @BeforeEach
    void createNewContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void cleanupContext() {
        context.close(); // closes context and page together
    }

    @AfterAll
    void tearDown() {
        browser.close();
        playwright.close();
    }

    @Test
    void testValidLogin() {
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "standard_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        page.waitForTimeout(2000);

        assertTrue(page.url().contains("inventory.html"), "Should redirect to inventory on valid login");
    }

    @Test
    void testInvalidLogin() {
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "wrong_user");
        page.fill("[data-test='password']", "wrong_pass");
        page.click("[data-test='login-button']");
        page.waitForTimeout(2000);

        Locator error = page.locator("[data-test='error']");
        assertTrue(error.isVisible(), "Error message should appear for invalid login");
    }

    @Test
    void testLockedOutUser() {
        page.navigate("https://www.saucedemo.com/");
        page.fill("[data-test='username']", "locked_out_user");
        page.fill("[data-test='password']", "secret_sauce");
        page.click("[data-test='login-button']");
        page.waitForTimeout(2000);

        Locator error = page.locator("[data-test='error']");
        assertTrue(error.isVisible());
        assertTrue(error.innerText().toLowerCase().contains("locked out"), "Should show locked out message");
    }
}
