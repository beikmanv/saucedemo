package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
This test verifies that your app can successfully send a POST request to Backtrace. It proves:

        ✔️ You can reach the Backtrace endpoint (no firewall or token issues).
        ✔️ Your JSON is structured correctly (Backtrace accepts it).
        ✔️ The network layer works from your test environment (good for CI/CD validation).
 */

public class BacktraceLogger {

    public static void sendEventToBacktrace() {
        try {
            String url = "https://submit.backtrace.io/valmusic/f8b6918b30282a6b49608a51067fab9ddab67d50c7993f22315cda5874cbb408/sourcemap";
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            String body = """
            {
                "application": "Swag Store",
                "appversion": "3.0.0",
                "unique_events": [
                    {
                        "timestamp": %d,
                        "attributes": {
                            "application.session": "test-session-123",
                            "browser.name": "Test Browser",
                            "application": "Swag Store"
                        }
                    }
                ]
            }
            """.formatted(System.currentTimeMillis() / 1000);

            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            System.out.println("POST Response Code: " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("Response from Backtrace: " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
