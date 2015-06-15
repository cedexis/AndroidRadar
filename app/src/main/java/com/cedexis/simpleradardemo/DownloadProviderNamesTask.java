package com.cedexis.simpleradardemo;

import android.os.AsyncTask;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DownloadProviderNamesTask extends AsyncTask<Pair<Integer, Integer>, Void, JSONObject> {

    private Caller _caller = null;

    public DownloadProviderNamesTask(Caller _caller) {
        this._caller = _caller;
    }

    public interface Caller {
        public void onPostExecute(JSONObject providerNames);
    }

    @Override
    protected JSONObject doInBackground(Pair<Integer, Integer>... params) {
        StringBuilder urlBuilder = new StringBuilder("http://radar.cedexis.com");
        urlBuilder.append("/");
        urlBuilder.append(params[0].first);
        urlBuilder.append("/");
        urlBuilder.append(params[0].second);
        urlBuilder.append("/radar/");
        urlBuilder.append(new Date().getTime());
        urlBuilder.append("/providerNames.json");
        try {
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder source = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                source.append(line);
            }
            return new JSONObject(source.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        _caller.onPostExecute(jsonObject);
    }
}
