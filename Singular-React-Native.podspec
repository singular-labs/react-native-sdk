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
  spec.source_files      = "ios/*.{h,m}"
  spec.platform         = :ios, "8.0"
  spec.static_framework = true
  spec.dependency 'Singular-SDK', '12.0.3'

  spec.dependency 'React'
end
