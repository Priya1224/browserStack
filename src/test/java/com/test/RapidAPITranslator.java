package com.test;

import kong.unirest.json.JSONObject;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

public class RapidAPITranslator {

    private static final String API_URL = "https://google-translate113.p.rapidapi.com/api/v1/translator/text";
    private static final String API_KEY = "b9ccc130c9msh4b80fffb84ef16ap10038ejsn796726403ef5"; // Replace with your actual RapidAPI key

    public static String translate(String text, String targetLanguage) throws Exception {
        AsyncHttpClient client = new DefaultAsyncHttpClient();

        String jsonBody = "{\"from\":\"auto\",\"to\":\"" + targetLanguage + "\",\"text\":\"" + text + "\"}";

        String response = client.prepare("POST", API_URL)
                .setHeader("x-rapidapi-key", API_KEY)
                .setHeader("x-rapidapi-host", "google-translate113.p.rapidapi.com")
                .setHeader("Content-Type", "application/json")
                .setBody(jsonBody)
                .execute()
                .toCompletableFuture()
                .thenApply(Response::getResponseBody)
                .join();

        client.close();

        // Parse JSON response
        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("trans")) {
            return jsonResponse.getString("trans");
        } else {
            return "Translation failed: 'translated_text' key not found.";
        }
    }
}

