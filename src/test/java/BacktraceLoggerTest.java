import org.example.BacktraceLogger;
import org.junit.jupiter.api.Test;

public class BacktraceLoggerTest {

    @Test
    public void testSendEvent() {
        BacktraceLogger.sendEventToBacktrace();
    }
}
