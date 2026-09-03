package net.singular.react_native;
import com.singular.sdk.Singular;
import com.singular.sdk.SingularConfig;
import com.singular.sdk.ShortLinkHandler;
import com.singular.sdk.SingularUserDetails;
import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import com.facebook.react.bridge.ReactApplicationContext;

public class SingularHelper {

    protected interface Constants {
        String SINGULAR_LINK_HANDLER_CONST = "SingularLinkHandler";
        String DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST = "DeviceAttributionCallbackHandler";
        String SHORT_LINK_HANDLER_CONST = "ShortLinkHandler";
        String DID_SET_SDID_HANDLER_CONST = "DidSetSdidCallback";
        String SDID_RECEIVED_HANDLER_CONST = "SdidReceivedCallback";
    }

    public static void initWithSingularConfig(ReactApplicationContext context, SingularConfig config) {
        Singular.init(context, config);
    }

    public static void setCustomUserId(String customUserId) {
        Singular.setCustomUserId(customUserId);
    }
    
    public static void unsetCustomUserId() {
        Singular.unsetCustomUserId();
    }
    
    public static void setDeviceCustomUserId(String customUserId) {
        Singular.setDeviceCustomUserId(customUserId);
    }
    
    public static void event(String name) {
        Singular.event(name);
    }
    
    public static void eventWithArgs(String name, String extra) {
        Singular.event(name, extra);
    }
    
    public static void revenue(String currency, double amount) {
        Singular.revenue(currency, amount);
    }
    
    public static void revenueWithArgs(String currency, double amount, Map<String, Object> args) {
        Singular.revenue(currency, amount, args);
    }

    public static void customRevenue(String eventName, String currency, double amount) {
        Singular.customRevenue(eventName, currency, amount);
    }
    
    public static void customRevenueWithArgs(String eventName, String currency, double amount, Map<String, Object> args) {
        Singular.customRevenue(eventName, currency, amount, args);
    }

    public static void setUninstallToken(String token) {
        Singular.setFCMDeviceToken(token);
    }
    
    public static void trackingOptIn() {
        Singular.trackingOptIn();
    }
    
    public static void trackingUnder13() {
        Singular.trackingUnder13();
    }
    
    public static void stopAllTracking() {
        Singular.stopAllTracking();
    }
    
    public static void resumeAllTracking() {
        Singular.resumeAllTracking();
    }
    
    public static boolean isAllTrackingStopped() {
        return Singular.isAllTrackingStopped();
    }
    
    public static void limitDataSharing(boolean limitDataSharingValue) {
        Singular.limitDataSharing(limitDataSharingValue);
    }
    
    public static boolean getLimitDataSharing() {
        return Singular.getLimitDataSharing();
    }
    
    public static void setReactSDKVersion(String wrapper, String version) {
        Singular.setWrapperNameAndVersion(wrapper, version);
    }
    
    public static boolean setGlobalProperty(String key, String value, boolean overrideExisting) {
        return Singular.setGlobalProperty(key, value, overrideExisting);
    }
    
    public static void unsetGlobalProperty(String key) {
        Singular.unsetGlobalProperty(key);
    }
    
    public static void clearGlobalProperties() {
        Singular.clearGlobalProperties();
    }
    
    public static Map<String, String> getGlobalProperties() {
        return Singular.getGlobalProperties();
    }
    
    public static void setUserDetails(SingularUserDetails userDetails) {
        Singular.setUserDetails(userDetails);
    }

    public static void clearUserDetails() {
        Singular.clearUserDetails();
    }

    public static SingularUserDetails buildUserDetails(JSONObject values) {
        if (values == null) {
            return null;
        }

        SingularUserDetails userDetails = new SingularUserDetails();

        String email = values.optString("email", null);
        if (isValidNonEmptyString(email)) {
            userDetails.setEmail(email);
        }

        String phoneNumber = values.optString("phoneNumber", null);
        if (isValidNonEmptyString(phoneNumber)) {
            userDetails.setPhoneNumber(phoneNumber);
        }

        String emailSTD = values.optString("emailSTD", null);
        if (isValidNonEmptyString(emailSTD)) {
            userDetails.setEmailSTD(emailSTD);
        }

        String emailNoDots = values.optString("emailNoDots", null);
        if (isValidNonEmptyString(emailNoDots)) {
            userDetails.setEmailNoDots(emailNoDots);
        }

        String phoneE164 = values.optString("phoneE164", null);
        if (isValidNonEmptyString(phoneE164)) {
            userDetails.setPhoneE164(phoneE164);
        }

        String phoneDigits = values.optString("phoneDigits", null);
        if (isValidNonEmptyString(phoneDigits)) {
            userDetails.setPhoneDigits(phoneDigits);
        }

        return userDetails;
    }

    public static void setLimitAdvertisingIdentifiers(boolean enabled) {
        Singular.setLimitAdvertisingIdentifiers(enabled);
    }

    public static void createReferrerShortLink(String baseLink,
                                               String referrerName,
                                               String referrerId,
                                               JSONObject passthroughParams,
                                               ShortLinkHandler completionHandler) {
        Singular.createReferrerShortLink(baseLink, referrerName, referrerId, passthroughParams, completionHandler);
    }

    public static boolean isValidNonEmptyString(String str) {
        return str != null
                && str instanceof String
                && str.length() > 0
                && !str.toLowerCase().equals("null")
                && !str.toLowerCase().equals("undefined")
                && !str.toLowerCase().equals("false")
                && !str.equals("NaN");
    }
}
