package net.singular.react_native;

import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.singular.sdk.SDIDAccessorHandler;
import com.singular.sdk.Singular;
import com.singular.sdk.SingularConfig;
import com.singular.sdk.SingularDeviceAttributionHandler;
import com.singular.sdk.SingularLinkHandler;
import com.singular.sdk.SingularLinkParams;
import com.singular.sdk.ShortLinkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SingularBridgeModule extends ReactContextBaseJavaModule {
    private interface Constants {
        String SINGULAR_LINK_HANDLER_CONST = "SingularLinkHandler";
        String DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST = "DeviceAttributionCallbackHandler";
        String SHORT_LINK_HANDLER_CONST = "ShortLinkHandler";
    }

    public static final String REACT_CLASS = "SingularBridge";
    private static ReactApplicationContext reactContext = null;
    private static SingularConfig config;
    private static SingularLinkHandler singularLinkHandler;
    private static int currentIntentHash;

    private static String[][] pushNotificationsLinkPaths;

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

            JSONArray espDomains = configJson.optJSONArray("espDomains");
            if (espDomains != null && espDomains.length() >0){
                List<String> domainsList = new LinkedList<>();
                for (int i = 0 ; i < espDomains.length() ; i++){
                    String domain = espDomains.getString(i);
                    if (domain != null && domain.length() >0){
                        domainsList.add(domain);
                    }
                }
                config.withESPDomains(domainsList);
            }

            long ddlTimeoutSec = configJson.optLong("ddlTimeoutSec", 0);
            if (ddlTimeoutSec > 0) {
                config.withDDLTimeoutInSec(ddlTimeoutSec);
            }

            config.withSingularDeviceAttribution(new SingularDeviceAttributionHandler() {
                @Override
                public void onDeviceAttributionInfoReceived(Map<String, Object> deviceAttributionData) {
                    try {
                        WritableMap attributionInfo = convertJsonToWritableMap(new JSONObject(deviceAttributionData));

                        // Raising the Device attribution handler in the react-native code

                        reactContext.
                                getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(Constants.DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST, attributionInfo);
                    } catch (Throwable e) {
                        Log.d("Singular", "could not convert json to writable map");
                    }
                }
            });

            singularLinkHandler = new SingularLinkHandler() {
                @Override
                public void onResolved(SingularLinkParams singularLinkParams) {
                    WritableMap params = Arguments.createMap();
                    params.putString("deeplink", singularLinkParams.getDeeplink());
                    params.putString("passthrough", singularLinkParams.getPassthrough());
                    params.putBoolean("isDeferred", singularLinkParams.isDeferred());

                    WritableMap urlParams = Arguments.createMap();
                    if (singularLinkParams.getUrlParameters() != null) {
                        for (Map.Entry<String,String> entry : singularLinkParams.getUrlParameters().entrySet()) {
                            urlParams.putString(entry.getKey(), entry.getValue());
                        }
                    }

                    params.putMap("urlParameters", urlParams);

                    // Raising the Singular Link handler in the react-native code
                    reactContext.
                            getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(Constants.SINGULAR_LINK_HANDLER_CONST, params);
                }
            };

            JSONArray pushNotificationLinkPaths = configJson.optJSONArray("pushNotificationsLinkPaths");
            String[][] pushSelectors = convertTo2DArray(pushNotificationLinkPaths);

            if (pushSelectors != null) {
                pushNotificationsLinkPaths = pushSelectors;
            }

            if (reactContext.hasCurrentActivity()) {
                Intent intent = getCurrentActivity().getIntent();
                if (intent != null) {
                    int intentHash = intent.hashCode();

                    if (intentHash != currentIntentHash) {
                        currentIntentHash = intentHash;

                        long shortLinkResolveTimeout = configJson.optLong("shortLinkResolveTimeout", 10);
                        config.withSingularLink(getCurrentActivity().getIntent(), singularLinkHandler, shortLinkResolveTimeout);

                        if (intent.getExtras() != null && intent.getExtras().size() > 0) {
                            if (pushNotificationsLinkPaths != null) {
                                config.withPushNotificationPayload(intent, pushNotificationsLinkPaths);
                            }
                        }
                    }
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

            boolean limitedIdentifiersEnabled = configJson.optBoolean("limitedIdentifiersEnabled", false);
            if (limitedIdentifiersEnabled) {
                config.withLimitedIdentifiersEnabled();
            }

            int logLevel = configJson.optInt("logLevel", -1);
            if (logLevel >= 0) {
                config.withLogLevel(logLevel);
            }

            String facebookAppId = configJson.optString("facebookAppId", null);
            if (facebookAppId != null) {
                config.withFacebookAppId(facebookAppId);
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

            String customSdid = configJson.optString("customSdid");
            if (!isValidNonEmptyString(customSdid)) {
                customSdid = null;
            }

            config.withCustomSdid(customSdid, new SDIDAccessorHandler() {
                @Override
                public void didSetSdid(String result) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("DidSetSdidCallback", result);
                }

                @Override
                public void sdidReceived(String result) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("SdidReceivedCallback", result);
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private String[][] convertTo2DArray(JSONArray jsonArray) {
        try {
            if (jsonArray == null || jsonArray.length() <= 0) {
                return null;
            }

            String[][] result = new String[jsonArray.length()][];

            for (int outerIndex = 0; outerIndex < jsonArray.length(); outerIndex++) {
                JSONArray innerArray = jsonArray.getJSONArray(outerIndex);
                result[outerIndex] = new String[innerArray.length()];
                for (int innerIndex = 0; innerIndex < innerArray.length(); innerIndex++) {
                    result[outerIndex][innerIndex] = innerArray.getString(innerIndex);
                }
            }

            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    private WritableMap convertJsonToWritableMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = Arguments.createMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                map.putMap(key, convertJsonToWritableMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                map.putArray(key, convertJsonToArray((JSONArray) value));
            } else if (value instanceof Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String) {
                map.putString(key, (String) value);
            } else {
                map.putString(key, value.toString());
            }
        }

        return map;
    }

    private WritableArray convertJsonToArray(JSONArray jsonArray) throws JSONException {
        WritableArray array = new WritableNativeArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                array.pushMap(this.convertJsonToWritableMap((JSONObject) value));
            } else if (value instanceof JSONArray) {
                array.pushArray(convertJsonToArray((JSONArray) value));
            } else if (value instanceof Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof String) {
                array.pushString((String) value);
            } else {
                array.pushString(value.toString());
            }
        }

        return array;
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

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return args;
    }

    public static void onNewIntent(Intent intent) {
        if(intent == null) {
            return;
        }

        if (config == null) {
            return;
        }

        if (intent.hashCode() == currentIntentHash) {
            return;
        }

        currentIntentHash = intent.hashCode();

        if (intent.getData() != null && singularLinkHandler != null) {
            // We want to trigger the singular link handler only if it's registered
            config.withSingularLink(intent, singularLinkHandler);
        }

        if (intent.getExtras() != null && intent.getExtras().size() > 0 && pushNotificationsLinkPaths != null && pushNotificationsLinkPaths.length > 0) {
            config.withPushNotificationPayload(intent, pushNotificationsLinkPaths);
        }

        Singular.init(reactContext, config);
    }

    @ReactMethod
    public void createReferrerShortLink(String baseLink,
                                        String referrerName,
                                        String referrerId,
                                        String args){

        JSONObject params = null;
        try{
            params = new JSONObject(args);
        }catch (JSONException e){
            e.printStackTrace();
        }

        Singular.createReferrerShortLink(baseLink,
                referrerName,
                referrerId,
                params,
                new ShortLinkHandler() {
                    @Override
                    public void onSuccess(final String link) {
                        WritableMap params = Arguments.createMap();
                        params.putString("data", link);
                        params.putString("error", "");
                        reactContext.
                                getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(Constants.SHORT_LINK_HANDLER_CONST, params);

                    }

                    @Override
                    public void onError(final String error) {
                        WritableMap params = Arguments.createMap();
                        params.putString("data", "");
                        params.putString("error", error);
                        reactContext.
                                getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(Constants.SHORT_LINK_HANDLER_CONST, params);
                    }
                });
    }

    private boolean isValidNonEmptyString(String nullableJavascriptString) {
        return nullableJavascriptString != null
                && nullableJavascriptString instanceof String
                && nullableJavascriptString.length() > 0
                && !nullableJavascriptString.toLowerCase().equals("null")
                && !nullableJavascriptString.toLowerCase().equals("undefined")
                && !nullableJavascriptString.toLowerCase().equals("false")
                && !nullableJavascriptString.equals("NaN");
    }
    
    @ReactMethod
    public void addListener(String eventName) {
        // Keep: Required for RN built in Event Emitter Calls.
    }
    
    @ReactMethod
    public void removeListeners(Integer count) {
        // Keep: Required for RN built in Event Emitter Calls.
    }
}
