package net.singular.react_native;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.singular.sdk.Singular;
import com.singular.sdk.SingularConfig;
import com.singular.sdk.SingularLinkHandler;
import com.singular.sdk.SingularLinkParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SingularBridgeModule extends ReactContextBaseJavaModule {
    public static final String REACT_CLASS = "SingularBridge";
    private static ReactApplicationContext reactContext = null;
    private static SingularConfig config;
    private static SingularLinkHandler singularLinkHandler;
    private static int currentIntentHash;

    public SingularBridgeModule(ReactApplicationContext context) {
        super(context);

        reactContext = context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void init(String configJsonString) {
        buildSingularConfig(configJsonString);
        Singular.init(reactContext, config);
    }

    @ReactMethod
    public void setCustomUserId(String customUserId) {
        Singular.setCustomUserId(customUserId);
    }

    @ReactMethod
    public void unsetCustomUserId() {
        Singular.unsetCustomUserId();
    }

    @ReactMethod
    public void setDeviceCustomUserId(String customUserId) {
        Singular.setDeviceCustomUserId(customUserId);
    }

    @ReactMethod
    public void event(String name) {
        Singular.event(name);
    }

    @ReactMethod
    public void eventWithArgs(String name, String extra) {
        Singular.event(name, extra);
    }

    @ReactMethod
    public void revenue(String currency, double amount) {
        Singular.revenue(currency, amount);
    }

    @ReactMethod
    public void revenueWithArgs(String currency, double amount, String args) {
        Singular.revenue(currency, amount, convertJsonToMap(args));
    }

    @ReactMethod
    public void customRevenue(String eventName, String currency, double amount) {
        Singular.customRevenue(eventName, currency, amount);
    }

    @ReactMethod
    public void customRevenueWithArgs(String eventName, String currency, double amount, String args) {
        Singular.customRevenue(eventName, currency, amount, convertJsonToMap(args));
    }

    @ReactMethod
    public void setUninstallToken(String token) {
        Singular.setFCMDeviceToken(token);
    }

    @ReactMethod
    public void trackingOptIn() {
        Singular.trackingOptIn();
    }

    @ReactMethod
    public void trackingUnder13() {
        Singular.trackingUnder13();
    }

    @ReactMethod
    public void stopAllTracking() {
        Singular.stopAllTracking();
    }

    @ReactMethod
    public void resumeAllTracking() {
        Singular.resumeAllTracking();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public boolean isAllTrackingStopped() {
        return Singular.isAllTrackingStopped();
    }

    @ReactMethod
    public void limitDataSharing(boolean limitDataSharingValue) {
        Singular.limitDataSharing(limitDataSharingValue);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public boolean getLimitDataSharing() {
        return Singular.getLimitDataSharing();
    }

    @ReactMethod
    public void setReactSDKVersion(String wrapper, String version) {
        Singular.setWrapperNameAndVersion(wrapper, version);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public boolean setGlobalProperty(String key, String value, boolean overrideExisting) {
        return Singular.setGlobalProperty(key,value,overrideExisting);
    }

    @ReactMethod
    public void unsetGlobalProperty(String key) {
        Singular.unsetGlobalProperty(key);
    }

    @ReactMethod
    public void clearGlobalProperties() {
        Singular.clearGlobalProperties();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public WritableMap getGlobalProperties() {
        return toWritableMap(Singular.getGlobalProperties());
    }

    private void buildSingularConfig(String configString) {
        try {
            JSONObject configJson = new JSONObject(configString);

            String apikey = configJson.optString("apikey", null);
            String secret = configJson.optString("secret", null);

            config = new SingularConfig(apikey, secret);

            long ddlTimeoutSec = configJson.optLong("ddlTimeoutSec", 0);

            if (ddlTimeoutSec > 0) {
                config.withDDLTimeoutInSec(ddlTimeoutSec);
            }

            singularLinkHandler = new SingularLinkHandler() {
                @Override
                public void onResolved(SingularLinkParams singularLinkParams) {

                    WritableMap params = Arguments.createMap();
                    params.putString("deeplink", singularLinkParams.getDeeplink());
                    params.putString("passthrough", singularLinkParams.getPassthrough());
                    params.putBoolean("isDeferred", singularLinkParams.isDeferred());

                    // Raising the Singular Link handler in the react-native code
                    reactContext.
                            getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("SingularLinkHandler", params);
                }
            };

            if (reactContext.hasCurrentActivity() && getCurrentActivity().getIntent() != null) {
                int intentHash = getCurrentActivity().getIntent().hashCode();

                if (intentHash != currentIntentHash) {
                    currentIntentHash = intentHash;

                    long shortLinkResolveTimeout = configJson.optLong("shortLinkResolveTimeout", 10);
                    config.withSingularLink(getCurrentActivity().getIntent(), singularLinkHandler, shortLinkResolveTimeout);
                }
            }

            String customUserId = configJson.optString("customUserId", null);

            if (customUserId != null) {
                config.withCustomUserId(customUserId);
            }

            String imei = configJson.optString("imei", null);

            if (imei != null) {
                config.withIMEI(imei);
            }

            int sessionTimeout = configJson.optInt("sessionTimeout", -1);

            if (sessionTimeout >= 0) {
                config.withSessionTimeoutInSec(sessionTimeout);
            }

            Object limitDataSharing = configJson.opt("limitDataSharing");

            if (limitDataSharing != JSONObject.NULL) {
                config.withLimitDataSharing((boolean)limitDataSharing);
            }

            boolean collectOAID = configJson.optBoolean("collectOAID", false);
            if (collectOAID) {
                config.withOAIDCollection();
            }

            boolean enableLogging = configJson.optBoolean("enableLogging", false);
            if (enableLogging) {
                config.withLoggingEnabled();
            }

            JSONObject globalProperties = configJson.optJSONObject("globalProperties");

            // Adding all of the global properties to the singular config
            if (globalProperties != null) {
                Iterator<String> iter = globalProperties.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    JSONObject property = globalProperties.getJSONObject(key);

                    config.withGlobalProperty(property.getString("Key"),
                            property.getString("Value"),
                            property.getBoolean("OverrideExisting"));
                }
            }
        } catch (JSONException ignored) {
        }
    }

    private WritableMap toWritableMap(Map<String, String> map) {
        WritableMap writableMap = Arguments.createMap();
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            Object value = pair.getValue();

            if (value == null) {
                writableMap.putNull((String) pair.getKey());
            }  else if (value instanceof String) {
                writableMap.putString((String) pair.getKey(), (String) value);
            }
            iterator.remove();
        }

        return writableMap;
    }


    private Map<String, Object> convertJsonToMap(String json) {
        Map<String, Object> args = new HashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();

                args.put(key, jsonObject.get(key));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return args;
    }

    public static void onNewIntent(Intent intent) {
        if(intent == null){
            return;
        }

        // We want to trigger the singular link handler only if it's registered
        if (config != null &&
                singularLinkHandler != null &&
                intent.hashCode() != currentIntentHash &&
                intent.getData() != null) {
            currentIntentHash = intent.hashCode();
            config.withSingularLink(intent, singularLinkHandler);
            Singular.init(reactContext, config);
        }
    }
}
