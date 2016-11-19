package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

class JasonerHttpPost {

    static ObjectNode sendPost(String encodedUrl, JsonNode jsonData) throws Exception {

        String url = URLDecoder.decode(encodedUrl,"utf-8");
        URL obj = new URL(url);
        HttpURLConnection con;
        if(url.startsWith("https")) {
            con = (HttpsURLConnection) obj.openConnection();
        } else {
            con = (HttpURLConnection) obj.openConnection();
        }


        //add reuqest header
        con.setRequestMethod("POST");
        String USER_AGENT = "Java 8 application";
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");



        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(Json.stringify(jsonData));
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        ObjectNode result = Json.newObject();
        result.put("status",responseCode);
        result.put("content",response.toString());
        result.put("forwardedTo",url);
        return result;

    }


}
