import {NativeEventEmitter, NativeModules} from 'react-native';
import {version} from './package.json';

const {SingularBridge} = NativeModules;

const SDK_NAME = 'ReactNative';
const SDK_VERSION = version;

export class Singular {

    static _singularLinkHandlerEmitter = new NativeEventEmitter(SingularBridge);

    static init(singularConfig) {
        if (!singularConfig.singularLinkHandler) {
            SingularBridge.init(singularConfig.apikey, singularConfig.secret, singularConfig.customUserId, singularConfig.sessionTimeout);
        } else {
            this._singularLinkHandler = singularConfig.singularLinkHandler;

            this._singularLinkHandlerEmitter.addListener(
                'SingularLinkHandler',
                singularLinkParams => {
                    if (this._singularLinkHandler) {
                        this._singularLinkHandler(singularLinkParams);
                    }
                });

            SingularBridge.initWithSingularLink(singularConfig.apikey, singularConfig.secret, singularConfig.customUserId, singularConfig.sessionTimeout);
        }

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

    // This method will report revenue to Singular and will perform receipt validation (if enabled) on our backend.
    // The purchase object should be of SingularIOSPurchase / SingularAndroidPurchase type.
    static inAppPurchase(eventName, purchase) {
        this.eventWithArgs(eventName, purchase.getPurchaseValues());
    }

    // This method will report revenue to Singular and will perform receipt validation (if enabled) on our backend.
    // The purchase object should be of SingularIOSPurchase / SingularAndroidPurchase type.
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

    static isAllTrackingStopped() {
        return SingularBridge.isAllTrackingStopped();
    }

    static limitDataSharing(limitDataSharingValue) {
        SingularBridge.limitDataSharing(limitDataSharingValue);
    }

    static getLimitDataSharing() {
        return SingularBridge.getLimitDataSharing();
    }
}
