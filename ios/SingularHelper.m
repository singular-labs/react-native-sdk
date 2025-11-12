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

