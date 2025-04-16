import org.example.utils.CleanupVideos;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.nio.file.Paths;

public class BeforeAllTests {

    @BeforeAll
    public static void globalSetup() {
        try {
            CleanupVideos.deleteVideos(Paths.get("videos"));
            System.out.println("All old videos cleaned up before tests.");
        } catch (IOException e) {
            System.err.println("Failed to clean up videos: " + e.getMessage());
        }
    }
}
