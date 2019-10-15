package com.neeraja.transactiondemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.UnknownHostException;

public class HttpRequest {
    public static final String CONTENT_TYPE = "application/json";
    public static SharedPreferences sharedPreferences;

    public static Object getInputStreamFromUrl(String url, Class classOfT,
                                               Context context) throws CustomException {
        Utils.logD("URL : " + url);
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpclient = new DefaultHttpClient();
            httpGet.addHeader("Content-type", CONTENT_TYPE);
            httpGet.addHeader("Accept-Encoding", "gzip");
            sharedPreferences = context.getSharedPreferences(Constants.myPref, Context.MODE_PRIVATE);
            String token = sharedPreferences.getString(Constants.TOKEN, null);
            if (Utils.isValidString(token)) {
                Utils.logD("TOKEN : " + token);
                httpGet.addHeader(Constants.authorization, token);
            }

            Utils.logD("Log 2");
            HttpResponse response = httpclient.execute(httpGet);

            Utils.logD("Log 3");
            Utils.logD("Log 3");
            Header contentEncoding = response
                    .getFirstHeader("Content-Encoding");
            boolean isGzip = false;
            if (contentEncoding != null
                    && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                isGzip = true;
            }
            return Utils.parse(response.getEntity().getContent(), classOfT,
                    isGzip);

        } catch (UnknownHostException e) {
            Globals.lastErrMsg = Constants.SERVER_NOT_REACHABLE;
            throw new CustomException("", Constants.PROB_WITH_SERVER);
        } catch (Exception e) {
            Utils.logD(e.getMessage());
            Globals.lastErrMsg = Constants.DEVICE_CONNECTIVITY;
            throw new CustomException(Constants.DEVICE_CONNECTIVITY, "");
        }
    }

    public static Object postData(String restAPIPath, String data, Class classOfT, Context context)
            throws CustomException {

        Utils.logD("URL : " + restAPIPath);
        Utils.logD("Data : " + data);

        try {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(restAPIPath);
            sharedPreferences = context.getSharedPreferences(Constants.myPref, Context.MODE_PRIVATE);
            String token = sharedPreferences.getString(Constants.TOKEN, null);
            if (Utils.isValidString(token))
                httpPost.addHeader(Constants.authorization, token);

            httpPost.setEntity(new ByteArrayEntity(data.getBytes("UTF8")));

            httpPost.addHeader("Content-type", CONTENT_TYPE);
            httpPost.addHeader("Content-Encoding", "gzip");

            HttpResponse response = httpclient.execute(httpPost);

            int statusCode = response.getStatusLine().getStatusCode();

            Utils.logD("StatusCode : " + statusCode);
            if (statusCode != 200) {

                if (statusCode > 400 && statusCode < 500) {
                    Globals.lastErrMsg = Constants.SERVER_NOT_REACHABLE;
                    throw new CustomException("", Constants.PROB_WITH_SERVER);
                } else if (statusCode >= 500) {
                    Globals.lastErrMsg = Constants.PROB_WITH_SERVER;
                    throw new CustomException("", Constants.SERVER_NOT_REACHABLE);
                }

                return null;

            } else {
                Header contentEncoding = response
                        .getFirstHeader("Content-Encoding");
                boolean isGzip = false;
                if (contentEncoding != null
                        && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
                    isGzip = true;
                }
                return Utils.parse(response.getEntity().getContent(), classOfT, isGzip);
            }

        } catch (Exception e) {
            Globals.lastErrMsg = "Server Unavailable while sending data";
            throw new CustomException(Globals.lastErrMsg, "");
        }

    }

}
