export class SingularConfig {
    apikey;
    secret;
    sessionTimeout;
    customUserId;
    singularLinkHandler;

    // SKAN
    skAdNetworkEnabled;
    manualSkanConversionManagement;
    conversionValueHandler;

    constructor(apikey, secret) {
        this.apikey = apikey;
        this.secret = secret;
        this.sessionTimeout = -1; // default -1, uses default timeout (60s)
    }

    withSessionTimeoutInSec(sessionTimeout){
        this.sessionTimeout = sessionTimeout;
        return this;
    }

    withCustomUserId(customUserId){
        this.customUserId = customUserId;
        return this;
    }

    withSingularLink(singularLinkHandler){
        this.singularLinkHandler = singularLinkHandler;
        return this;
    }

    withSkAdNetworkEnabled(skAdNetworkEnabled) {
        this.skAdNetworkEnabled = skAdNetworkEnabled;
        return this;   
    }

    withManualSkanConversionManagement(manualSkanConversionManagement) {
        this.manualSkanConversionManagement = manualSkanConversionManagement;
        return this;   
    }

    withConversionValueHandler(conversionValueHandler){
        this.conversionValueHandler = conversionValueHandler;
        return this;
    }
}
