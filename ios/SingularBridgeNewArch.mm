#import "SingularBridge.h"
#import "SingularHelper.h"

#import <Singular/Singular.h>
#import <Singular/SingularConfig.h>
#import <Singular/SingularLinkParams.h>

#if RCT_NEW_ARCH_ENABLED
#import "NativeSingular.h"
#import "NativeSingularJSI.h"
#import <ReactCommon/RCTTurboModule.h>
#endif

@implementation SingularBridge

static RCTEventEmitter* eventEmitter;

static NSString *apikey;
static NSString *secret;
static NSDictionary *launchOptions;

static NSString* const version = @"4.0.0";
static NSString* const wrapper = @"ReactNative";

// Ad Revenue key constants
static NSString* const kAdRevenueEvent = @"__ADMON_USER_LEVEL_REVENUE__";
static NSString* const kAdPlatform = @"ad_platform";
static NSString* const kAdCurrency = @"ad_currency";
static NSString* const kAdRevenue = @"ad_revenue";
static NSString* const kAdRevenueR = @"r";
static NSString* const kAdRevenuePCC = @"pcc";
static NSString* const kIsAdmonRevenue = @"is_admon_revenue";
static NSString* const kIsRevenueEvent = @"is_revenue_event";
static NSString* const kAdMediationPlatform = @"ad_mediation_platform";
static NSString* const kAdType = @"ad_type";
static NSString* const kAdGroupType = @"ad_group_type";
static NSString* const kAdImpressionId = @"ad_impression_id";
static NSString* const kAdPlacementName = @"ad_placement_name";
static NSString* const kAdUnitId = @"ad_unit_id";
static NSString* const kAdUnitName = @"ad_unit_name";
static NSString* const kAdGroupId = @"ad_group_id";
static NSString* const kAdGroupName = @"ad_group_name";
static NSString* const kAdGroupPriority = @"ad_group_priority";
static NSString* const kAdPrecision = @"ad_precision";
static NSString* const kAdPlacementId = @"ad_placement_id";

// Purchase key constants
static NSString* const kPurchaseProductKey = @"pk";
static NSString* const kPurchaseTransactionId = @"pti";
static NSString* const kPurchaseReceipt = @"ptr";

// Singular Link key constants
static NSString* const kSingularLinkDeeplink = @"deeplink";
static NSString* const kSingularLinkPassthrough = @"passthrough";
static NSString* const kSingularLinkIsDeferred = @"isDeferred";
static NSString* const kSingularLinkUrlParameters = @"urlParameters";

RCT_EXPORT_MODULE(SingularBridge);

+(void)startSessionWithLaunchOptions:(NSDictionary*)options {
    launchOptions = options;
}

// Handling Singular Link when the app is opened from a Singular Link while it was in the background.
// The client will need to call this method in the AppDelegate in continueUserActivity.
+(void)startSessionWithUserActivity:(NSUserActivity*)userActivity {
    [Singular startSession:apikey
                   withKey:secret
           andUserActivity:userActivity
   withSingularLinkHandler:^(SingularLinkParams * params) {
        NSDictionary *linkData = @{
            kSingularLinkDeeplink: [params getDeepLink] ? [params getDeepLink] : @"",
            kSingularLinkPassthrough: [params getPassthrough] ? [params getPassthrough] : @"",
            kSingularLinkIsDeferred: [params isDeferred] ? @YES : @NO,
            kSingularLinkUrlParameters: [params getUrlParameters] ? [params getUrlParameters] : @{}
        };
        
        [eventEmitter sendEventWithName:SINGULAR_LINK_HANDLER_CONST body:linkData];
    }];
}

- (NSArray<NSString *> *)supportedEvents {
    return [SingularHelper supportedEvents];
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeSingularSpecJSI>(params);
}

#pragma mark - SingularSpec Protocol

- (void)init:(JS::NativeSingular::SingularConfig &)config {
    SingularConfig *singularConfig = [[SingularConfig alloc] initWithApiKey:config.apikey() andSecret:config.secret()];
    singularConfig.launchOptions = launchOptions;
    
    apikey = config.apikey();
    secret = config.secret();
    
    if (config.skAdNetworkEnabled().has_value()) {
        singularConfig.skAdNetworkEnabled = config.skAdNetworkEnabled().value();
    }
    if (config.clipboardAttribution().has_value()) {
        singularConfig.clipboardAttribution = config.clipboardAttribution().value();
    }
    if (config.shortLinkResolveTimeout().has_value()) {
        singularConfig.shortLinkResolveTimeOut = config.shortLinkResolveTimeout().value();
    }
    if (config.manualSkanConversionManagement().has_value()) {
        singularConfig.manualSkanConversionManagement = config.manualSkanConversionManagement().value();
    }
    if (config.waitForTrackingAuthorizationWithTimeoutInterval().has_value()) {
        singularConfig.waitForTrackingAuthorizationWithTimeoutInterval = config.waitForTrackingAuthorizationWithTimeoutInterval().value();
    }
    if (config.customSdid()) {
        singularConfig.customSdid = config.customSdid();
    }
    if (config.limitAdvertisingIdentifiers().has_value()) {
        singularConfig.limitAdvertisingIdentifiers = config.limitAdvertisingIdentifiers().value();
    }
    if (config.enableOdmWithTimeoutInterval().has_value()) {
        singularConfig.enableOdmWithTimeoutInterval = config.enableOdmWithTimeoutInterval().value();
    }
    if (config.sessionTimeout().has_value()) {
        [SingularHelper setSessionTimeout:(int)config.sessionTimeout().value()];
    }
    if (config.limitDataSharing().has_value()) {
        [SingularHelper limitDataSharing:config.limitDataSharing().value()];
    }
    if (config.customUserId()) {
        [SingularHelper setCustomUserId:config.customUserId()];
    }

    if (config.espDomains().has_value()) {
        auto espDomains = config.espDomains().value();
        NSMutableArray<NSString *> *espDomainsArray = [[NSMutableArray alloc] init];
        for (const auto& domain : espDomains) {
            [espDomainsArray addObject:domain];
        }
        singularConfig.espDomains = [espDomainsArray copy];
    }
    
    if (config.brandedDomains().has_value()) {
        auto brandedDomains = config.brandedDomains().value();
        NSMutableArray<NSString *> *brandedDomainsArray = [[NSMutableArray alloc] init];
        for (const auto& domain : brandedDomains) {
            [brandedDomainsArray addObject:domain];
        }
        singularConfig.brandedDomains = [brandedDomainsArray copy];
    }
    
    if (config.pushNotificationsLinkPaths().has_value()) {
        auto pushPaths = config.pushNotificationsLinkPaths().value();
        NSMutableArray<NSArray<NSString *> *> *pushPathsArray = [[NSMutableArray alloc] init];
        for (const auto& pathGroup : pushPaths) {
            NSMutableArray<NSString *> *pathGroupArray = [[NSMutableArray alloc] init];
            for (const auto& path : pathGroup) {
                [pathGroupArray addObject:path];
            }
            [pushPathsArray addObject:[pathGroupArray copy]];
        }
        singularConfig.pushNotificationLinkPath = [pushPathsArray copy];
    }
    
    if (config.globalProperties()) {
        NSDictionary *globalProps = (NSDictionary *)config.globalProperties();
        if (globalProps && [globalProps count] > 0) {
            for (NSDictionary *property in [globalProps allValues]) {
                if ([property isKindOfClass:[NSDictionary class]]) {
                    NSString *propertyKey = [property objectForKey:@"Key"];
                    NSString *propertyValue = [property objectForKey:@"Value"];
                    BOOL overrideExisting = [[property objectForKey:@"OverrideExisting"] boolValue];
                    
                    if (propertyKey && propertyValue) {
                        [singularConfig setGlobalProperty:propertyKey
                                                withValue:propertyValue
                                         overrideExisting:overrideExisting];
                    }
                }
            }
        }
    }
    
    singularConfig.singularLinksHandler = ^(SingularLinkParams * _Nonnull params) {
        NSDictionary *linkData = @{
            kSingularLinkDeeplink: [params getDeepLink] ?: @"",
            kSingularLinkPassthrough: [params getPassthrough] ?: @"",
            kSingularLinkIsDeferred: @([params isDeferred]),
            kSingularLinkUrlParameters: [params getUrlParameters] ?: @{}
        };
        
        [self sendEventWithName:SINGULAR_LINK_HANDLER_CONST body:linkData];
    };
    
    singularConfig.sdidReceivedHandler = ^(NSString * _Nonnull sdid) {
        [eventEmitter sendEventWithName:SDID_RECEIVED_CALLBACK_CONST body:sdid];
    };
    
    singularConfig.didSetSdidHandler = ^(NSString * _Nonnull sdid) {
        [eventEmitter sendEventWithName:SDID_SET_CALLBACK_CONST body:sdid];
    };
    
    singularConfig.conversionValueUpdatedCallback = ^(NSInteger value) {
        [eventEmitter sendEventWithName:CONVERSION_VALUE_UPDATED_HANDLER_CONST body:@(value)];
    };
    
    singularConfig.conversionValuesUpdatedCallback = ^(NSNumber *fineValue, NSNumber *coarseValue, BOOL lockWindow) {
        NSInteger fine = -1;
        NSInteger coarse = -1;
        
        if (fineValue != nil) {
            fine = [fineValue intValue];
        }
        if (coarseValue != nil) {
            coarse = [coarseValue intValue];
        }
        
        NSDictionary *conversionValues = @{
            @"conversionValue": @(fine),
            @"coarse": @(coarse),
            @"lock": @(lockWindow)
        };
        
        [eventEmitter sendEventWithName:CONVERSION_VALUES_UPDATED_HANDLER_CONST body:conversionValues];
    };
    
    singularConfig.deviceAttributionCallback = ^(NSDictionary *attributionData) {
        [eventEmitter sendEventWithName:DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST body:attributionData];
    };
    
    eventEmitter = self;
    
    [SingularHelper initWithConfig:singularConfig];
    [SingularHelper setReactSDKVersion:wrapper version:version];
}

#pragma mark - Event Methods

- (void)event:(NSString *)eventName {
    [SingularHelper event:eventName];
}

- (void)eventWithArgs:(NSString *)eventName args:(NSDictionary *)args {
    [SingularHelper eventWithArgs:eventName args:args];
}

- (void)adRevenue:(JS::NativeSingular::SingularAdData &)adData {
    NSMutableDictionary *adRevenueData = [NSMutableDictionary dictionary];
    adRevenueData[kAdPlatform] = adData.ad_platform();
    adRevenueData[kAdCurrency] = adData.ad_currency();
    adRevenueData[kAdRevenue] = @(adData.ad_revenue());
    adRevenueData[kAdRevenueR] = @(adData.ad_revenue());
    adRevenueData[kAdRevenuePCC] = adData.ad_currency();
    adRevenueData[kIsAdmonRevenue] = @(YES);
    adRevenueData[kIsRevenueEvent] = @(YES);

    if (adData.ad_mediation_platform()) {
        adRevenueData[kAdMediationPlatform] = adData.ad_mediation_platform();
    }
    if (adData.ad_type()) {
        adRevenueData[kAdType] = adData.ad_type();
    }
    if (adData.ad_group_type()) {
        adRevenueData[kAdGroupType] = adData.ad_group_type();
    }
    if (adData.ad_impression_id()) {
        adRevenueData[kAdImpressionId] = adData.ad_impression_id();
    }
    if (adData.ad_placement_name()) {
        adRevenueData[kAdPlacementName] = adData.ad_placement_name();
    }
    if (adData.ad_unit_id()) {
        adRevenueData[kAdUnitId] = adData.ad_unit_id();
    }
    if (adData.ad_unit_name()) {
        adRevenueData[kAdUnitName] = adData.ad_unit_name();
    }
    if (adData.ad_group_id()) {
        adRevenueData[kAdGroupId] = adData.ad_group_id();
    }
    if (adData.ad_group_name()) {
        adRevenueData[kAdGroupName] = adData.ad_group_name();
    }
    if (adData.ad_group_priority().has_value()) {
        adRevenueData[kAdGroupPriority] = @(adData.ad_group_priority().value());
    }
    if (adData.ad_precision()) {
        adRevenueData[kAdPrecision] = adData.ad_precision();
    }
    if (adData.ad_placement_id()) {
        adRevenueData[kAdPlacementId] = adData.ad_placement_id();
    }

    [SingularHelper eventWithArgs:kAdRevenueEvent args:adRevenueData];
}

- (NSDictionary *)getPurchaseValues:(JS::NativeSingular::SingularPurchase &)purchase {
    NSMutableDictionary *purchaseValues = [NSMutableDictionary dictionary];
    purchaseValues[kAdRevenueR] = @(purchase.revenue());
    purchaseValues[kAdRevenuePCC] = purchase.currency();
    purchaseValues[kIsRevenueEvent] = @(YES);

    if (purchase.productId()) {
        purchaseValues[kPurchaseProductKey] = purchase.productId();
    }
    if (purchase.transactionId()) {
        purchaseValues[kPurchaseTransactionId] = purchase.transactionId();
    }
    if (purchase.receipt()) {
        purchaseValues[kPurchaseReceipt] = purchase.receipt();
    }

    return purchaseValues;
}

- (void)inAppPurchase:(NSString *)eventName purchase:(JS::NativeSingular::SingularPurchase &)purchase {
    NSDictionary *purchaseValues = [self getPurchaseValues:purchase];
    [SingularHelper eventWithArgs:eventName args:purchaseValues];
}

- (void)inAppPurchaseWithArgs:(NSString *)eventName purchase:(JS::NativeSingular::SingularPurchase &)purchase args:(NSDictionary *)args {
    NSDictionary *purchaseValues = [self getPurchaseValues:purchase];
    NSMutableDictionary *combinedArgs = [purchaseValues mutableCopy];
    [combinedArgs addEntriesFromDictionary:args];

    [SingularHelper eventWithArgs:eventName args:combinedArgs];
}

- (void)clearGlobalProperties {
    [SingularHelper clearGlobalProperties];
}

- (void)createReferrerShortLink:(NSString *)baseLink referrerName:(NSString *)referrerName referrerId:(NSString *)referrerId passthroughParams:(NSDictionary *)passthroughParams completionHandler:(RCTResponseSenderBlock)completionHandler {
    [SingularHelper createReferrerShortLink:baseLink referrerName:referrerName referrerId:referrerId passthroughParams:passthroughParams completionHandler:^(NSString *result, NSString *error) {
        if (result) {
            completionHandler(@[result, @""]);
        } else {
            completionHandler(@[@"", error]);
        }
    }];
}

- (void)customRevenue:(NSString *)eventName currency:(NSString *)currency amount:(double)amount {
    [SingularHelper customRevenue:eventName currency:currency amount:amount];
}

- (void)customRevenueWithArgs:(NSString *)eventName currency:(NSString *)currency amount:(double)amount args:(NSDictionary *)args {
    [SingularHelper customRevenueWithArgs:eventName currency:currency amount:amount args:args];
}

- (NSDictionary *)getGlobalProperties {
    return [SingularHelper getGlobalProperties];
}

- (NSNumber *)getLimitDataSharing {
    BOOL isLimited = [SingularHelper getLimitDataSharing];
    return @(isLimited);
}

- (void)handlePushNotification:(NSDictionary *)pushNotificationPayload {
    [SingularHelper handlePushNotification:pushNotificationPayload];
}

- (NSNumber *)isAllTrackingStopped {
    BOOL isStopped = [SingularHelper isAllTrackingStopped];
    return @(isStopped);
}

- (void)limitDataSharing:(BOOL)shouldLimitDataSharing {
    [SingularHelper limitDataSharing:shouldLimitDataSharing];
}

- (void)resumeAllTracking {
    [SingularHelper resumeAllTracking];
}

- (void)revenue:(NSString *)currency amount:(double)amount {
    [SingularHelper revenue:currency amount:amount];
}

- (void)revenueWithArgs:(NSString *)currency amount:(double)amount args:(NSDictionary *)args {
    [SingularHelper revenueWithArgs:currency amount:amount args:args];
}

- (void)setCustomUserId:(NSString *)customUserId {
    [SingularHelper setCustomUserId:customUserId];
}

- (void)setDeviceCustomUserId:(NSString *)customUserId {
    [SingularHelper setDeviceCustomUserId:customUserId];
}

- (NSNumber *)setGlobalProperty:(NSString *)key value:(NSString *)value overrideExisting:(BOOL)overrideExisting {
    BOOL success = [SingularHelper setGlobalProperty:key value:value overrideExisting:overrideExisting];
    return @(success);
}

- (void)setLimitAdvertisingIdentifiers:(BOOL)enabled {
    [SingularHelper setLimitAdvertisingIdentifiers:enabled];
}

- (void)setUninstallToken:(NSString *)token {
    [SingularHelper setUninstallToken:token];
}

- (NSNumber *)skanGetConversionValue {
    return [SingularHelper skanGetConversionValue];
}

- (void)skanRegisterAppForAdNetworkAttribution {
    [SingularHelper skanRegisterAppForAdNetworkAttribution];
}

- (NSNumber *)skanUpdateConversionValue:(double)conversionValue {
    BOOL success = [SingularHelper skanUpdateConversionValue:(NSInteger)conversionValue];
    return @(success);
}

- (void)skanUpdateConversionValues:(double)conversionValue coarse:(double)coarse lock:(BOOL)lock {
    [SingularHelper skanUpdateConversionValues:(NSInteger)conversionValue coarse:(NSInteger)coarse lock:lock];
}

- (void)stopAllTracking {
    [SingularHelper stopAllTracking];
}

- (void)trackingOptIn {
    [SingularHelper trackingOptIn];
}

- (void)trackingUnder13 {
    [SingularHelper trackingUnder13];
}

- (void)unsetCustomUserId {
    [SingularHelper unsetCustomUserId];
}

- (void)unsetGlobalProperty:(NSString *)key {
    [SingularHelper unsetGlobalProperty:key];
}

@end

