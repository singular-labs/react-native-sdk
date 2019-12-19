export class SingularConfig {
    apikey;
    secret;
    customUserId;
    singularLinksCallback;

    constructor(apikey, secret) {
        this.apikey = apikey;
        this.secret = secret;
        this.customUserId = null;
    }

    withCustomUserId(customUserId){
        this.customUserId = customUserId;
        return this;
    }

    withSingularLinks(singularLinksCallback){
        this.singularLinksCallback = singularLinksCallback;
        return this;
    }
}
