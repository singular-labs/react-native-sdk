import {NativeEventEmitter, NativeModules, Platform} from 'react-native';
import {version} from './package.json';

const {SingularBridge} = NativeModules;

const SDK_NAME = 'ReactNative';
const SDK_VERSION = version;
const ADMON_REVENUE_EVENT_NAME = '__ADMON_USER_LEVEL_REVENUE__';

export class Singular {

    static _singularNativeEmitter = new NativeEventEmitter(SingularBridge);

    static init(singularConfig) {
        this._singularLinkHandler = singularConfig.singularLinkHandler;
        this._conversionValueUpdatedHandler = singularConfig.conversionValueUpdatedHandler;
        this._conversionValuesUpdatedHandler = singularConfig.conversionValuesUpdatedHandler;
        this._deviceAttributionCallbackHandler = singularConfig.deviceAttributionCallbackHandler;

        this._didSetSdidCallback = singularConfig.didSetSdidCallback;
        this._sdidReceivedCallback = singularConfig.sdidReceivedCallback;

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

        this._singularNativeEmitter.addListener(
            'ConversionValuesUpdatedHandler',
            updatedConversionValues => {
                if (this._conversionValuesUpdatedHandler) {
                    this._conversionValuesUpdatedHandler(updatedConversionValues);
                }
            });

        this._singularNativeEmitter.addListener(
             'DeviceAttributionCallbackHandler',
              attributes => {
                if (this._deviceAttributionCallbackHandler) {
                     this._deviceAttributionCallbackHandler(attributes);
                }
        });

        this._singularNativeEmitter.addListener(
            'SdidReceivedCallback',
            result => {
                if (this._sdidReceivedCallback) {
                    this._sdidReceivedCallback(result);
                }
            });

        this._singularNativeEmitter.addListener(
            'DidSetSdidCallback',
            result => {
                if (this._didSetSdidCallback) {
                    this._didSetSdidCallback(result);
                }
            });

        SingularBridge.init(JSON.stringify(singularConfig));
        SingularBridge.setReactSDKVersion(SDK_NAME, SDK_VERSION);
    }

    static createReferrerShortLink(baseLink, referrerName, referrerId, passthroughParams, completionHandler){
        let eventSubscription = this._singularNativeEmitter.addListener(
            'ShortLinkHandler',
            (res) => {
                eventSubscription.remove();
                if (completionHandler) {
                    completionHandler(res.data, res.error && res.error.length ? res.error: undefined);
                }
            });
        SingularBridge.createReferrerShortLink(baseLink, referrerName, referrerId, JSON.stringify(passthroughParams));
    } 

    static setCustomUserId(customUserId) {
        SingularBridge.setCustomUserId(customUserId);
    }

    static unsetCustomUserId() {
        SingularBridge.unsetCustomUserId();
    }

    static setDeviceCustomUserId(customUserId) {
        SingularBridge.setDeviceCustomUserId(customUserId);
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

    // SKAN methods
    static skanUpdateConversionValue(conversionValue) {
        if (Platform.OS === 'ios') {
            return SingularBridge.skanUpdateConversionValue(conversionValue);
        }
        return true
    }
    
    static skanUpdateConversionValues(conversionValue, coarse, lock) {
        if (Platform.OS === 'ios') {
            SingularBridge.skanUpdateConversionValues(conversionValue, coarse, lock);
        }
    }

    static skanGetConversionValue() {
        if (Platform.OS === 'ios') {
            return SingularBridge.skanGetConversionValue();
        }
        return null
    }

    static skanRegisterAppForAdNetworkAttribution() {
        if (Platform.OS === 'ios') {
            SingularBridge.skanRegisterAppForAdNetworkAttribution();
        }
    }

    static adRevenue(adData) {
        if (!adData || !adData.hasRequiredParams()) {
            return;
        }
        this.eventWithArgs(ADMON_REVENUE_EVENT_NAME, adData);
    }

    static setGlobalProperty(key, value,overrideExisting) {
        return SingularBridge.setGlobalProperty(key, value, overrideExisting);
    }

    static unsetGlobalProperty(key) {
        SingularBridge.unsetGlobalProperty(key);
    }

    static clearGlobalProperties() {
        SingularBridge.clearGlobalProperties();
    }

    static getGlobalProperties() {
        return SingularBridge.getGlobalProperties();
    }
}
