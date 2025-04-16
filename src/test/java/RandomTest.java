import com.microsoft.playwright.*;
import org.example.pages.LoginPage;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;

public class RandomTest {
    // This tells SLF4J to create a logger that tags all log messages with the class name RandomTest.
    // It helps you identify which class the log message came from.
    private static final Logger logger = LoggerFactory.getLogger(RandomTest.class);

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

        // Create a new page
        Page page = context.newPage();
        logger.info("Logging in...");

        if (!page.url().contains("inventory")) {
            logger.error("Login did not redirect to inventory. Current URL: {}", page.url());
        }



    }
}
