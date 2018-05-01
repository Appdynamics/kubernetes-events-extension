package com.appdynamics.monitors.kubernetes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class EventsRestOperation {
    private static final Logger logger = LoggerFactory.getLogger(EventsRestOperation.class);

    static JsonNode doRequest(URL url, String accountName, String apiKey, String requestBody, String method){
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
            if (method.equals("POST")) {
                conn.setRequestProperty("Content-Type", "application/vnd.appd.events+json;v=2");
            }
            conn.setRequestProperty("Accept", "application/vnd.appd.events+json;v=2");
            conn.setRequestProperty("X-Events-API-AccountName", accountName);
            conn.setRequestProperty("X-Events-API-Key", apiKey);
            if (method.equals("POST")){
                OutputStream output = conn.getOutputStream();
                output.write(requestBody.getBytes("UTF-8"));
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String response = "";
            //noinspection StatementWithEmptyBody
            for (String line; (line = br.readLine()) != null; response += line);
            conn.disconnect();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response);
        } catch (IOException e) {
            logger.error("Error while processing {} on URL {} with Body {}", method, url, requestBody);
            return null;
        }
    }
}
