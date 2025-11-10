#import <Foundation/Foundation.h>

@class SingularConfig;

#define SINGULAR_LINK_HANDLER_CONST                 @"SingularLinkHandler"
#define CONVERSION_VALUE_UPDATED_HANDLER_CONST      @"ConversionValueUpdatedHandler"
#define DEVICE_ATTRIBUTION_CALLBACK_HANDLER_CONST   @"DeviceAttributionCallbackHandler"
#define CONVERSION_VALUES_UPDATED_HANDLER_CONST     @"ConversionValuesUpdatedHandler"
#define SHORT_LINK_HANDLER_CONST                    @"ShortLinkHandler"
#define SDID_RECEIVED_CALLBACK_CONST                @"SdidReceivedCallback"
#define SDID_SET_CALLBACK_CONST                     @"DidSetSdidCallback"

@interface SingularHelper : NSObject

+ (void)initWithConfig:(SingularConfig *)config;

+ (void)setSessionTimeout:(int)sessionTimeout;
+ (void)event:(NSString *)eventName;
+ (void)eventWithArgs:(NSString *)eventName args:(NSDictionary *)args;

+ (void)setCustomUserId:(NSString *)customUserId;
+ (void)unsetCustomUserId;
+ (void)setDeviceCustomUserId:(NSString *)customUserId;

+ (void)revenue:(NSString *)currency amount:(double)amount;
+ (void)revenueWithArgs:(NSString *)currency amount:(double)amount args:(NSDictionary *)args;
+ (void)customRevenue:(NSString *)eventName currency:(NSString *)currency amount:(double)amount;
+ (void)customRevenueWithArgs:(NSString *)eventName currency:(NSString *)currency amount:(double)amount args:(NSDictionary *)args;

+ (void)inAppPurchase:(NSString *)eventName purchase:(NSDictionary *)purchase;
+ (void)inAppPurchaseWithArgs:(NSString *)eventName purchase:(NSDictionary *)purchase args:(NSDictionary *)args;

+ (void)setUninstallToken:(NSString *)token;

+ (void)trackingOptIn;
+ (void)trackingUnder13;
+ (void)stopAllTracking;
+ (void)resumeAllTracking;
+ (BOOL)isAllTrackingStopped;

+ (void)limitDataSharing:(BOOL)shouldLimit;
+ (BOOL)getLimitDataSharing;

+ (BOOL)setGlobalProperty:(NSString *)key value:(NSString *)value overrideExisting:(BOOL)override;
+ (void)unsetGlobalProperty:(NSString *)key;
+ (void)clearGlobalProperties;
+ (NSDictionary *)getGlobalProperties;

+ (BOOL)skanUpdateConversionValue:(NSInteger)conversionValue;
+ (void)skanUpdateConversionValues:(NSInteger)conversionValue coarse:(NSInteger)coarse lock:(BOOL)lock;
+ (NSNumber *)skanGetConversionValue;
+ (void)skanRegisterAppForAdNetworkAttribution;

+ (void)handlePushNotification:(NSDictionary *)pushNotificationPayload;

+ (void)setLimitAdvertisingIdentifiers:(BOOL)enabled;

+ (void)createReferrerShortLink:(NSString *)baseLink
                   referrerName:(NSString *)referrerName
                     referrerId:(NSString *)referrerId
              passthroughParams:(NSDictionary *)passthroughParams
              completionHandler:(void(^)(NSString *result, NSString *error))completionHandler;

+ (NSString *)dictionaryToJSONString:(NSDictionary *)dictionary;
+ (NSDictionary *)jsonStringToDictionary:(NSString *)jsonString;
+ (void)setReactSDKVersion:(NSString*)wrapper version:(NSString*)version;

+ (NSArray<NSString *> *)supportedEvents;
@end

