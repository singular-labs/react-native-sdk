import { TurboModule, TurboModuleRegistry } from 'react-native';

export type SerializableValue = boolean | number | string | null | SerializableArray | SerializableObject;

export type SerializableArray = Array<SerializableValue>;

export type SerializableObject = {
  [key: string]: SerializableValue;
};

export interface SingularLinkParams {
    deeplink: string;
    passthrough: string;
    isDeferred: boolean;
    urlParameters: { [key: string]: string };
}

export interface SingularPurchase {
  revenue: number;
  currency: string;
  productId?: string;
  transactionId?: string;
  receipt?: string;
  receipt_signature?: string;
}

export interface SingularAdData {
  ad_platform: string;
  ad_currency: string;
  ad_revenue: number;
  ad_mediation_platform?: string;
  ad_type?: string;
  ad_group_type?: string;
  ad_impression_id?: string;
  ad_placement_name?: string;
  ad_unit_id?: string;
  ad_unit_name?: string;
  ad_group_id?: string;
  ad_group_name?: string;
  ad_group_priority?: number;
  ad_precision?: string;
  ad_placement_id?: string;
}

export interface SingularConfig {
  apikey: string;
  secret: string;
  sessionTimeout?: number;
  customUserId?: string;
  shortLinkResolveTimeout?: number;
  skAdNetworkEnabled?: boolean;
  clipboardAttribution?: boolean;
  manualSkanConversionManagement?: boolean;
  waitForTrackingAuthorizationWithTimeoutInterval?: number;
  customSdid?: string;
  limitDataSharing?: boolean | null;
  globalProperties?: SerializableObject;
  collectOAID?: boolean;
  enableLogging?: boolean;
  espDomains?: string[];
  facebookAppId?: string;
  pushNotificationsLinkPaths?: string[][];
  brandedDomains?: string[];
  limitAdvertisingIdentifiers?: boolean;
  enableOdmWithTimeoutInterval?: number;
}

export interface Spec extends TurboModule {
    init(config: SingularConfig): void;

    // Required for emitting events - these are called by RN internally
    addListener(eventType: string): void;
    removeListeners(count: number): void;

    event(eventName: string): void;
    eventWithArgs(eventName: string, args: SerializableObject): void;

    setCustomUserId(customUserId: string): void;
    unsetCustomUserId(): void;
    setDeviceCustomUserId(customUserId: string): void;

    revenue(currency: string, amount: number): void;
    revenueWithArgs(currency: string, amount: number, args: SerializableObject): void;
    customRevenue(eventName: string, currency: string, amount: number): void;
    customRevenueWithArgs(eventName: string, currency: string, amount: number, args: SerializableObject): void;

    inAppPurchase(eventName: string, purchase: SingularPurchase): void;
    inAppPurchaseWithArgs(eventName: string, purchase: SingularPurchase, args: SerializableObject): void;

    setUninstallToken(token: string): void;

    trackingOptIn(): void;
    trackingUnder13(): void;
    stopAllTracking(): void;
    resumeAllTracking(): void;
    isAllTrackingStopped(): boolean;

    limitDataSharing(shouldLimitDataSharing: boolean): void;
    getLimitDataSharing(): boolean;

    setGlobalProperty(key: string, value: string, overrideExisting: boolean): boolean;
    unsetGlobalProperty(key: string): void;
    clearGlobalProperties(): void;
    getGlobalProperties(): SerializableObject;

    createReferrerShortLink(baseLink: string, referrerName: string, referrerId: string, passthroughParams: SerializableObject, completionHandler: (result: string, error: string) => void): void;

    adRevenue(adData: SingularAdData): void;

    setLimitAdvertisingIdentifiers(enabled: boolean): void;

    // iOS-specific methods
    skanUpdateConversionValue(conversionValue: number): boolean;
    skanUpdateConversionValues(conversionValue: number, coarse: number, lock: boolean): void;
    skanGetConversionValue(): number | null;
    skanRegisterAppForAdNetworkAttribution(): void;
    handlePushNotification(pushNotificationPayload: SerializableObject): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('SingularBridge');
