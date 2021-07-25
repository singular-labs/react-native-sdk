export class SingularConfig {
    apikey;
    secret;
    sessionTimeout;
    customUserId;
    singularLinkHandler;
    shortLinkResolveTimeout;

    // SKAN
    skAdNetworkEnabled;
    manualSkanConversionManagement;
    conversionValueUpdatedHandler;
    waitForTrackingAuthorizationWithTimeoutInterval;

    // Limit Data Sharing
    limitDataSharing;

    constructor(apikey, secret) {
        this.apikey = apikey;
        this.secret = secret;
        this.sessionTimeout = -1; // default -1, uses default timeout (60s)
        this.skAdNetworkEnabled = false;
        this.manualSkanConversionManagement = false;
        this.waitForTrackingAuthorizationWithTimeoutInterval = 0;
        this.limitDataSharing = null;
        this.shortLinkResolveTimeout = 10; // default timeout 10s
    }

    withSessionTimeoutInSec(sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
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
}
