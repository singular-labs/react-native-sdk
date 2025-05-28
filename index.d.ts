type SerializableValue = boolean | number | string | null | SerializableArray | SerializableObject;
type SerializableArray = Array<SerializableValue>;
type SerializableObject = {
    [key in string | number]: SerializableValue;
};

export interface SingularLinkParams {
    deeplink: string;
    passthrough: string;
    isDeferred: boolean;
    urlParameters: Map<string, string>;
}

export class SingularConfig {
    constructor(apikey: string, secret: string);
    
    withSessionTimeoutInSec(sessionTimeout: number): SingularConfig;
    withCustomUserId(customUserId: string): SingularConfig;
    withSingularLink(handler: (params: SingularLinkParams) => void): SingularConfig;
    withSkAdNetworkEnabled(enabled: boolean): SingularConfig;
    withClipboardAttribution(): SingularConfig;
    withManualSkanConversionManagement(): SingularConfig;
    withConversionValueUpdatedHandler(handler: (value: number) => void): SingularConfig;
    withConversionValuesUpdatedHandler(handler: (fineValue: number, coarseValue: number, lockWindow: boolean) => void): SingularConfig;
    withWaitForTrackingAuthorizationWithTimeoutInterval(interval: number): SingularConfig;
    withShortLinkResolveTimeout(shortLinkResolveTimeout: number): SingularConfig;
    withLimitDataSharing(shouldLimitDataSharing: boolean): SingularConfig;
    withGlobalProperty(key: string, value: string, overrideExisting: boolean): SingularConfig;
    withOAIDCollection(): SingularConfig;
    withLoggingEnabled(): SingularConfig;
    withLogLevel(level: number): SingularConfig;
    withEspDomains(domains: [string]) : SingularConfig;
    withFacebookAppId(appId: string): SingularConfig;
    withDeviceAttributionCallbackHandler(deviceAttributionCallbackHandler:(attributes: Map<string, any>) => void): SingularConfig;
    withCustomSdid(customSdid: string, didSetSdidCallback: (result: string) => void, sdidReceivedCallback: (result: string) => void): SingularConfig;
    withPushNotificationsLinkPaths(pushNotificationsLinkPaths: [[string]]) : SingularConfig;
    withBrandedDomains(domains: [string]) : SingularConfig;
    withLimitAdvertisingIdentifiers(limitAdvertisingIdentifiers: boolean): SingularConfig;
}

export class SingularPurchase {
    constructor(revenue: number, currency: string);

    getPurchaseValues(): SerializableObject;
}

export class SingularIOSPurchase extends SingularPurchase {
    constructor(revenue: number, currency: string, productId: string, transactionId: string, receipt: string);
}

export class SingularAndroidPurchase extends SingularPurchase {
    constructor(revenue: number, currency: string, receipt: string, signature: string);
}

export class Singular {
    static init(config: SingularConfig): void;

    static setCustomUserId(customUserId: string): void;
    static unsetCustomUserId(): void;
    static setDeviceCustomUserId(customUserId: string): void;

    static event(eventName: string): void;
    static eventWithArgs(eventName: string, args: SerializableObject): void;

    static revenue(currency: string, amount: number): void;
    static revenueWithArgs(currency: string, amount: number, args: SerializableObject): void;
    static customRevenue(eventName: string, currency: string, amount: number): void;
    static customRevenueWithArgs(eventName: string, currency: string, amount: number, args: SerializableObject): void;

    static inAppPurchase(eventName: string, purchase: SingularPurchase): void;
    static inAppPurchaseWithArgs(eventName: string, purchase: SingularPurchase, args: SerializableObject): void;

    static setUninstallToken(token: string): void;

    static trackingOptIn(): void;
    static trackingUnder13(): void;
    static stopAllTracking(): void;
    static resumeAllTracking(): void;
    static isAllTrackingStopped(): boolean;

    static limitDataSharing(shoudlLimitDataSharing: boolean): void;
    static getLimitDataSharing(): boolean;

    static setGlobalProperty(key: string, value: string, overrideExisting: boolean): boolean;
    static unsetGlobalProperty(key: string): void;
    static clearGlobalProperties(): void;
    static getGlobalProperties(): SerializableObject;

    static skanUpdateConversionValue(conversionValue: number): boolean;
    static skanUpdateConversionValues(conversionValue: number, coarse: number, lock: boolean): void;
    static skanGetConversionValue(): number | null;
    static skanRegisterAppForAdNetworkAttribution(): void;
    
    static createReferrerShortLink(baseLink: string, referrerName: string, referrerId: string, passthroughParams: SerializableObject, completionHandler: (result: string, error: string) => void): void;

    static adRevenue(adData: SingularAdData): void;
    
    static setGlobalProperty(key: string, value: string, overrideExisting: boolean): boolean;
    static unsetGlobalProperty(key: string): void;
    static clearGlobalProperties(): void;
    static getGlobalProperties(): Map<string, string>;

    static handlePushNotification(pushNotificationPayload: boolean): void;
    static setLimitAdvertisingIdentifiers(enabled: boolean): void;
}

export class SingularAdData {
    constructor(adPlatform: string, currency: string, revenue: number);
    
    withNetworkName(networkName: string): SingularAdData;
    withAdType(adType: string): SingularAdData;
    withGroupType(adGroupType: string): SingularAdData;
    withImpressionId(impressionId: string): SingularAdData;
    withAdPlacementName(adPlacementName: string): SingularAdData;
    withAdUnitId(adUnitId: string): SingularAdData;
    withAdGroupId(adGroupId: string): SingularAdData;
    withAdGroupName(adGroupName: string): SingularAdData;
    withAdGroupPriority(adGroupPriority: string): SingularAdData;
    withPrecision(precision: string): SingularAdData;
    withPlacementId(placementIdstring: string): SingularAdData;
    withAdUnitName(adUnitName: string): SingularAdData;
}

import { Events } from './Events'
import { Attributes } from './Attributes'

declare const Events: {
    sngRate:string,
    sngSpentCredits:string,
    sngTutorialComplete:string
    sngLogin:string,
    sngStartTrial:string,
    sngSubscribe:string,
    sngBook:string,
    sngContentViewList:string,
    sngInvite:string,
    sngShare:string,
    sngSubmitApplication:string,
    sngUpdate:string,
    sngEcommercePurchase:string,
    sngViewCart:string,
    sngAchievementUnlocked:string,
    sngAddPaymentInfo:string,
    sngAddToCart:string,
    sngAddToWishlist:string,
    sngCheckoutInitiated:string,
    sngCompleteRegistration:string,
    sngContentView:string,
    sngLevelAchieved:string,
    sngSearch:string
};
   
declare const Attributes: {
    sngAttrFromDate:string,
    sngAttrToDate:string,
    sngAttrAchievementId:string,
    sngAttrContent:string,
    sngAttrContentId:string,
    sngAttrContentList:string,
    sngAttrContentType:string,
    sngAttrCouponCode:string,
    sngAttrDeepLink:string,
    sngAttrEventEnd:string,
    sngAttrEventStart:string,
    sngAttrHotelScore:string,
    sngAttrItemDescription:string,
    sngAttrItemPrice:string,
    sngAttrLatitude:string,
    sngAttrLevel:string,
    sngAttrLocation:string,
    sngAttrLocationAddressCountry:string,
    sngAttrLocationAddressRegionOrProvince:string,
    sngAttrLocationAddressStreet:string,
    sngAttrLongitude:string,
    sngAttrMax:string,
    sngAttrNewVersion:string,
    sngAttrOrigin:string,
    sngAttrPaymentInfoAvailable:string,
    sngAttrQuantity:string,
    sngAttrRating:string,
    sngAttrRegistrationMethod:string,
    sngAttrReviewText:string,
    sngAttrScore:string,
    sngAttrSearchString:string,
    sngAttrSubscriptionId:string,
    sngAttrSuccess:string,
    sngAttrTransactionId:string,
    sngAttrTutorialId:string,
    sngAttrValid:string
}

export { Events };
export { Attributes };
