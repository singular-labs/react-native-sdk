import {NativeEventEmitter, NativeModules} from 'react-native';

const {SingularBridge} = NativeModules;

const SDK_NAME = 'ReactNative';
const SDK_VERSION = '1.0.0';

export class Singular {

    static _singularLinkHandler;
    static _singularLinksHandlerEmitter = new NativeEventEmitter(SingularBridge);

    static init(apikey, secret, customUserId) {
        SingularBridge.init(apikey, secret, customUserId);
        SingularBridge.setReactSDKVersion(SDK_NAME, SDK_VERSION);
    }

    static initWithSingularLinks(apikey, secret, customUserId, singularLinksCallback) {
        this._singularLinkHandler = singularLinksCallback;

        this._singularLinksHandlerEmitter.addListener(
            'SingularLinkHandler',
            singularLinksParams => {
                if (this._singularLinkHandler) {
                    this._singularLinkHandler(singularLinksParams);
                }
            });

        SingularBridge.initWithSingularLinks(apikey, secret, customUserId);
        SingularBridge.setReactSDKVersion(SDK_NAME, SDK_VERSION);
    }

    static setCustomUserId(customUserId) {
        SingularBridge.setCustomUserId(customUserId);
    }

    static unsetCustomUserId() {
        SingularBridge.unsetCustomUserId();
    }

    static event(eventName) {
        SingularBridge.event(eventName);
    }

    static eventWithArgs(eventName, args) {
        SingularBridge.eventWithArgs(eventName, JSON.stringify(args));
    }

    static revenue(currency, amount) {
        SingularBridge.revenue(currency, amount);
    }

    static revenueWithArgs(currency, amount, args) {
        SingularBridge.revenueWithArgs(currency, amount, JSON.stringify(args));
    }

    static customRevenue(eventName, currency, amount) {
        SingularBridge.customRevenue(eventName, currency, amount);
    }

    static customRevenueWithArgs(eventName, currency, amount, args) {
        SingularBridge.customRevenueWithArgs(
            eventName,
            currency,
            amount,
            JSON.stringify(args),
        );
    }

    static inAppPurchase(eventName, purchase) {
        this.eventWithArgs(eventName, purchase.getPurchaseValues());
    }

    static inAppPurchaseWithArgs(eventName, purchase, args) {
        this.eventWithArgs(eventName, {...purchase.getPurchaseValues(), args});
    }

    static setUninstallToken(token) {
        SingularBridge.setUninstallToken(token);
    }

    static trackingOptIn() {
        SingularBridge.trackingOptIn();
    }

    static trackingUnder13() {
        SingularBridge.trackingUnder13();
    }

    static stopAllTracking() {
        SingularBridge.stopAllTracking();
    }

    static resumeAllTracking() {
        SingularBridge.resumeAllTracking();
    }

    async static isAllTrackingStopped() {
        return await SingularBridge.isAllTrackingStopped();
    }
}
