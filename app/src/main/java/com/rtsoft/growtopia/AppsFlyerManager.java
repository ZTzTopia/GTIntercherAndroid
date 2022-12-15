package com.rtsoft.growtopia;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;

import java.util.HashMap;
import java.util.Map;

public class AppsFlyerManager {
    private static final String Key = "m2TXzMjM53e5MCwGasukoW";
    private final Context baseContext;

    public AppsFlyerManager(Context context) {
        baseContext = context;
    }

    public void Init() {
        AppsFlyerLib.getInstance().init(Key, null, baseContext);
        AppsFlyerLib.getInstance().setDebugLog(false);
    }

    public String GetAppsFlyerId() {
        return AppsFlyerLib.getInstance().getAppsFlyerUID(baseContext);
    }

    public void Start() {
        AppsFlyerLib.getInstance()
            .start(baseContext.getApplicationContext(), Key, new AppsFlyerRequestListener() {
                @Override
                public void onSuccess() {
                    Log.d(
                        "AppsFlyer",
                        "Launch sent successfully, got 200 response code from server"
                    );
                }

                @Override
                public void onError(int i, @NonNull String s) {
                    Log.d(
                        "AppsFlyer",
                        "Launch failed to be sent:\nError code: " + i + "\nError description: " + s
                    );
                }
            });
    }

    private String cleanPrice(String price) {
        return price.trim().replaceAll(",", ".").replace(" ", "");
    }

    public void LogPurchase(String item, String currency, String price) {
        Log.d("Appsflyer", "Starting purchase tracking.");
        Log.d("Appsflyer", "Item:" + item);
        Log.d("Appsflyer", "Currency:" + currency);
        Log.d("Appsflyer", "Price:" + price);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(AFInAppEventParameterName.CONTENT_ID, item);
        hashMap.put(AFInAppEventParameterName.CURRENCY, currency);
        hashMap.put(AFInAppEventParameterName.REVENUE, cleanPrice(price));

        LogEvent(AFInAppEventType.PURCHASE, hashMap);
    }

    public void LogEvent(String eventName, Map<String, Object> eventValues) {
        Log.d("Appsflyer", "Log Event:" + eventName);
        Log.d("Appsflyer", "Value:" + eventValues.toString());

        AppsFlyerLib.getInstance()
            .logEvent(baseContext.getApplicationContext(), eventName, eventValues);

        Log.d("Appsflyer", "Appsflyer even logged");
    }

    public void LogEvent(String eventName, String EventValueStr) {
        Log.d("Appsflyer", "Log Event:" + eventName);
        Log.d("Appsflyer", "EventValueStr:" + EventValueStr);

        HashMap<String, Object> hashMap = new HashMap<>();

        if (eventName.equals("LEVEL_ACHIEVED")) {
            hashMap.put(AFInAppEventParameterName.LEVEL, EventValueStr.split("\\|", 2)[1]);
            eventName = AFInAppEventType.LEVEL_ACHIEVED;
        } else {
            if (EventValueStr.endsWith("\n")) {
                EventValueStr = EventValueStr.substring(0, EventValueStr.length() - 1);
            }

            String[] split = EventValueStr.replace("\n", "|").split("\\|", -1);
            for (int i = 0; i < split.length - (split.length % 2); i += 2) {
                hashMap.put(split[i], split[i + 1]);
            }
        }

        LogEvent(eventName, hashMap);
    }
}