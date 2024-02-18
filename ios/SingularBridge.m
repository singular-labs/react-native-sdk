#import "SingularBridge.h"

#import <Singular/Singular.h>
#import <Singular/SingularConfig.h>

#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#elif __has_include(“RCTBridge.h”)
#else
#import “RCTBridge.h”
#endif

#if __has_include(<React/RCTEventDispatcher.h>)
#import <React/RCTEventDispatcher.h>
#elif __has_include(“RCTEventDispatcher.h”)
#else
#import “RCTEventDispatcher.h”
#endif

#define SINGULAR_LINK_HANDLER_CONST                 @"SingularLinkHandler"
#define CONVERSION_VALUE_UPDATED_HANDLER_CONST      @"ConversionValueUpdatedHandler"
#define DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST   @"DeviceAttributionCallbackHandler"
#define CONVERSION_VALUES_UPDATED_HANDLER_CONST     @"ConversionValuesUpdatedHandler"
#define SHORT_LINK_HANDLER_CONST                    @"ShortLinkHandler"
#define SDID_RECEIVED_CALLBACK_CONST                @"SdidReceivedCallback"
#define SDID_SET_CALLBACK_CONST                     @"DidSetSdidCallback"

@implementation SingularBridge
@synthesize bridge = _bridge;

static NSString* apikey;
static NSString* secret;
static NSDictionary* launchOptions;
static RCTEventEmitter* eventEmitter;

// Saving the launchOptions for later when the SDK is initialized to handle Singular Links.
// The client will need to call this method is the AppDelegate in didFinishLaunchingWithOptions.
+(void)startSessionWithLaunchOptions:(NSDictionary*)options{
    launchOptions = options;
}

// Handling Singular Link when the app is opened from a Singular Link while it was in the background.
// The client will need to call this method in the AppDelegate in continueUserActivity.
+(void)startSessionWithUserActivity:(NSUserActivity*)userActivity{
    [Singular startSession:apikey
                   withKey:secret
           andUserActivity:userActivity
   withSingularLinkHandler:^(SingularLinkParams * params){
        [SingularBridge handleSingularLink:params];
    }];
}

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
    return @[SINGULAR_LINK_HANDLER_CONST,
             CONVERSION_VALUE_UPDATED_HANDLER_CONST,
             SHORT_LINK_HANDLER_CONST,
             CONVERSION_VALUES_UPDATED_HANDLER_CONST,
             DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST,
             SDID_RECEIVED_CALLBACK_CONST,
             SDID_SET_CALLBACK_CONST];
}

// Init method using a json string representing the config
RCT_EXPORT_METHOD(init:(NSString*) jsonSingularConfig){
    NSDictionary* singularConfigDict = [SingularBridge jsonToDictionary:jsonSingularConfig];

    apikey = [singularConfigDict objectForKey:@"apikey"];
    secret = [singularConfigDict objectForKey:@"secret"];

    // General Fields
    SingularConfig* singularConfig = [[SingularConfig alloc] initWithApiKey:apikey andSecret:secret];

    // Singular Links fields
    singularConfig.launchOptions = launchOptions;
    singularConfig.supportedDomains = [singularConfigDict objectForKey:@"supportedDomains"];
    singularConfig.espDomains = [singularConfigDict objectForKey:@"espDomains"];
    singularConfig.shortLinkResolveTimeOut = [[singularConfigDict objectForKey:@"shortLinkResolveTimeout"] longValue];
    singularConfig.singularLinksHandler = ^(SingularLinkParams * params){
        [SingularBridge handleSingularLink:params];
    };

    // Global Properties fields
    NSDictionary* globalProperties = [singularConfigDict objectForKey:@"globalProperties"];
    if (globalProperties && [globalProperties count] > 0){
         for (NSDictionary* property in [globalProperties allValues]) {
             [singularConfig setGlobalProperty:[property objectForKey:@"Key"]
                                     withValue:[property objectForKey:@"Value"]
                              overrideExisting:[[property objectForKey:@"OverrideExisting"] boolValue]];
        }
    }

    // SKAN
    singularConfig.clipboardAttribution = [[singularConfigDict objectForKey:@"clipboardAttribution"] boolValue];
    singularConfig.skAdNetworkEnabled = [[singularConfigDict objectForKey:@"skAdNetworkEnabled"] boolValue];
    singularConfig.manualSkanConversionManagement = [[singularConfigDict objectForKey:@"manualSkanConversionManagement"] boolValue];
    singularConfig.conversionValueUpdatedCallback = ^(NSInteger conversionValue) {
        [SingularBridge handleConversionValueUpdated:conversionValue];
    };
    singularConfig.conversionValuesUpdatedCallback = ^(NSNumber *fineValue, NSNumber *coarseValue, BOOL lockWindow) {
        [SingularBridge handleConversionValuesUpdated:fineValue andCoarseValue:coarseValue andLockWindow:lockWindow];
    };
    
    singularConfig.waitForTrackingAuthorizationWithTimeoutInterval =
        [[singularConfigDict objectForKey:@"waitForTrackingAuthorizationWithTimeoutInterval"] intValue];

    singularConfig.deviceAttributionCallback = ^(NSDictionary *deviceAttributionData) {
        [SingularBridge handleDeviceAttributionData:deviceAttributionData];
    };
    
    NSString* customUserId = [singularConfigDict objectForKey:@"customUserId"];

    if (customUserId) {
        [Singular setCustomUserId:customUserId];
    }

    NSNumber* limitDataSharing = [singularConfigDict objectForKey:@"limitDataSharing"];

    if (![limitDataSharing isEqual:[NSNull null]]) {
        [Singular limitDataSharing:[limitDataSharing boolValue]];
    }

    NSNumber* sessionTimeout = [singularConfigDict objectForKey:@"sessionTimeout"];

    if (sessionTimeout >= 0) {
        [Singular setSessionTimeout:[sessionTimeout intValue]];
    }

    NSString *customSdid = [singularConfigDict objectForKey:@"customSdid"];
    if (![SingularBridge isValidNonEmptyString:customSdid]) {
        customSdid = nil;
    }
    
    singularConfig.customSdid = customSdid;

    singularConfig.sdidReceivedHandler = ^(NSString *result) {
        [eventEmitter sendEventWithName:SDID_RECEIVED_CALLBACK_CONST body:result];
    };

    singularConfig.didSetSdidHandler = ^(NSString *result) {
        [eventEmitter sendEventWithName:SDID_SET_CALLBACK_CONST body:result];
    };

    eventEmitter = self;

    [Singular start:singularConfig];
}

RCT_EXPORT_METHOD(createReferrerShortLink:(NSString *)baseLink
                  referrerName:(NSString *)referrerName
                  referrerId:(NSString *)referrerId
                  passthroughParams:(NSString *)args){
    [Singular createReferrerShortLink:baseLink
                         referrerName:referrerName
                           referrerId:referrerId
                    passthroughParams:[SingularBridge jsonToDictionary:args]
                    completionHandler:^(NSString *data, NSError *error) {
                            [eventEmitter sendEventWithName:SHORT_LINK_HANDLER_CONST body:@{
                                @"data": data? data: @"",
                                @"error": error ? error.description: @""
                            }];
    }];
}

RCT_EXPORT_METHOD(setCustomUserId:(NSString*)customUserId){
    [Singular setCustomUserId:customUserId];
}

RCT_EXPORT_METHOD(unsetCustomUserId){
    [Singular unsetCustomUserId];
}

RCT_EXPORT_METHOD(setDeviceCustomUserId:(NSString*)customUserId){
    [Singular setDeviceCustomUserId:customUserId];
}

RCT_EXPORT_METHOD(event:(NSString*)eventName){
    [Singular event:eventName];
}

RCT_EXPORT_METHOD(eventWithArgs:(NSString*)eventName args:(NSString*)args){
    [Singular event:eventName withArgs:[SingularBridge jsonToDictionary:args]];
}

RCT_EXPORT_METHOD(revenue:(NSString*)currency amount:(double)amount){
    [Singular revenue:currency amount:amount];
}

RCT_EXPORT_METHOD(revenueWithArgs:(NSString*)currency amount:(double)amount args:(NSString*)args){
    [Singular revenue:currency amount:amount withAttributes:[SingularBridge jsonToDictionary:args]];
}

RCT_EXPORT_METHOD(customRevenue:(NSString*)eventName currency:(NSString*)currency amount:(double)amount){
    [Singular customRevenue:eventName currency:currency amount:amount];
}

RCT_EXPORT_METHOD(customRevenueWithArgs:(NSString*)eventName currency:(NSString*)currency amount:(double)amount args:(NSString*)args){
    [Singular customRevenue:eventName currency:currency amount:amount withAttributes:[SingularBridge jsonToDictionary:args]];
}

RCT_EXPORT_METHOD(setUninstallToken:(NSString*)token){
    NSData *tokenData = [SingularBridge convertHexStringToDataBytes:token];
    if (tokenData) {
        [Singular registerDeviceTokenForUninstall:tokenData];
    }
}

RCT_EXPORT_METHOD(trackingOptIn){
    [Singular trackingOptIn];
}

RCT_EXPORT_METHOD(trackingUnder13){
    [Singular trackingUnder13];
}

RCT_EXPORT_METHOD(stopAllTracking){
    [Singular stopAllTracking];
}

RCT_EXPORT_METHOD(resumeAllTracking){
    [Singular resumeAllTracking];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(isAllTrackingStopped){
    return [Singular isAllTrackingStopped] ? @YES : @NO;
}

RCT_EXPORT_METHOD(limitDataSharing:(BOOL)limitDataSharingValue){
    [Singular limitDataSharing:limitDataSharingValue];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(getLimitDataSharing){
    return [Singular getLimitDataSharing] ? @YES : @NO;
}

RCT_EXPORT_METHOD(setReactSDKVersion:(NSString*)wrapper version:(NSString*)version){
    [Singular setWrapperName:wrapper andVersion:version];
}

// export SKAN methods
RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(skanUpdateConversionValue:(NSInteger)conversionValue){
    return [Singular skanUpdateConversionValue:conversionValue] ? @YES : @NO;
}

RCT_EXPORT_METHOD(skanUpdateConversionValues:(NSInteger)conversionValue coarse:(NSInteger)coarse lock:(BOOL)lock){
    [Singular skanUpdateConversionValue:conversionValue coarse:coarse lock:lock];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(skanGetConversionValue){
    return [Singular skanGetConversionValue];
}

RCT_EXPORT_METHOD(skanRegisterAppForAdNetworkAttribution){
    [Singular skanRegisterAppForAdNetworkAttribution];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(setGlobalProperty:(NSString *)key value:(NSString *)value overrideExisting:(BOOL)overrideExisting) {
    return [Singular setGlobalProperty:key andValue:value overrideExisting:overrideExisting] ? @YES : @NO;
}

RCT_EXPORT_METHOD(unsetGlobalProperty:(NSString *) key) {
    [Singular unsetGlobalProperty:key];
}

RCT_EXPORT_METHOD(clearGlobalProperties) {
    [Singular clearGlobalProperties];
}

RCT_EXPORT_BLOCKING_SYNCHRONOUS_METHOD(getGlobalProperties) {
    return [Singular getGlobalProperties];
}

#pragma mark - Private methods

+(NSDictionary*)jsonToDictionary:(NSString*)json{
    if(!json){
        return nil;
    }

    NSError *jsonError = nil;
    NSData *objectData = [json dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *data = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];

    if(jsonError){
        return nil;
    }

    return data;
}

+(void)handleSingularLink:(SingularLinkParams*)params {
    // Raising the Singular Link handler in the react-native code
    [eventEmitter sendEventWithName:SINGULAR_LINK_HANDLER_CONST body:@{
        @"deeplink": [params getDeepLink] ? [params getDeepLink] : @"",
        @"passthrough": [params getPassthrough] ? [params getPassthrough] : @"",
        @"isDeferred": [params isDeferred] ? @YES : @NO,
        @"urlParameters": [params getUrlParameters] ? [params getUrlParameters] : @{ }
    }];

}

+(void)handleConversionValueUpdated:(NSInteger)conversionValue {
    // Raising the Conversion Value handler in the react-native code
    [eventEmitter sendEventWithName:CONVERSION_VALUE_UPDATED_HANDLER_CONST body:@(conversionValue)];
}

+(void)handleDeviceAttributionData:(NSDictionary *)attributionData {
    // Raising the Device attribution handler in the react-native code
    [eventEmitter sendEventWithName:DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST body:attributionData];
}

+(void)handleConversionValuesUpdated:(NSNumber *)fineValue andCoarseValue:(NSNumber *)coarseValue andLockWindow:(BOOL)lockWindow {
    NSInteger fine = -1;
    NSInteger coarse = -1;
    
    if (fineValue != nil) {
        fine = [fineValue intValue];
    }
    if (coarseValue != nil) {
        coarse = [coarseValue intValue];
    }

    [eventEmitter sendEventWithName:CONVERSION_VALUES_UPDATED_HANDLER_CONST body:@{
        @"conversionValue": @(fine),
        @"coarse": @(coarse),
        @"lock": @(lockWindow)
    }];
}

+ (NSData *)convertHexStringToDataBytes:(NSString *)hexString {
    if([hexString length] % 2 != 0) {
        return nil;
    }

    const char *chars = [hexString UTF8String];
    int index = 0, length = (int)[hexString length];

    NSMutableData *data = [NSMutableData dataWithCapacity:length / 2];
    char byteChars[3] = {'\0','\0','\0'};
    unsigned long wholeByte;

    while (index < length) {
        byteChars[0] = chars[index++];
        byteChars[1] = chars[index++];
        wholeByte = strtoul(byteChars, NULL, 16);
        [data appendBytes:&wholeByte length:1];
    }
    
    return data;
}

+ (BOOL)isValidNonEmptyString:(NSString *)nullableJavascriptString {
    return nullableJavascriptString
    && ![customSdid isEqual:[NSNull null]]
    && [nullableJavascriptString isMemberOfClass:NSString.class]
    && nullableJavascriptString.length > 0
    && ![nullableJavascriptString.lowercaseString isEqualToString:@"null"]
    && ![nullableJavascriptString.lowercaseString isEqualToString:@"undefined"]
    && ![nullableJavascriptString.lowercaseString isEqualToString:@"false"]
    && ![nullableJavascriptString isEqualToString:@"NaN"];
}

@end
