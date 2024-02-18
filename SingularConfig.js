export class SingularConfig {
    apikey;
    secret;
    sessionTimeout;
    customUserId;
    singularLinkHandler;
    shortLinkResolveTimeout;

    // SKAN
    skAdNetworkEnabled;
    clipboardAttribution;
    manualSkanConversionManagement;
    conversionValueUpdatedHandler;
    conversionValuesUpdatedHandler;
    waitForTrackingAuthorizationWithTimeoutInterval;

    deviceAttributionCallbackHandler;

    // Limit Data Sharing
    limitDataSharing;

    // Global Properties
    globalProperties;
    collectOAID
    enableLogging;
    espDomains;
    facebookAppId;

    // SDID
    customSdid;
    didSetSdidCallback;
    sdidReceivedCallback;

    constructor(apikey, secret) {
        this.apikey = apikey;
        this.secret = secret;
        this.sessionTimeout = -1; // default -1, uses default timeout (60s)
        this.skAdNetworkEnabled = true;
        this.manualSkanConversionManagement = false;
        this.waitForTrackingAuthorizationWithTimeoutInterval = 0;
        this.limitDataSharing = null;
        this.shortLinkResolveTimeout = 10; // default timeout 10s
        this.globalProperties = {}
        this.collectOAID = false;
        this.enableLogging = false;
        this.clipboardAttribution = false;
    }

    withSessionTimeoutInSec(sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    withClipboardAttribution() {
        this.clipboardAttribution = true;
        return this;
    }

    withCustomUserId(customUserId) {
        this.customUserId = customUserId;
        return this;
    }

    withSingularLink(singularLinkHandler) {
        this.singularLinkHandler = singularLinkHandler;
        return this;
    }

    withSkAdNetworkEnabled(skAdNetworkEnabled) {
        this.skAdNetworkEnabled = skAdNetworkEnabled;
        return this;
    }

    withManualSkanConversionManagement() {
        this.manualSkanConversionManagement = true;
        return this;
    }

    withConversionValueUpdatedHandler(conversionValueUpdatedHandler) {
        this.conversionValueUpdatedHandler = conversionValueUpdatedHandler;
        return this;
    }

    withConversionValuesUpdatedHandler(conversionValuesUpdatedHandler) {
        this.conversionValuesUpdatedHandler = conversionValuesUpdatedHandler;
        return this;
    }

    withDeviceAttributionCallbackHandler(deviceAttributionCallbackHandler) {
        this.deviceAttributionCallbackHandler = deviceAttributionCallbackHandler;
        return this;
    }
    
    withWaitForTrackingAuthorizationWithTimeoutInterval(waitForTrackingAuthorizationWithTimeoutInterval) {
        this.waitForTrackingAuthorizationWithTimeoutInterval = waitForTrackingAuthorizationWithTimeoutInterval;
        return this;
    }

    withLimitDataSharing(shouldLimitDataSharing) {
        this.limitDataSharing = shouldLimitDataSharing;
        return this;
    }

    withShortLinkResolveTimeout(shortLinkResolveTimeout) {
        this.shortLinkResolveTimeout = shortLinkResolveTimeout;
        return this;
    }

    withGlobalProperty(key, value,overrideExisting) {
        this.globalProperties[key] = {"Key":key, "Value":value,"OverrideExisting":overrideExisting};
        return this;
    }

    withOAIDCollection() {
        this.collectOAID = true;
        return this;
    }

    withLoggingEnabled() {
        this.enableLogging = true;
        return this;
    }

    withLogLevel(level) {
        this.logLevel = level;
        return this;
    }

    withEspDomains(domains) {
        this.espDomains = domains;
        return this;
    }

    withFacebookAppId(appId) {
        this.facebookAppId = appId;
        return this;
    }

    withSdid(customSdid, didSetSdidCallback, sdidReceivedCallback) {
        this.customSdid = customSdid;
        this.didSetSdidCallback = didSetSdidCallback;
        this.sdidReceivedCallback = sdidReceivedCallback;
        return this;
    }
}
