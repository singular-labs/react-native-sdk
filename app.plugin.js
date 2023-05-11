const { AndroidConfig, withProjectBuildGradle } = require('@expo/config-plugins');

const withSingularPermissions = (config) => {
  return AndroidConfig.Permissions.withPermissions(config, [
    'android.permission.INTERNET',
    'android.permission.ACCESS_NETWORK_STATE',
    'com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE',
    'com.android.vending.CHECK_LICENSE',
    'com.google.android.gms.permission.AD_ID',
  ]);
};

const withSingularProjectBuildGradle = (config) => {
  return withProjectBuildGradle(config, async (config) => {
    config.modResults.contents = config.modResults.contents.replace(
      /allprojects {(?:.|\n)*repositories {/m,
      str => `${str}\n        maven { url 'https://maven.singular.net/' }`
    )

    return config;
  });
};

module.exports = (config) => {
  config = withSingularPermissions(config);
  config = withSingularProjectBuildGradle(config);

  return config;
};