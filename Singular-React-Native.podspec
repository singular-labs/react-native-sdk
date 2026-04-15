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
  spec.dependency 'Singular-SDK', '12.10.1'
  spec.static_framework = true

  # RN 0.71+ dependency setup.this handles both architectures, fallback for older versions
  if respond_to?(:install_modules_dependencies, true)
    install_modules_dependencies(spec)
  else
    spec.dependency "React"
  end
end

