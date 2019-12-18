#import "SingularBridge.h"

#import <Singular/Singular.h>

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

@implementation SingularBridge
@synthesize bridge = _bridge;

static NSString* apikey;
static NSString* secret;
static NSDictionary* launchOptions;
static BOOL isSingularLinksEnabled = NO;
static RCTEventEmitter* eventEmitter;

// Saving the launchOptions for later when the SDK is initialize to handle Singular Links.
// The client will need to call this method is his AppDelegate in didFinishLaunchingWithOptions.
+(void)startSessionWithLaunchOptions:(NSDictionary*)options{
    launchOptions = options;
}

// Handling Singular Link when the app is opened from a Singular Link while it was in the background.
// The client will need to call this method is his AppDelegate in continueUserActivity.
+(void)startSessionWithUserActivity:(NSUserActivity*)userActivity{
    if(!isSingularLinksEnabled){
        return;
    }

    [Singular startSession:apikey
                   withKey:secret
           andUserActivity:userActivity
   withSingularLinkHandler:^(SingularLinkParams * params){
        [SingularBridge handleSingularLinks:params];
    }];
}

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"SingularLinkHandler"];
}

RCT_EXPORT_METHOD(init:(NSString*)apikey secret:(NSString*)secret customUserId:(NSString*)customUserId){
    if(customUserId){
        [Singular setCustomUserId:customUserId];
    }

    [Singular startSession:apikey withKey:secret];
}

RCT_EXPORT_METHOD(initWithSingularLinks:(NSString*)apikey
                  secret:(NSString*)secret
                  customUserId:(NSString*)customUserId){
    if(customUserId){
        [Singular setCustomUserId:customUserId];
    }

    isSingularLinksEnabled = YES;
    eventEmitter = self;

    [Singular startSession:apikey
                   withKey:secret
          andLaunchOptions:launchOptions
   withSingularLinkHandler:^(SingularLinkParams * params){
        [SingularBridge handleSingularLinks:params];
    }];
}

RCT_EXPORT_METHOD(setCustomUserId:(NSString*)customUserId){
    [Singular setCustomUserId:customUserId];
}

RCT_EXPORT_METHOD(unsetCustomUserId){
    [Singular unsetCustomUserId];
}

RCT_EXPORT_METHOD(event:(NSString*)eventName){
    [Singular event:eventName];
}

RCT_EXPORT_METHOD(eventWithArgs:(NSString*)eventName args:(NSString*)args){
    if(!args){
        [Singular event:eventName];
    }

    [Singular event:eventName withArgs:[SingularBridge jsonToDictionary:args]];
}

RCT_EXPORT_METHOD(revenue:(NSString*)currency amount:(double)amount){
    [Singular revenue:currency amount:amount];
}

RCT_EXPORT_METHOD(revenueWithArgs:(NSString*)currency amount:(double)amount args:(NSString*)args){
    if(!args){
        [Singular revenue:currency amount:amount];
    }

    [Singular revenue:currency amount:amount withAttributes:[SingularBridge jsonToDictionary:args]];
}

RCT_EXPORT_METHOD(customRevenue:(NSString*)eventName currency:(NSString*)currency amount:(double)amount){
    [Singular customRevenue:eventName currency:currency amount:amount];
}

RCT_EXPORT_METHOD(customRevenueWithArgs:(NSString*)eventName currency:(NSString*)currency amount:(double)amount args:(NSString*)args){
    if(!args){
        [Singular customRevenue:eventName currency:currency amount:amount];
    }

    [Singular customRevenue:eventName currency:currency amount:amount withAttributes:[SingularBridge jsonToDictionary:args]];
}

RCT_EXPORT_METHOD(setUninstallToken:(NSString*)token){
    [Singular registerDeviceTokenForUninstall:[token dataUsingEncoding:NSUTF8StringEncoding]];
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

RCT_REMAP_METHOD(isAllTrackingStopped, resolver: (RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject){
    resolve([Singular isAllTrackingStopped] ? @YES : @NO);
}

RCT_EXPORT_METHOD(setReactSDKVersion:(NSString*)wrapper version:(NSString*)version){
    [Singular setWrapperName:wrapper andVersion:version];
}

#pragma mark - Private methods

+(NSDictionary*)jsonToDictionary:(NSString*)json{
    NSError *jsonError;
    NSData *objectData = [json dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *data = [NSJSONSerialization JSONObjectWithData:objectData
                                                         options:NSJSONReadingMutableContainers
                                                           error:&jsonError];

    if(!jsonError){
        return nil;
    }

    return data;
}

+(void)handleSingularLinks:(SingularLinkParams*)params{

    // Raising the Singular Link handler in the react-native code
    [eventEmitter sendEventWithName:@"SingularLinkHandler" body:@{
        @"deeplink": [params getDeepLink],
        @"passthrough": [params getPassthrough],
        @"isDeferred": [params isDeferred] ? @YES : @NO
    }];
}

@end
