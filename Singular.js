import {NativeEventEmitter, NativeModules, Platform, TurboModuleRegistry} from 'react-native';
import packageJson from './package.json';

const SDK_NAME = 'ReactNative';
const SDK_VERSION = packageJson.version;
const ADMON_REVENUE_EVENT_NAME = '__ADMON_USER_LEVEL_REVENUE__';

let SingularBridge;
let isNewArch = false;

const isNewArchitectureEnabled  = global?.nativeFabricUIManager != null

try {
  if (isNewArchitectureEnabled) {
    const turboModule = TurboModuleRegistry?.get?.('SingularBridge');
    if (turboModule) {
      SingularBridge = turboModule;
      isNewArch = true;
      __DEV__ && console.log('[Singular SDK] Using New Architecture (TurboModule)');
    } else {
      throw new Error('TurboModule not available');
    }
  } else {
    throw new Error('New Architecture not enabled');
  }
} catch (error) {
  SingularBridge = NativeModules.SingularBridge;
  __DEV__ && console.log('[Singular SDK] Using Old Architecture (Legacy Bridge)');
}

export class Singular {
    static _singularNativeEmitter = new NativeEventEmitter(SingularBridge);

    static init(singularConfig) {
      if (!singularConfig) {
        __DEV__ && console.log('[Singular SDK] singularConfig must be a non-null object');
        return;
      }

      const isPlainObject =
        typeof singularConfig === 'object' &&
        (!singularConfig.constructor || singularConfig.constructor.name === 'Object');

      const handlerProperties = [
        'singularLinkHandler',
        'conversionValueUpdatedHandler',
        'conversionValuesUpdatedHandler',
        'deviceAttributionCallbackHandler',
        'didSetSdidCallback',
        'sdidReceivedCallback',
      ];

      if (singularConfig.singularLinkHandler && !this._singularLinkHandler) {
        this._singularLinkHandler = singularConfig.singularLinkHandler;
      }
      if (singularConfig.conversionValueUpdatedHandler && !this._conversionValueUpdatedHandler) {
        this._conversionValueUpdatedHandler = singularConfig.conversionValueUpdatedHandler;
      }
      if (singularConfig.conversionValuesUpdatedHandler && !this._conversionValuesUpdatedHandler) {
        this._conversionValuesUpdatedHandler = singularConfig.conversionValuesUpdatedHandler;
      }
      if (singularConfig.deviceAttributionCallbackHandler && !this._deviceAttributionCallbackHandler) {
        this._deviceAttributionCallbackHandler = singularConfig.deviceAttributionCallbackHandler;
      }
      if (singularConfig.didSetSdidCallback && !this._didSetSdidCallback) {
        this._didSetSdidCallback = singularConfig.didSetSdidCallback;
      }
      if (singularConfig.sdidReceivedCallback && !this._sdidReceivedCallback) {
        this._sdidReceivedCallback = singularConfig.sdidReceivedCallback;
      }

      // register callback listeners
      this.registerListeners();

      const configWithoutHandlers = Object.keys(singularConfig)
        .filter(
          (key) =>
            !handlerProperties.includes(key) && singularConfig[key] !== undefined
        )
        .reduce((obj, key) => {
          obj[key] = singularConfig[key];
          return obj;
        }, {});

      if (isNewArch) {
        // If it's not a plain object (e.g., SingularConfig instance), serialize it
        // Otherwise pass the plain object directly
        var configForTurbo = null;
        if (isPlainObject) {
            configForTurbo = configWithoutHandlers;
        } else {
            configForTurbo = JSON.parse(JSON.stringify(configWithoutHandlers));
        }

        SingularBridge.init(configForTurbo);
        return;
      }

      SingularBridge.init(JSON.stringify(singularConfig));
      SingularBridge.setReactSDKVersion(SDK_NAME, SDK_VERSION);
    }

    static registerListeners() {
        if (this._singularLinkHandler) {
            this._singularNativeEmitter.addListener('SingularLinkHandler',
                (params) => {
                    this._singularLinkHandler?.(params);
                });
        }

        if (this._deviceAttributionCallbackHandler) {
            this._singularNativeEmitter.addListener('DeviceAttributionCallbackHandler',
                (attributes) => this._deviceAttributionCallbackHandler?.(attributes));
        }

        if (this._conversionValueUpdatedHandler) {
            this._singularNativeEmitter.addListener('ConversionValueUpdatedHandler',
                (conversionValue) => this._conversionValueUpdatedHandler?.(conversionValue));
        }

        if (this._conversionValuesUpdatedHandler) {
            this._singularNativeEmitter.addListener('ConversionValuesUpdatedHandler',
                (updatedConversionValues) => this._conversionValuesUpdatedHandler?.(updatedConversionValues));
        }

        if (this._didSetSdidCallback) {
            this._singularNativeEmitter.addListener('DidSetSdidCallback',
                (result) => this._didSetSdidCallback?.(result));
        }

        if (this._sdidReceivedCallback) {
            this._singularNativeEmitter.addListener('SdidReceivedCallback',
                (result) => this._sdidReceivedCallback?.(result));
        }
    }

    static createReferrerShortLink(baseLink, referrerName, referrerId, passthroughParams, completionHandler) {
        if (isNewArch) {
            SingularBridge.createReferrerShortLink(baseLink, referrerName, referrerId, passthroughParams, completionHandler);
        } else {
            const subscription = this._singularNativeEmitter.addListener(
                'ShortLinkHandler',
                (res) => {
                    subscription.remove();
                    if (completionHandler) {
                        completionHandler(res.data, res.error && res.error.length ? res.error: undefined);
                    }
                });
            SingularBridge.createReferrerShortLink(baseLink, referrerName, referrerId, JSON.stringify(passthroughParams));
        }
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
        if (isNewArch) {
            SingularBridge.eventWithArgs(eventName, args);
        } else {
            SingularBridge.eventWithArgs(eventName, JSON.stringify(args));
        }
    }

    static revenue(currency, amount) {
        SingularBridge.revenue(currency, amount);
    }

    static revenueWithArgs(currency, amount, args) {
        if (isNewArch) {
            SingularBridge.revenueWithArgs(currency, amount, args);
        } else {
            SingularBridge.revenueWithArgs(currency, amount, JSON.stringify(args));
        }
    }

    static customRevenue(eventName, currency, amount) {
        SingularBridge.customRevenue(eventName, currency, amount);
    }

    static customRevenueWithArgs(eventName, currency, amount, args) {
        if (isNewArch) {
            SingularBridge.customRevenueWithArgs(eventName, currency, amount, args);
        } else {
            SingularBridge.customRevenueWithArgs(eventName, currency, amount, JSON.stringify(args));
        }
    }

    static inAppPurchase(eventName, purchase) {
        if (isNewArch) {
            if (purchase && typeof purchase.toSpecObject === 'function') {
                SingularBridge.inAppPurchase(eventName, purchase.toSpecObject());
            } else {
                const convertedPurchase = {
                    revenue: purchase.r || purchase.revenue,
                    currency: purchase.pcc || purchase.currency,
                    productId: purchase.pk || purchase.productId,
                    transactionId: purchase.pti || purchase.transactionId,
                    receipt: purchase.ptr || purchase.receipt,
                    receipt_signature: purchase.receipt_signature
                };
                SingularBridge.inAppPurchase(eventName, convertedPurchase);
            }
        } else {
            this.eventWithArgs(eventName, purchase.getPurchaseValues());
        }
    }

    static inAppPurchaseWithArgs(eventName, purchase, args) {
        if (isNewArch) {
            if (purchase && typeof purchase.toSpecObject === 'function') {
                SingularBridge.inAppPurchaseWithArgs(eventName, purchase.toSpecObject(), args);
            } else {
                const convertedPurchase = {
                    revenue: purchase.r || purchase.revenue,
                    currency: purchase.pcc || purchase.currency,
                    productId: purchase.pk || purchase.productId,
                    transactionId: purchase.pti || purchase.transactionId,
                    receipt: purchase.ptr || purchase.receipt,
                    receipt_signature: purchase.receipt_signature
                };
                SingularBridge.inAppPurchaseWithArgs(eventName, convertedPurchase, args);
            }
        } else {
            this.eventWithArgs(eventName, {...purchase.getPurchaseValues(), ...args});
        }
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
        if (!adData) {
           return;
        }

        if (typeof adData.hasRequiredParams === 'function') {
            if (!adData.hasRequiredParams()) {
                return;
            }
        }

        if (typeof adData.hasRequiredParams !== 'function') {
            const hasRequiredParams = adData.ad_platform && adData.ad_currency && adData.ad_revenue;
            if (!hasRequiredParams) {
                return;
            }
        }

        if (isNewArch) {
            SingularBridge.adRevenue(adData);
        } else {
            this.eventWithArgs(ADMON_REVENUE_EVENT_NAME, adData);
        }
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

    static handlePushNotification(pushNotificationPayload) {
        if (Platform.OS === 'ios') {
           SingularBridge.handlePushNotification(pushNotificationPayload);
        }
    }

    static setLimitAdvertisingIdentifiers(enabled) {
        SingularBridge.setLimitAdvertisingIdentifiers(enabled);
    }
}

export { SingularBridge };
