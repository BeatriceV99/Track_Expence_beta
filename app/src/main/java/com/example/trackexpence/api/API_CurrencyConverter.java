package com.example.trackexpence.api;
import android.os.AsyncTask;
import android.util.Log;
import android.content.Context;
import android.content.res.Resources;

import com.example.trackexpence.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class API_CurrencyConverter extends AsyncTask<String, Void, String> {

    private static final String TAG = API_CurrencyConverter.class.getCanonicalName();

    private Context PMContext;

    public API_CurrencyConverter(Context pContext) {
        this.PMContext = pContext;
    }

    private static String getApiKey(Context pPMContext) {

        String ApiKey="";

        try {
            Properties properties = new Properties();
            Resources resources = pPMContext.getResources();
            InputStream inputStream = resources.openRawResource(R.raw.apiconfig);
            properties.load(inputStream);
            ApiKey = properties.getProperty("freecurrencyapi_apikey");
        }
        catch (IOException e) {
            Log.e(TAG, "API_CurrencyConverter---Error reading API key from properties file. Error: "+e);
        }

        return ApiKey;
    }


    @Override
    protected String doInBackground(String pParametersArray[]) {
        try {

            String xCurrencyStart=pParametersArray[0];
            String xCurrencyEnd=pParametersArray[1];
            String xApiKey=getApiKey(PMContext);

            if(!xCurrencyStart.isEmpty() && !xCurrencyEnd.isEmpty() && !xApiKey.isEmpty()){
                Log.d(TAG, "API_CurrencyConverter---Start" );
                Log.d(TAG, "API_CurrencyConverter---CurrencyStart: "+xCurrencyStart+" CurrencyEnd: "+xCurrencyEnd);

                URL url = new URL("https://api.freecurrencyapi.com/v1/latest?apikey="+xApiKey+"&base_currency="+xCurrencyStart+"&currencies="+xCurrencyEnd);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                //read response from freecurrencyapi
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                //close connection
                reader.close();
                inputStream.close();
                conn.disconnect();

                return result.toString();
            }

            if(xApiKey.isEmpty()){
                Log.e(TAG, "API_CurrencyConverter---Api key not found" );
            }


        }
        catch (IOException e) {
            Log.e(TAG, "API_CurrencyConverter---Error: "+e);
        }

        return null;
    }
}
