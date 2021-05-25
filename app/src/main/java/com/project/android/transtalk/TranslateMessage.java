package com.project.android.transtalk;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class TranslateMessage {

    private String response;

    public String translate(Context mContext, final String message, final String mCurrentUser, final String mChatUser, String currentUserLanguage, String chatUserLanguage) throws InterruptedException, ExecutionException, JSONException, IOException {

        String API_URL = Util.getProperty("API_URL", mContext) + "key=" + Util.getProperty("API_KEY", mContext) + "&";
        String responseURL = null;
        if (currentUserLanguage.equals(chatUserLanguage)) {
            response = message;
        } else {
            String presentURL = API_URL + "q=" + message + "&target=" + chatUserLanguage;
            responseURL = new AsyncTranslateMessage().execute(presentURL).get();
            if (responseURL != null) {
                JSONObject jsonObject = new JSONObject(responseURL);
                JSONObject dataObject = jsonObject.getJSONObject("data");
                JSONArray translationArray = dataObject.getJSONArray("translations");
                JSONObject translationObject = translationArray.getJSONObject(0);
                response = translationObject.getString("translatedText");
            } else {
                response = message;
            }
        }
        return response;
    }

    public class AsyncTranslateMessage extends AsyncTask<String, Void, String> {

        private Exception exception;
        protected void onPreExecute() {
        }

        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }
            Log.i("INFO", response);
        }
    }
}

