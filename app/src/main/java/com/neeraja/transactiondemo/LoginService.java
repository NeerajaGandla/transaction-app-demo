package com.neeraja.transactiondemo;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.neeraja.transactiondemo.data.LoginResponse;
import com.neeraja.transactiondemo.utils.ApiUtils;
import com.neeraja.transactiondemo.utils.Constants;
import com.neeraja.transactiondemo.utils.CustomException;
import com.neeraja.transactiondemo.utils.HttpRequest;
import com.neeraja.transactiondemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginService extends IntentService {
    private static final String TAG = "LoginService";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public LoginService() {
        super("LoginService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = getSharedPreferences(Constants.myPref, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        login();
    }

    private void login() {
        try {
            JSONObject response = (JSONObject) HttpRequest.postData(ApiUtils.getLoginUrl(),null,JSONObject.class, getApplicationContext());

            if (response != null) {
                String token = response.getString(Constants.TOKEN);
                if (Utils.isValidString(token)) {
                    editor.putString(Constants.TOKEN, token);
                    editor.commit();
                }
            }
        } catch (CustomException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
