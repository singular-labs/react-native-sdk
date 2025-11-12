#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#else
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#endif

#if RCT_NEW_ARCH_ENABLED
#import <ReactCommon/RCTTurboModule.h>
#import <NativeSingular/NativeSingular.h>
#endif

#if RCT_NEW_ARCH_ENABLED
@interface SingularBridge : RCTEventEmitter <NativeSingularSpec>
#else
@interface SingularBridge : RCTEventEmitter <RCTBridgeModule>
#endif

+ (void)startSessionWithUserActivity:(NSUserActivity*)userActivity;
+ (void)startSessionWithLaunchOptions:(NSDictionary*)options;

@end
