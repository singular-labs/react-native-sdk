#import "SingularHelper.h"
#import <Singular/Singular.h>
#import <Singular/SingularConfig.h>

@implementation SingularHelper

#pragma mark - Initialization

+ (void)initWithConfig:(SingularConfig *)config {
    [Singular start:config];
}

+ (void)setSessionTimeout:(int)sessionTimeout {
    [Singular setSessionTimeout:sessionTimeout];
}


// The JS withLogLevel() API takes android.util.Log style levels
// Verbose=2, Debug=3, Info=4, Warn=5, Error=6, Assert=7

// iOS native SDK log levels (SingularLogLevel)
// None=0, Error=1, Warning=2, Info=3, Debug=4, Verbose=5

+ (SingularLogLevel)mapReactNativeLogLevelToiOS:(NSInteger)reactNativeLogLevel {
    switch (reactNativeLogLevel) {
        case 2: return SingularLogLevelVerbose;
        case 3: return SingularLogLevelDebug;
        case 4: return SingularLogLevelInfo;
        case 5: return SingularLogLevelWarning;
        case 6: return SingularLogLevelError;
        case 7: return SingularLogLevelNone;
        default: return SingularLogLevelError;
    }
}

+ (void)applyLoggingConfig:(SingularConfig *)config
             enableLogging:(BOOL)enableLogging
                  logLevel:(NSInteger)logLevel {
    config.enableLogging = enableLogging;
    config.logLevel = [self mapReactNativeLogLevelToiOS:logLevel];
}

#pragma mark - Event Tracking

+ (void)event:(NSString *)eventName {
    [Singular event:eventName];
}

+ (void)eventWithArgs:(NSString *)eventName args:(NSDictionary *)args {
    [Singular event:eventName withArgs:args];
}

#pragma mark - User Management

+ (void)setCustomUserId:(NSString *)customUserId {
    [Singular setCustomUserId:customUserId];
}

+ (void)unsetCustomUserId {
    [Singular unsetCustomUserId];
}

+ (void)setDeviceCustomUserId:(NSString *)customUserId {
    [Singular setDeviceCustomUserId:customUserId];
}

#pragma mark - Revenue Tracking

+ (void)revenue:(NSString *)currency amount:(double)amount {
    [Singular revenue:currency amount:amount];
}

+ (void)revenueWithArgs:(NSString *)currency amount:(double)amount args:(NSDictionary *)args {
    [Singular revenue:currency amount:amount withAttributes:args];
}

+ (void)customRevenue:(NSString *)eventName currency:(NSString *)currency amount:(double)amount {
    [Singular customRevenue:eventName currency:currency amount:amount];
}

+ (void)customRevenueWithArgs:(NSString *)eventName currency:(NSString *)currency amount:(double)amount args:(NSDictionary *)args {
    [Singular customRevenue:eventName currency:currency amount:amount withAttributes:args];
}

#pragma mark - In-App Purchases

+ (void)inAppPurchase:(NSString *)eventName purchase:(NSDictionary *)purchase {
    [Singular event:eventName withArgs:purchase];
}

+ (void)inAppPurchaseWithArgs:(NSString *)eventName purchase:(NSDictionary *)purchase args:(NSDictionary *)args {
    NSMutableDictionary *merged = [args mutableCopy];
    [merged addEntriesFromDictionary:purchase];
    [Singular event:eventName withArgs:merged];
}

#pragma mark - Push Notifications

+ (void)setUninstallToken:(NSString *)token {
    NSData *tokenData = [self convertHexStringToDataBytes:token];
    [Singular registerDeviceTokenForUninstall:tokenData];
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

#pragma mark - Tracking Control

+ (void)trackingOptIn {
    [Singular trackingOptIn];
}

+ (void)trackingUnder13 {
    [Singular trackingUnder13];
}

+ (void)stopAllTracking {
    [Singular stopAllTracking];
}

+ (void)resumeAllTracking {
    [Singular resumeAllTracking];
}

+ (BOOL)isAllTrackingStopped {
    return [Singular isAllTrackingStopped];
}

#pragma mark - Data Sharing

+ (void)limitDataSharing:(BOOL)shouldLimit {
    [Singular limitDataSharing:shouldLimit];
}

+ (BOOL)getLimitDataSharing {
    return [Singular getLimitDataSharing];
}

#pragma mark - Global Properties

#pragma mark - User Details

+ (BOOL)isNonEmptyString:(id)value {
    if (![value isKindOfClass:[NSString class]] || [(NSString *)value length] == 0) {
        return NO;
    }

    NSString *lowercased = [(NSString *)value lowercaseString];
    return ![lowercased isEqualToString:@"null"] && ![lowercased isEqualToString:@"undefined"];
}

+ (SingularUserDetails *)userDetailsFromDictionary:(NSDictionary *)values {
    if (![values isKindOfClass:[NSDictionary class]]) {
        return nil;
    }

    SingularUserDetails *userDetails = [[SingularUserDetails alloc] init];

    if ([self isNonEmptyString:values[@"email"]]) {
        [userDetails setEmail:values[@"email"]];
    }
    if ([self isNonEmptyString:values[@"phoneNumber"]]) {
        [userDetails setPhoneNumber:values[@"phoneNumber"]];
    }
    if ([self isNonEmptyString:values[@"emailSTD"]]) {
        [userDetails setEmailSTD:values[@"emailSTD"]];
    }
    if ([self isNonEmptyString:values[@"emailNoDots"]]) {
        [userDetails setEmailNoDots:values[@"emailNoDots"]];
    }
    if ([self isNonEmptyString:values[@"phoneE164"]]) {
        [userDetails setPhoneE164:values[@"phoneE164"]];
    }
    if ([self isNonEmptyString:values[@"phoneDigits"]]) {
        [userDetails setPhoneDigits:values[@"phoneDigits"]];
    }

    return userDetails;
}

+ (void)setUserDetailsFromDictionary:(NSDictionary *)values {
    SingularUserDetails *userDetails = [self userDetailsFromDictionary:values];
    if (userDetails) {
        [Singular setUserDetails:userDetails];
    }
}

+ (void)clearUserDetails {
    [Singular clearUserDetails];
}

+ (void)applyUserDetails:(NSDictionary *)values toConfig:(SingularConfig *)config {
    if (!config) {
        return;
    }

    SingularUserDetails *userDetails = [self userDetailsFromDictionary:values];
    if (userDetails) {
        config.userDetails = userDetails;
    }
}

+ (BOOL)setGlobalProperty:(NSString *)key value:(NSString *)value overrideExisting:(BOOL)override {
    return [Singular setGlobalProperty:key andValue:value overrideExisting:override];
}

+ (void)unsetGlobalProperty:(NSString *)key {
    [Singular unsetGlobalProperty:key];
}

+ (void)clearGlobalProperties {
    [Singular clearGlobalProperties];
}

+ (NSDictionary *)getGlobalProperties {
    NSDictionary *properties = [Singular getGlobalProperties];
    return properties ?: @{};
}

#pragma mark - SKAN Methods

+ (BOOL)skanUpdateConversionValue:(NSInteger)conversionValue {
    BOOL success = [Singular skanUpdateConversionValue:conversionValue];
    return success;
}

+ (void)skanUpdateConversionValues:(NSInteger)conversionValue coarse:(NSInteger)coarse lock:(BOOL)lock {
    [Singular skanUpdateConversionValue:conversionValue coarse:coarse lock:lock];
}

+ (NSNumber *)skanGetConversionValue {
    NSNumber *value = [Singular skanGetConversionValue];
    return value;
}

+ (void)skanRegisterAppForAdNetworkAttribution {
    [Singular skanRegisterAppForAdNetworkAttribution];
}

#pragma mark - Push Notifications

+ (void)handlePushNotification:(NSDictionary *)pushNotificationPayload {
    [Singular handlePushNotification:pushNotificationPayload];
}

#pragma mark - Advertising Identifiers

+ (void)setLimitAdvertisingIdentifiers:(BOOL)enabled {
    [Singular setLimitAdvertisingIdentifiers:enabled];
}

#pragma mark - Short Links

+ (void)createReferrerShortLink:(NSString *)baseLink 
                   referrerName:(NSString *)referrerName 
                     referrerId:(NSString *)referrerId 
               passthroughParams:(NSDictionary *)passthroughParams
               completionHandler:(void(^)(NSString *result, NSString *error))completionHandler {
    [Singular createReferrerShortLink:baseLink
                         referrerName:referrerName
                           referrerId:referrerId
                     passthroughParams:passthroughParams
                     completionHandler:^(NSString *result, NSError *error) {
        if (error) {
            completionHandler(nil, error.localizedDescription);
        } else {
            completionHandler(result, nil);
        }
    }];
}

+ (void)setDeferredDeepLinkTimeout:(int)duration {
    [Singular setDeferredDeepLinkTimeout:duration];
}

#pragma mark - Helper Methods

+ (NSString *)dictionaryToJSONString:(NSDictionary *)dictionary {
    if (!dictionary) {
        return @"{}";
    }
    
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dictionary options:0 error:&error];
    
    if (error) {
        NSLog(@"Error converting dictionary to JSON: %@", error.localizedDescription);
        return @"{}";
    }
    
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

+ (NSDictionary *)jsonStringToDictionary:(NSString *)jsonString {
    if (!jsonString || [jsonString isEqualToString:@""]) {
        return @{};
    }
    
    NSError *error;
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *dictionary = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:&error];
    
    if (error) {
        NSLog(@"Error parsing JSON string: %@", error.localizedDescription);
        return @{};
    }
    
    return dictionary;
}

+ (void)setReactSDKVersion:(NSString*)wrapper version:(NSString*)version {
    [Singular setWrapperName:wrapper andVersion:version];
}

+ (NSArray<NSString *> *)supportedEvents {
    return @[SINGULAR_LINK_HANDLER_CONST,
             CONVERSION_VALUE_UPDATED_HANDLER_CONST,
             SHORT_LINK_HANDLER_CONST,
             CONVERSION_VALUES_UPDATED_HANDLER_CONST,
             DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST,
             SDID_RECEIVED_CALLBACK_CONST,
             SDID_SET_CALLBACK_CONST];
}

@end

