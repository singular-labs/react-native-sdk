package net.singular.react_native;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import android.content.Intent;
import org.json.JSONObject;
import org.json.JSONArray;

import com.singular.sdk.Singular;
import com.singular.sdk.SingularConfig;
import com.singular.sdk.SingularDeviceAttributionHandler;
import com.singular.sdk.SingularLinkHandler;
import com.singular.sdk.SingularLinkParams;
import com.singular.sdk.SDIDAccessorHandler;
import com.singular.sdk.ShortLinkHandler;


public class SingularBridgeModule extends NativeSingularSpec {
    public static final String NAME = "SingularBridge";
    private static final String version = "4.0.0";
    private static final String wrapper = "ReactNative";

    private static SingularConfig config;
    private static int currentIntentHash;
    private long shortLinkResolveTimeout = 10;

    private static String[][] pushNotificationsLinkPaths;
    private static SingularLinkHandler staticSingularLinkHandler;
    private static ReactApplicationContext reactContext;

    private static final JSONObject EMPTY_JSON_OBJECT = new JSONObject();
    private static final JSONArray EMPTY_JSON_ARRAY = new JSONArray();

    private interface Constants {
        // Config key constants
        String CONFIG_KEY_APIKEY = "apikey";
        String CONFIG_KEY_SECRET = "secret";
        String CONFIG_KEY_ESP_DOMAINS = "espDomains";
        String CONFIG_KEY_SESSION_TIMEOUT = "sessionTimeout";
        String CONFIG_KEY_DDL_TIMEOUT_SEC = "ddlTimeoutSec";
        String CONFIG_KEY_SHORT_LINK_RESOLVE_TIMEOUT = "shortLinkResolveTimeout";
        String CONFIG_KEY_CUSTOM_USER_ID = "customUserId";
        String CONFIG_KEY_IMEI = "imei";
        String CONFIG_KEY_LIMIT_DATA_SHARING = "limitDataSharing";
        String CONFIG_KEY_COLLECT_OAID = "collectOAID";
        String CONFIG_KEY_ENABLE_LOGGING = "enableLogging";
        String CONFIG_KEY_LIMIT_ADVERTISING_IDENTIFIERS = "limitAdvertisingIdentifiers";
        String CONFIG_KEY_LOG_LEVEL = "logLevel";
        String CONFIG_KEY_FACEBOOK_APP_ID = "facebookAppId";
        String CONFIG_KEY_GLOBAL_PROPERTIES = "globalProperties";
        String CONFIG_KEY_CUSTOM_SDID = "customSdid";
        String CONFIG_KEY_BRANDED_DOMAINS = "brandedDomains";
        String CONFIG_KEY_PUSH_NOTIFICATIONS_LINK_PATHS = "pushNotificationsLinkPaths";
        
        // Global properties key constants
        String GLOBAL_PROP_KEY = "Key";
        String GLOBAL_PROP_VALUE = "Value";
        String GLOBAL_PROP_OVERRIDE_EXISTING = "OverrideExisting";
        
        // Ad Revenue key constants
        interface AdRevenue {
            String AD_REVENUE_EVENT = "__ADMON_USER_LEVEL_REVENUE__";
            String AD_PLATFORM = "ad_platform";
            String AD_CURRENCY = "ad_currency";
            String AD_REVENUE = "ad_revenue";
            String R = "r";
            String PCC = "pcc";
            String IS_ADMON_REVENUE = "is_admon_revenue";
            String IS_REVENUE_EVENT = "is_revenue_event";
            String AD_MEDIATION_PLATFORM = "ad_mediation_platform";
            String AD_TYPE = "ad_type";
            String AD_GROUP_TYPE = "ad_group_type";
            String AD_IMPRESSION_ID = "ad_impression_id";
            String AD_PLACEMENT_NAME = "ad_placement_name";
            String AD_UNIT_ID = "ad_unit_id";
            String AD_UNIT_NAME = "ad_unit_name";
            String AD_GROUP_ID = "ad_group_id";
            String AD_GROUP_NAME = "ad_group_name";
            String AD_GROUP_PRIORITY = "ad_group_priority";
            String AD_PRECISION = "ad_precision";
            String AD_PLACEMENT_ID = "ad_placement_id";
        }
    }

    private String convertToJsonString(ReadableMap map) {
        if (map == null) return null;
        try {
            return convertReadableMapToJSONObject(map).toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    public SingularBridgeModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public void init(ReadableMap configMap) {
        try {
            config = buildSingularConfigFromReadableMap(configMap);
            if (config != null) {
                SingularHelper.initWithSingularConfig(reactContext, config);
                SingularHelper.setReactSDKVersion(wrapper, version);
            }
        } catch (Exception e) {
            Log.e("SingularSDK", "Error during init", e);
        }
    }

    // Required for event emission in TurboModules
    @Override
    public void addListener(String eventType) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @Override
    public void removeListeners(double count) {
        // Keep: Required for RN built in Event Emitter Calls.
    }

    @Override
    public void event(String eventName) {
        SingularHelper.event(eventName);
    }

    @Override
    public void eventWithArgs(String eventName, ReadableMap args) {
        String argsString = convertToJsonString(args);
        SingularHelper.eventWithArgs(eventName, argsString);
    }

    @Override
    public void setCustomUserId(String customUserId) {
        SingularHelper.setCustomUserId(customUserId);
    }

    @Override
    public void unsetCustomUserId() {
        SingularHelper.unsetCustomUserId();
    }

    @Override
    public void setDeviceCustomUserId(String customUserId) {
        SingularHelper.setDeviceCustomUserId(customUserId);
    }

    @Override
    public void revenue(String currency, double amount) {
        SingularHelper.revenue(currency, amount);
    }

    @Override
    public void revenueWithArgs(String currency, double amount, ReadableMap args) {
        Map<String, Object> argsMap = convertReadableMapToMap(args);
        SingularHelper.revenueWithArgs(currency, amount, argsMap);
    }

    @Override
    public void customRevenue(String eventName, String currency, double amount) {
        SingularHelper.customRevenue(eventName, currency, amount);
    }

    @Override
    public void customRevenueWithArgs(String eventName, String currency, double amount, ReadableMap args) {
        Map<String, Object> argsMap = convertReadableMapToMap(args);
        SingularHelper.customRevenueWithArgs(eventName, currency, amount, argsMap);
    }

    @Override
    public void inAppPurchase(String eventName, ReadableMap purchase) {
        if (purchase == null) return;

        try {
            Map<String, Object> purchaseValues = getPurchaseValues(purchase);
            if (purchaseValues != null && !purchaseValues.isEmpty()) {
                JSONObject jsonObject = new JSONObject(purchaseValues);
                SingularHelper.eventWithArgs(eventName, jsonObject.toString());
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void inAppPurchaseWithArgs(String eventName, ReadableMap purchase, ReadableMap args) {
        if (purchase == null) return;

        try {
            Map<String, Object> purchaseValues = getPurchaseValues(purchase);
            if (purchaseValues == null || purchaseValues.isEmpty()) return;

            if (args != null) {
                Map<String, Object> argsMap = convertReadableMapToMap(args);
                if (argsMap != null && !argsMap.isEmpty()) {
                    purchaseValues.putAll(argsMap);
                }
            }

            JSONObject jsonObject = new JSONObject(purchaseValues);
            SingularHelper.eventWithArgs(eventName, jsonObject.toString());
        } catch (Exception e) {
        }
    }

    @Override
    public void setUninstallToken(String token) {
        SingularHelper.setUninstallToken(token);
    }

    @Override
    public void trackingOptIn() {
        SingularHelper.trackingOptIn();
    }

    @Override
    public void trackingUnder13() {
        SingularHelper.trackingUnder13();
    }

    @Override
    public void stopAllTracking() {
        SingularHelper.stopAllTracking();
    }

    @Override
    public void resumeAllTracking() {
        SingularHelper.resumeAllTracking();
    }

    @Override
    public boolean isAllTrackingStopped() {
        return SingularHelper.isAllTrackingStopped();
    }

    @Override
    public void limitDataSharing(boolean shouldLimitDataSharing) {
        SingularHelper.limitDataSharing(shouldLimitDataSharing);
    }

    @Override
    public boolean getLimitDataSharing() {
        return SingularHelper.getLimitDataSharing();
    }

    @Override
    public boolean setGlobalProperty(String key, String value, boolean overrideExisting) {
        return SingularHelper.setGlobalProperty(key, value, overrideExisting);
    }

    @Override
    public void unsetGlobalProperty(String key) {
        SingularHelper.unsetGlobalProperty(key);
    }

    @Override
    public void clearGlobalProperties() {
        SingularHelper.clearGlobalProperties();
    }

    @Override
    public WritableMap getGlobalProperties() {
        Map<String, String> globalProps = SingularHelper.getGlobalProperties();
        return convertStringMapToWritableMap(globalProps);
    }

    @Override
    public void createReferrerShortLink(String baseLink, String referrerName, String referrerId, ReadableMap passthroughParams, Callback completionHandler) {
        try {
            JSONObject params = null;
            if (passthroughParams != null) {
                params = convertReadableMapToJsonObject(passthroughParams);
            }

            SingularHelper.createReferrerShortLink(baseLink,
                    referrerName,
                    referrerId,
                    params,
                    new ShortLinkHandler() {
                        @Override
                        public void onSuccess(final String link) {
                            if (completionHandler != null) {
                                completionHandler.invoke(link, "");
                            }
                        }

                        @Override
                        public void onError(final String error) {
                            if (completionHandler != null) {
                                completionHandler.invoke("", error);
                            }
                        }
                    });
        } catch (Exception e) {
            if (completionHandler != null) {
                completionHandler.invoke("", "Error: " + e.getMessage());
            }
        }
    }

    @Override
    public void adRevenue(ReadableMap adData) {
        try {
            Map<String, Object> adRevenueData = new HashMap<>();

            adRevenueData.put(Constants.AdRevenue.AD_PLATFORM, adData.getString(Constants.AdRevenue.AD_PLATFORM));
            adRevenueData.put(Constants.AdRevenue.AD_CURRENCY, adData.getString(Constants.AdRevenue.AD_CURRENCY));
            adRevenueData.put(Constants.AdRevenue.AD_REVENUE, adData.getDouble(Constants.AdRevenue.AD_REVENUE));

            if (adData.hasKey(Constants.AdRevenue.R)) {
                adRevenueData.put(Constants.AdRevenue.R, adData.getDouble(Constants.AdRevenue.R));
            }
            if (adData.hasKey(Constants.AdRevenue.PCC)) {
                adRevenueData.put(Constants.AdRevenue.PCC, adData.getString(Constants.AdRevenue.PCC));
            }
            if (adData.hasKey(Constants.AdRevenue.IS_ADMON_REVENUE)) {
                adRevenueData.put(Constants.AdRevenue.IS_ADMON_REVENUE, adData.getBoolean(Constants.AdRevenue.IS_ADMON_REVENUE));
            }
            if (adData.hasKey(Constants.AdRevenue.IS_REVENUE_EVENT)) {
                adRevenueData.put(Constants.AdRevenue.IS_REVENUE_EVENT, adData.getBoolean(Constants.AdRevenue.IS_REVENUE_EVENT));
            }

            if (adData.hasKey(Constants.AdRevenue.AD_MEDIATION_PLATFORM) && !adData.isNull(Constants.AdRevenue.AD_MEDIATION_PLATFORM)) {
                adRevenueData.put(Constants.AdRevenue.AD_MEDIATION_PLATFORM, adData.getString(Constants.AdRevenue.AD_MEDIATION_PLATFORM));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_TYPE) && !adData.isNull(Constants.AdRevenue.AD_TYPE)) {
                adRevenueData.put(Constants.AdRevenue.AD_TYPE, adData.getString(Constants.AdRevenue.AD_TYPE));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_GROUP_TYPE) && !adData.isNull(Constants.AdRevenue.AD_GROUP_TYPE)) {
                adRevenueData.put(Constants.AdRevenue.AD_GROUP_TYPE, adData.getString(Constants.AdRevenue.AD_GROUP_TYPE));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_IMPRESSION_ID) && !adData.isNull(Constants.AdRevenue.AD_IMPRESSION_ID)) {
                adRevenueData.put(Constants.AdRevenue.AD_IMPRESSION_ID, adData.getString(Constants.AdRevenue.AD_IMPRESSION_ID));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_PLACEMENT_NAME) && !adData.isNull(Constants.AdRevenue.AD_PLACEMENT_NAME)) {
                adRevenueData.put(Constants.AdRevenue.AD_PLACEMENT_NAME, adData.getString(Constants.AdRevenue.AD_PLACEMENT_NAME));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_UNIT_ID) && !adData.isNull(Constants.AdRevenue.AD_UNIT_ID)) {
                adRevenueData.put(Constants.AdRevenue.AD_UNIT_ID, adData.getString(Constants.AdRevenue.AD_UNIT_ID));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_UNIT_NAME) && !adData.isNull(Constants.AdRevenue.AD_UNIT_NAME)) {
                adRevenueData.put(Constants.AdRevenue.AD_UNIT_NAME, adData.getString(Constants.AdRevenue.AD_UNIT_NAME));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_GROUP_ID) && !adData.isNull(Constants.AdRevenue.AD_GROUP_ID)) {
                adRevenueData.put(Constants.AdRevenue.AD_GROUP_ID, adData.getString(Constants.AdRevenue.AD_GROUP_ID));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_GROUP_NAME) && !adData.isNull(Constants.AdRevenue.AD_GROUP_NAME)) {
                adRevenueData.put(Constants.AdRevenue.AD_GROUP_NAME, adData.getString(Constants.AdRevenue.AD_GROUP_NAME));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_GROUP_PRIORITY) && !adData.isNull(Constants.AdRevenue.AD_GROUP_PRIORITY)) {
                adRevenueData.put(Constants.AdRevenue.AD_GROUP_PRIORITY, adData.getString(Constants.AdRevenue.AD_GROUP_PRIORITY));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_PRECISION) && !adData.isNull(Constants.AdRevenue.AD_PRECISION)) {
                adRevenueData.put(Constants.AdRevenue.AD_PRECISION, adData.getString(Constants.AdRevenue.AD_PRECISION));
            }
            if (adData.hasKey(Constants.AdRevenue.AD_PLACEMENT_ID) && !adData.isNull(Constants.AdRevenue.AD_PLACEMENT_ID)) {
                adRevenueData.put(Constants.AdRevenue.AD_PLACEMENT_ID, adData.getString(Constants.AdRevenue.AD_PLACEMENT_ID));
            }

            JSONObject jsonObject = new JSONObject(adRevenueData);
            String adRevenueString = jsonObject.toString();
            Singular.event(Constants.AdRevenue.AD_REVENUE_EVENT, adRevenueString);
        } catch (Exception e) {
        }
    }

    @Override
    public void setLimitAdvertisingIdentifiers(boolean enabled) {
        SingularHelper.setLimitAdvertisingIdentifiers(enabled);
    }

    // iOS-specific methods (no-op on Android, but required for Codegen)
    @Override
    public boolean skanUpdateConversionValue(double conversionValue) {
        // SKAdNetwork is iOS-only, no-op on Android
        return false;
    }

    @Override
    public void skanUpdateConversionValues(double conversionValue, double coarse, boolean lock) {
        // SKAdNetwork is iOS-only, no-op on Android
    }

    @Override
    public Double skanGetConversionValue() {
        // SKAdNetwork is iOS-only, return null on Android
        return null;
    }

    @Override
    public void skanRegisterAppForAdNetworkAttribution() {
        // SKAdNetwork is iOS-only, no-op on Android
    }

    @Override
    public void handlePushNotification(ReadableMap pushNotificationPayload) {
        // Push notification handling is iOS-specific, no-op on Android
        // Android handles push notifications differently
    }

    private SDIDAccessorHandler createSdidHandler() {
        return new SDIDAccessorHandler() {
            @Override
            public void didSetSdid(String result) {
                reactContext.
                        getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(SingularHelper.Constants.DID_SET_SDID_HANDLER_CONST, result);
            }

            @Override
            public void sdidReceived(String result) {
                reactContext.
                        getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(SingularHelper.Constants.SDID_RECEIVED_HANDLER_CONST, result);
            }
        };
    }

    private SingularConfig buildSingularConfigFromReadableMap(ReadableMap configMap) {
        try {
            String apikey = configMap.getString(Constants.CONFIG_KEY_APIKEY);
            String secret = configMap.getString(Constants.CONFIG_KEY_SECRET);
            SingularConfig config = new SingularConfig(apikey, secret);
            handleConfigOptionalFields(configMap, config);
            setupCallbacks(config);
            return config;
        } catch (Exception e) {
            return null;
        }
    }

    private void handleConfigOptionalFields(ReadableMap configMap, SingularConfig config) {
        if (configMap.hasKey(Constants.CONFIG_KEY_ESP_DOMAINS)) {
            ReadableArray espDomainsArray = configMap.getArray(Constants.CONFIG_KEY_ESP_DOMAINS);
            List<String> espDomainsList = convertReadableArrayToList(espDomainsArray);
            if (espDomainsList != null && espDomainsList.size() > 0) {
                config.withESPDomains(espDomainsList);
            }
        }

        int sessionTimeout = -1;
        if (configMap.hasKey(Constants.CONFIG_KEY_SESSION_TIMEOUT)) {
            sessionTimeout = configMap.getInt(Constants.CONFIG_KEY_SESSION_TIMEOUT);
        }
        if (sessionTimeout >= 0) {
            config.withSessionTimeoutInSec(sessionTimeout);
        }

        long ddlTimeoutSec = 0;
        if (configMap.hasKey(Constants.CONFIG_KEY_DDL_TIMEOUT_SEC)) {
            ddlTimeoutSec = configMap.getInt(Constants.CONFIG_KEY_DDL_TIMEOUT_SEC);
        }
        if (ddlTimeoutSec > 0) {
            config.withDDLTimeoutInSec(ddlTimeoutSec);
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_SHORT_LINK_RESOLVE_TIMEOUT)) {
            shortLinkResolveTimeout = configMap.getInt(Constants.CONFIG_KEY_SHORT_LINK_RESOLVE_TIMEOUT);
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_CUSTOM_USER_ID)) {
            String customUserId = configMap.getString(Constants.CONFIG_KEY_CUSTOM_USER_ID);
            if (customUserId != null) {
                config.withCustomUserId(customUserId);
            }
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_IMEI)) {
            String imei = configMap.getString(Constants.CONFIG_KEY_IMEI);
            if (imei != null) {
                config.withIMEI(imei);
            }
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_LIMIT_DATA_SHARING) && !configMap.isNull(Constants.CONFIG_KEY_LIMIT_DATA_SHARING)) {
            boolean limitDataSharing = configMap.getBoolean(Constants.CONFIG_KEY_LIMIT_DATA_SHARING);
            config.withLimitDataSharing(limitDataSharing);
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_COLLECT_OAID)) {
            boolean collectOAID = configMap.getBoolean(Constants.CONFIG_KEY_COLLECT_OAID);
            if (collectOAID) {
                config.withOAIDCollection();
            }
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_ENABLE_LOGGING)) {
            boolean enableLogging = configMap.getBoolean(Constants.CONFIG_KEY_ENABLE_LOGGING);
            if (enableLogging) {
                config.withLoggingEnabled();
            }
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_LIMIT_ADVERTISING_IDENTIFIERS)) {
            boolean limitAdvertisingIdentifiers = configMap.getBoolean(Constants.CONFIG_KEY_LIMIT_ADVERTISING_IDENTIFIERS);
            if (limitAdvertisingIdentifiers) {
                config.withLimitAdvertisingIdentifiers();
            }
        }

        int logLevel = -1;
        if (configMap.hasKey(Constants.CONFIG_KEY_LOG_LEVEL)) {
            logLevel = configMap.getInt(Constants.CONFIG_KEY_LOG_LEVEL);
        }

        if (logLevel >= 0) {
            config.withLogLevel(logLevel);
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_FACEBOOK_APP_ID)) {
            String facebookAppId = configMap.getString(Constants.CONFIG_KEY_FACEBOOK_APP_ID);
            if (facebookAppId != null) {
                config.withFacebookAppId(facebookAppId);
            }
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_GLOBAL_PROPERTIES)) {
            ReadableMap globalProperties = configMap.getMap(Constants.CONFIG_KEY_GLOBAL_PROPERTIES);
            if (globalProperties != null) {
                handleGlobalProperties(globalProperties, config);
            }
        }

        String customSdid = null;
        if (configMap.hasKey(Constants.CONFIG_KEY_CUSTOM_SDID)) {
            customSdid = configMap.getString(Constants.CONFIG_KEY_CUSTOM_SDID);
            if (!SingularHelper.isValidNonEmptyString(customSdid)) {
                customSdid = null;
            }
        }

        config.withCustomSdid(customSdid, createSdidHandler());

        if (configMap.hasKey(Constants.CONFIG_KEY_BRANDED_DOMAINS)) {
            ReadableArray brandedDomainsArray = configMap.getArray(Constants.CONFIG_KEY_BRANDED_DOMAINS);
            List<String> brandedDomainsList = convertReadableArrayToList(brandedDomainsArray);
            if (brandedDomainsList != null && brandedDomainsList.size() > 0) {
                config.withBrandedDomains(brandedDomainsList);
            }
        }

        if (configMap.hasKey(Constants.CONFIG_KEY_PUSH_NOTIFICATIONS_LINK_PATHS)) {
            ReadableArray pushNotificationLinkPathsArray = configMap.getArray(Constants.CONFIG_KEY_PUSH_NOTIFICATIONS_LINK_PATHS);
            String[][] pushSelectors = convertReadableArrayTo2DArray(pushNotificationLinkPathsArray);
            if (pushSelectors != null) {
                pushNotificationsLinkPaths = pushSelectors;
            }
        }
    }

    private void handleGlobalProperties(ReadableMap globalProperties, SingularConfig config) {
        if (globalProperties == null) return;

        ReadableMapKeySetIterator iterator = globalProperties.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableMap property = globalProperties.getMap(key);

            if (property != null) {
                String propertyKey = property.getString(Constants.GLOBAL_PROP_KEY);
                String propertyValue = property.getString(Constants.GLOBAL_PROP_VALUE);
                boolean overrideExisting = false;
                if (property.hasKey(Constants.GLOBAL_PROP_OVERRIDE_EXISTING)) {
                    overrideExisting = property.getBoolean(Constants.GLOBAL_PROP_OVERRIDE_EXISTING);
                }

                if (propertyKey != null && propertyValue != null) {
                    config.withGlobalProperty(propertyKey, propertyValue, overrideExisting);
                }
            }
        }
    }

    private void setupCallbacks(SingularConfig config) {
        config.withSingularDeviceAttribution(new SingularDeviceAttributionHandler() {
            @Override
            public void onDeviceAttributionInfoReceived(Map<String, Object> deviceAttributionData) {
                WritableMap attributionInfo = convertMapToWritableMap(deviceAttributionData);
                reactContext.
                        getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(SingularHelper.Constants.DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST, attributionInfo);
            }
        });

        staticSingularLinkHandler = new SingularLinkHandler() {
            @Override
            public void onResolved(SingularLinkParams singularLinkParams) {
                WritableMap params = createSingularLinkParams(singularLinkParams);
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(SingularHelper.Constants.SINGULAR_LINK_HANDLER_CONST, params);
            }
        };

        if (reactContext.hasCurrentActivity()) {
            Intent intent = getCurrentActivity().getIntent();
            if (intent != null) {
                int intentHash = intent.hashCode();
                if (intentHash != currentIntentHash) {
                    currentIntentHash = intentHash;
                    config.withSingularLink(intent, staticSingularLinkHandler, shortLinkResolveTimeout);
                    if (intent.getExtras() != null && intent.getExtras().size() > 0 && pushNotificationsLinkPaths != null) {
                        config.withPushNotificationPayload(intent, pushNotificationsLinkPaths);
                    }
                }
            }
        } else {
            config.withSingularLink(null, staticSingularLinkHandler, shortLinkResolveTimeout);
        }
    }

    private List<String> convertReadableArrayToList(ReadableArray readableArray) {
        if (readableArray == null) return null;

        List<String> list = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); i++) {
            if (readableArray.getType(i) == ReadableType.String) {
                String item = readableArray.getString(i);
                if (item != null && !item.isEmpty()) {
                    list.add(item);
                }
            }
        }
        return list.isEmpty() ? null : list;
    }

    private String[][] convertReadableArrayTo2DArray(ReadableArray readableArray) {
        if (readableArray == null || readableArray.size() <= 0) {
            return null;
        }

        try {
            String[][] result = new String[readableArray.size()][];

            for (int outerIndex = 0; outerIndex < readableArray.size(); outerIndex++) {
                if (readableArray.getType(outerIndex) == ReadableType.Array) {
                    ReadableArray innerArray = readableArray.getArray(outerIndex);
                    result[outerIndex] = new String[innerArray.size()];

                    for (int innerIndex = 0; innerIndex < innerArray.size(); innerIndex++) {
                        if (innerArray.getType(innerIndex) == ReadableType.String) {
                            result[outerIndex][innerIndex] = innerArray.getString(innerIndex);
                        }
                    }
                }
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private WritableMap convertMapToWritableMap(Map<String, Object> map) {
        WritableMap writableMap = Arguments.createMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                writableMap.putString(key, (String) value);
            } else if (value instanceof Integer) {
                writableMap.putInt(key, (Integer) value);
            } else if (value instanceof Double) {
                writableMap.putDouble(key, (Double) value);
            } else if (value instanceof Boolean) {
                writableMap.putBoolean(key, (Boolean) value);
            } else {
                writableMap.putString(key, value.toString());
            }
        }
        return writableMap;
    }

    private WritableMap createSingularLinkParams(SingularLinkParams singularLinkParams) {
        WritableMap params = Arguments.createMap();
        params.putString("deeplink", singularLinkParams.getDeeplink());
        params.putString("passthrough", singularLinkParams.getPassthrough());
        params.putBoolean("isDeferred", singularLinkParams.isDeferred());

        WritableMap urlParams = Arguments.createMap();
        if (singularLinkParams.getUrlParameters() != null) {
            for (Map.Entry<String, String> entry : singularLinkParams.getUrlParameters().entrySet()) {
                urlParams.putString(entry.getKey(), entry.getValue());
            }
        }
        params.putMap("urlParameters", urlParams);

        return params;
    }

    private Map<String, Object> convertReadableMapToMap(ReadableMap readableMap) {
        if (readableMap == null) return null;

        Map<String, Object> map = new HashMap<>();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);

            switch (type) {
                case Null:
                    map.put(key, null);
                    break;
                case Boolean:
                    map.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    map.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    map.put(key, readableMap.getString(key));
                    break;
                case Map:
                    map.put(key, convertReadableMapToMap(readableMap.getMap(key)));
                    break;
                case Array:
                    map.put(key, convertReadableArrayToObjectList(readableMap.getArray(key)));
                    break;
                default:
                    Log.w("SingularSDK", "Unknown ReadableType: " + type);
                    break;
            }
        }

        return map;
    }

    private JSONObject convertReadableMapToJSONObject(ReadableMap readableMap) throws Exception {
        if (readableMap == null) return EMPTY_JSON_OBJECT;

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        if (!iterator.hasNextKey()) return EMPTY_JSON_OBJECT;

        JSONObject jsonObject = new JSONObject();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);

            switch (type) {
                case Null:
                    jsonObject.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    jsonObject.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    jsonObject.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    jsonObject.put(key, readableMap.getString(key));
                    break;
                case Map:
                    jsonObject.put(key, convertReadableMapToJSONObject(readableMap.getMap(key)));
                    break;
                case Array:
                    jsonObject.put(key, convertReadableArrayToJSONArray(readableMap.getArray(key)));
                    break;
                default:
                    Log.w("SingularSDK", "Unknown ReadableType: " + type);
                    break;
            }
        }

        return jsonObject;
    }

    private JSONArray convertReadableArrayToJSONArray(ReadableArray readableArray) throws Exception {
        if (readableArray == null) return EMPTY_JSON_ARRAY;

        int size = readableArray.size();
        if (size == 0) return EMPTY_JSON_ARRAY;

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < size; i++) {
            ReadableType type = readableArray.getType(i);

            switch (type) {
                case Null:
                    jsonArray.put(JSONObject.NULL);
                    break;
                case Boolean:
                    jsonArray.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    jsonArray.put(readableArray.getDouble(i));
                    break;
                case String:
                    jsonArray.put(readableArray.getString(i));
                    break;
                case Map:
                    jsonArray.put(convertReadableMapToJSONObject(readableArray.getMap(i)));
                    break;
                case Array:
                    jsonArray.put(convertReadableArrayToJSONArray(readableArray.getArray(i)));
                    break;
                default:
                    Log.w("SingularSDK", "Unknown ReadableType: " + type);
                    break;
            }
        }

        return jsonArray;
    }

    private List<Object> convertReadableArrayToObjectList(ReadableArray readableArray) {
        if (readableArray == null) return null;

        List<Object> list = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType type = readableArray.getType(i);

            switch (type) {
                case Null:
                    list.add(null);
                    break;
                case Boolean:
                    list.add(readableArray.getBoolean(i));
                    break;
                case Number:
                    list.add(readableArray.getDouble(i));
                    break;
                case String:
                    list.add(readableArray.getString(i));
                    break;
                case Map:
                    list.add(convertReadableMapToMap(readableArray.getMap(i)));
                    break;
                case Array:
                    list.add(convertReadableArrayToObjectList(readableArray.getArray(i)));
                    break;
                default:
                    Log.w("SingularSDK", "Unknown ReadableType: " + type);
                    break;
            }
        }

        return list;
    }

    private WritableMap convertStringMapToWritableMap(Map<String, String> map) {
        WritableMap writableMap = Arguments.createMap();

        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                writableMap.putString(entry.getKey(), entry.getValue());
            }
        }

        return writableMap;
    }

    private Map<String, Object> getPurchaseValues(ReadableMap purchase) {
        Map<String, Object> purchaseValues = new HashMap<>();
        
        if (purchase.hasKey("revenue")) {
            purchaseValues.put("r", purchase.getDouble("revenue"));
        }

        if (purchase.hasKey("currency")) {
            purchaseValues.put("pcc", purchase.getString("currency"));
        }

        if (purchase.hasKey("is_revenue_event")) {
            purchaseValues.put("is_revenue_event", purchase.getBoolean("is_revenue_event"));
        }

        if (purchase.hasKey("receipt") && !purchase.isNull("receipt")) {
            String receiptValue = purchase.getString("receipt");
            purchaseValues.put("receipt", receiptValue);
            purchaseValues.put("ptr", receiptValue);
        }

        if (purchase.hasKey("receipt_signature") && !purchase.isNull("receipt_signature")) {
            purchaseValues.put("receipt_signature", purchase.getString("receipt_signature"));
        }

        return purchaseValues;
    }

    private JSONObject convertReadableMapToJsonObject(ReadableMap readableMap) {
        if (readableMap == null) return null;

        try {
            JSONObject jsonObject = new JSONObject();
            ReadableMapKeySetIterator iterator = readableMap.keySetIterator();

            while (iterator.hasNextKey()) {
                String key = iterator.nextKey();
                ReadableType type = readableMap.getType(key);

                switch (type) {
                    case Null:
                        jsonObject.put(key, JSONObject.NULL);
                        break;
                    case Boolean:
                        jsonObject.put(key, readableMap.getBoolean(key));
                        break;
                    case Number:
                        jsonObject.put(key, readableMap.getDouble(key));
                        break;
                    case String:
                        jsonObject.put(key, readableMap.getString(key));
                        break;
                    case Map:
                        JSONObject nestedObject = convertReadableMapToJsonObject(readableMap.getMap(key));
                        jsonObject.put(key, nestedObject);
                        break;
                    case Array:
                        JSONArray nestedArray = convertReadableArrayToJsonArray(readableMap.getArray(key));
                        jsonObject.put(key, nestedArray);
                        break;
                    default:
                        Log.w("SingularSDK", "Unknown ReadableType: " + type);
                        break;
                }
            }

            return jsonObject;
        } catch (Exception e) {
            return null;
        }
    }

    private JSONArray convertReadableArrayToJsonArray(ReadableArray readableArray) {
        if (readableArray == null) return null;

        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < readableArray.size(); i++) {
                ReadableType type = readableArray.getType(i);

                switch (type) {
                    case Null:
                        jsonArray.put(JSONObject.NULL);
                        break;
                    case Boolean:
                        jsonArray.put(readableArray.getBoolean(i));
                        break;
                    case Number:
                        jsonArray.put(readableArray.getDouble(i));
                        break;
                    case String:
                        jsonArray.put(readableArray.getString(i));
                        break;
                    case Map:
                        JSONObject nestedObject = convertReadableMapToJsonObject(readableArray.getMap(i));
                        jsonArray.put(nestedObject);
                        break;
                    case Array:
                        JSONArray nestedArray = convertReadableArrayToJsonArray(readableArray.getArray(i));
                        jsonArray.put(nestedArray);
                        break;
                    default:
                        Log.w("SingularSDK", "Unknown ReadableType: " + type);
                        break;
                }
            }

            return jsonArray;
        } catch (Exception e) {
            return null;
        }
    }

    public static void onNewIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        if (config == null) {
            return;
        }

        if (intent.hashCode() == currentIntentHash) {
            return;
        }

        currentIntentHash = intent.hashCode();

        if (intent.getData() != null && staticSingularLinkHandler != null) {
            config.withSingularLink(intent, staticSingularLinkHandler);
        }

        if (intent.getExtras() != null && intent.getExtras().size() > 0 && pushNotificationsLinkPaths != null && pushNotificationsLinkPaths.length > 0) {
            config.withPushNotificationPayload(intent, pushNotificationsLinkPaths);
        }

        SingularHelper.initWithSingularConfig(reactContext, config);
    }
}
