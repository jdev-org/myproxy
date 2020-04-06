package fr.landel.myproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Download {

    public static void main(String[] args) {

        String urlString = "http://www.vbc3.com/script/unicode_octal_html_code.php";

        // Create the URL
        try {
            URL remoteURL = new URL(urlString);

            // Create a connection to remote server
            HttpURLConnection proxyToServerCon = (HttpURLConnection) remoteURL.openConnection();
            proxyToServerCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            proxyToServerCon.setRequestProperty("Content-Language", "en-US");
            proxyToServerCon.setUseCaches(false);
            proxyToServerCon.setDoOutput(true);

            // Create Buffered Reader from remote Server
            try (BufferedReader proxyToServerBR = new BufferedReader(new InputStreamReader(proxyToServerCon.getInputStream()))) {

                // Send success code to client
                String line;

                for (Entry<String, List<String>> entry : proxyToServerCon.getHeaderFields().entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue().stream().collect(Collectors.joining(", ")));
                }

                // Read from input stream between proxy and remote server
                while ((line = proxyToServerBR.readLine()) != null) {
                    // Send on data to client
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
