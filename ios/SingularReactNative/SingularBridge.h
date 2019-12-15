#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#elif __has_include(“RCTBridgeModule.h”)
#import “RCTBridgeModule.h”
#else
#import “React/RCTBridgeModule.h” // Required when used as a Pod in a Swift project
#endif

@interface SingularBridge : RCTEventEmitter <RCTBridgeModule>{
}

+(void)startSessionWithLaunchOptions:(NSDictionary*)options;
+(void)startSessionWithUserActivity:(NSUserActivity*)userActivity;

@end
