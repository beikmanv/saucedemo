package E2ETests;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.example.pages.LoginPage;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FacebookTwitterTest extends BeforeAllTestsE2E {
    private static final Logger logger = LoggerFactory.getLogger(FacebookTwitterTest.class);

    @Test
    public void testFacebookTwitter() {
        logger.info("Test started");

        // Set up Playwright
        Playwright playwright = Playwright.create();
        Browser browser = playwright.webkit().launch(new BrowserType.LaunchOptions().setHeadless(false));
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

        // Locate by Class (full)
        Locator facebookLink = page.locator("[class='social_facebook']");
        Page facebookTab = page.waitForPopup(() -> {
            facebookLink.click();
        });
        facebookTab.waitForLoadState();
        assertEquals("https://www.facebook.com/saucelabs", facebookTab.url());

        // Locate by combining different selectors
        Locator twitterLink = page.locator("a[data-test='social-twitter'][href='https://twitter.com/saucelabs']");
        Page twitterTab = page.waitForPopup(() -> {twitterLink.click();});
        twitterTab.waitForLoadState();
        assertEquals("https://x.com/saucelabs", twitterTab.url());

        // Locate by Role (on Twitter (X) page)
        page.navigate("https://x.com/saucelabs");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        List<Locator> buttons = page.getByRole(AriaRole.BUTTON).all(); // Example of user-facing attribute (getByRole)
        System.out.println("Found buttons: " + buttons.size());
        Locator followButton1 = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Follow @saucelabs")); // getByRole is preferable when you are following accessibility guidelines
//        Locator followButton1 = page.locator("role=button[name='Follow @saucelabs']"); // Shortened version
        followButton1.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        if (followButton1.isVisible()) {
            System.out.println("Follow button found and is visible.");
            page.locator("div[data-testid='mask']").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
            followButton1.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            BoundingBox box = followButton1.boundingBox(); // The bounding box refers to the area on the screen where the element is located.
            if (box != null) {
                System.out.println("Bounding Box - X: " + box.x);
                System.out.println("Bounding Box - Y: " + box.y);
                System.out.println("Bounding Box - Width: " + box.width);
                System.out.println("Bounding Box - Height: " + box.height);
            } else {
                System.out.println("Bounding Box is null or the element is not visible.");
            }
        } else {
            System.out.println("Follow button not found or not visible.");
        }

        // Locate by Text and Index
        Locator followButtons = page.getByText("Follow"); // Example of user-facing attribute (getByText)
        Locator followButton2 = followButtons.nth(0); // Replace 0 with the appropriate index
        page.locator("div[data-testid='mask']").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        followButton2.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(followButton2.isVisible(), "Follow button should be visible");
        assertTrue(followButton2.isEnabled(), "Follow button should be enabled");
        followButton2.click(new Locator.ClickOptions().setForce(true)); // Use force to bypass blocking issues

        // Locate by combining different selectors
        Locator followText = page.locator("div[dir='ltr']:has-text('Follow Sauce Labs to see what they share on X.')");
        followText.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertTrue(followText.isVisible(), "Follow message should be visible after following @saucelabs");

        // Locate by XPath (not recommended)
        Locator loginLink = page.locator("//*[@id=\"layers\"]/div[2]/div/div/div/div/div/div[2]/div[2]/div/div[2]/div/div[2]/div[2]/a[1]/div");
        page.waitForTimeout(2000);
        loginLink.click();

        // Locate by Class and Index
        Locator googleButton = page.locator("[class='L5Fo6c-bF1uUb']").nth(1);
        googleButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        page.waitForTimeout(2000);
        googleButton.evaluate("element => element.click()");
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
