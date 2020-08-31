import {NativeEventEmitter, NativeModules} from 'react-native';
import {version} from './package.json';

const {SingularBridge} = NativeModules;

const SDK_NAME = 'ReactNative';
const SDK_VERSION = version;

export class Singular {

    static _singularNativeEmitter = new NativeEventEmitter(SingularBridge);

    static init(singularConfig) {
        this._singularLinkHandler = singularConfig.singularLinkHandler;
        this._conversionValueUpdatedHandler = singularConfig.conversionValueUpdatedHandler;

        this._singularNativeEmitter.addListener(
                'SingularLinkHandler',
                singularLinkParams => {
                    if (this._singularLinkHandler) {
                        this._singularLinkHandler(singularLinkParams);
                    }
                });

        this._singularNativeEmitter.addListener(
                'ConversionValueUpdatedHandler',
                conversionValue => {
                    if (this._conversionValueUpdatedHandler) {
                        this._conversionValueUpdatedHandler(conversionValue);
                    }
                });

        SingularBridge.init(JSON.stringify(singularConfig));
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
