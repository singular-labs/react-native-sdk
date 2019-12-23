export class SingularConfig {
    apikey;
    secret;
    sessionTimeout;
    customUserId;
    singularLinkHandler;

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
}
