require 'json'
package = JSON.parse(File.read("package.json"))

Pod::Spec.new do |spec|
  spec.name             = "Singular-React-Native"
  spec.version          = package["version"]
  spec.summary          = "Singular React Native bridge for iOS"
  spec.license          = "MIT"
  spec.homepage         = "https://www.singular.net/"
  spec.author           = "Singular Labs"
  spec.source           = { :git => "https://github.com/singular-labs/react-native-sdk.git", :tag => spec.version.to_s }
  # Source files based on architecture
  if ENV['RCT_NEW_ARCH_ENABLED'] == '1' then
    spec.source_files = "ios/SingularBridge.h", "ios/SingularBridgeNewArch.mm", "ios/SingularHelper.h", "ios/SingularHelper.m"
  else
    spec.source_files = "ios/SingularBridge.h", "ios/SingularBridgeOldArch.m", "ios/SingularHelper.h", "ios/SingularHelper.m"
  end
  spec.platform         = :ios, "12.0"
  spec.dependency 'Singular-SDK', '12.9.0'
  spec.static_framework = true

  spec.dependency 'React'

  # New Architecture support
  if ENV['RCT_NEW_ARCH_ENABLED'] == '1' then
    spec.compiler_flags = "-DRCT_NEW_ARCH_ENABLED=1"
    spec.pod_target_xcconfig = {
      'HEADER_SEARCH_PATHS' => '"$(PODS_ROOT)/boost" "${PODS_TARGET_SRCROOT}/../../ios/build/generated/ios"',
      "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
      "CLANG_CXX_LIBRARY" => "libc++",
      "OTHER_CPLUSPLUSFLAGS" => "-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -DFOLLY_CFG_NO_COROUTINES=1"
    }
    spec.dependency "React-Codegen"
    spec.dependency "RCT-Folly"
    spec.dependency "RCTRequired"
    spec.dependency "RCTTypeSafety"
    spec.dependency "ReactCommon/turbomodule/core"
  else
    spec.compiler_flags = "-DRCT_NEW_ARCH_ENABLED=0"
  end
end

