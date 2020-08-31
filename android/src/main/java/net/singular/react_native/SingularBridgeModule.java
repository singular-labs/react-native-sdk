package net.singular.react_native;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.app.Application;
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
    private SingularConfig config;
    private SingularLinkHandler singularLinkHandler;

    public SingularBridgeModule(ReactApplicationContext context) {
        super(context);

        reactContext = context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    private SingularConfig parseSingularConfig(String configString) {
        try {
            JSONObject configJson = new JSONObject(configString);

            String apikey = configJson.optString("apikey", null);
            String secret = configJson.optString("secret", null);

            SingularConfig singularConfig = new SingularConfig(apikey, secret);

            long ddlTimeoutSec = configJson.optLong("ddlTimeoutSec", 0);

            if (ddlTimeoutSec > 0) {
                singularConfig.withDDLTimeoutInSec(ddlTimeoutSec);
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

            if (reactContext.hasCurrentActivity() && getCurrentActivity().getApplication() != null) {
                // We register to the lifecycle events to auto detect new intent
                getCurrentActivity().getApplication().registerActivityLifecycleCallbacks(lifecycleCallbacks);

                config.withSingularLink(getCurrentActivity().getIntent(), singularLinkHandler);
            }

            String customUserId = configJson.optString("customUserId", null);

            if (customUserId != null) {
                singularConfig.withCustomUserId(customUserId);
            }

            String imei = configJson.optString("imei", null);

            if (imei != null) {
                singularConfig.withIMEI(imei);
            }

            JSONObject globalProperties = configJson.optJSONObject("globalProperties");

            // Adding all of the global properties to the singular config
            if (globalProperties != null) {
                Iterator<String> iter = globalProperties.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    JSONObject property = globalProperties.getJSONObject(key);

                    singularConfig.withGlobalProperty(property.getString("Key"),
                            property.getString("Value"),
                            property.getBoolean("OverrideExisting"));
                }
            }

            return singularConfig;

        } catch (JSONException ignored) {
        }

        return null;
    }

    @ReactMethod
    public void init(String configJsonString) {
        SingularConfig config = parseSingularConfig(configJsonString);
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

    private Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

            // We want to trigger the singular link handler only if it's registered
            if (config != null && singularLinkHandler != null && activity.getIntent().getData() != null) {
                config.withSingularLink(activity.getIntent(), singularLinkHandler);

                Singular.init(reactContext, config);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
}
