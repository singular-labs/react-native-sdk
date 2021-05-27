const ADMON_IS_ADMON_REVENUE = 'is_admon_revenue';
const ADMON_AD_PLATFORM = 'ad_platform';
const ADMON_CURRENCY = 'ad_currency';
const ADMON_REVENUE = 'ad_revenue';
const ADMON_NETWORK_NAME = 'ad_mediation_platform';
const ADMON_AD_TYPE = 'ad_type';
const ADMON_AD_GROUP_TYPE = 'ad_group_type';
const ADMON_IMPRESSION_ID = 'ad_impression_id';
const ADMON_AD_PLACEMENT_NAME = 'ad_placement_name';
const ADMON_AD_UNIT_ID = 'ad_unit_id';
const ADMON_AD_UNIT_NAME = 'ad_unit_name';
const ADMON_AD_GROUP_ID = 'ad_group_id';
const ADMON_AD_GROUP_NAME = 'ad_group_name';
const ADMON_AD_GROUP_PRIORITY = 'ad_group_priority';
const ADMON_PRECISION = 'ad_precision';
const ADMON_PLACEMENT_ID = 'ad_placement_id';
const IS_REVENUE_EVENT_KEY = 'is_revenue_event';

const requiredParams = [ADMON_AD_PLATFORM, ADMON_CURRENCY, ADMON_REVENUE];

export class SingularAdData {

    constructor(adPlatform, currency, revenue) {
        this[ADMON_AD_PLATFORM] = adPlatform;
        this[ADMON_REVENUE] = revenue;
        this[ADMON_CURRENCY] = currency;
        this[ADMON_IS_ADMON_REVENUE] = true;
        this[IS_REVENUE_EVENT_KEY] = true;

        this[ADMON_NETWORK_NAME] = adPlatform;
    }

    withNetworkName(networkName) {
        this[ADMON_NETWORK_NAME] = networkName;
        return this;
    }

    withAdType(adType) {
        this[ADMON_AD_TYPE] = adType;
        return this;
    }

    withGroupType(adGroupType) {
        this[ADMON_AD_GROUP_TYPE] = adGroupType;
        return this;
    }

    withImpressionId(impressionId) {
        this[ADMON_IMPRESSION_ID] = impressionId;
        return this;
    }

    withAdPlacementName(adPlacementName) {
        this[ADMON_AD_PLACEMENT_NAME] = adPlacementName;
        return this;
    }

    withAdUnitId(adUnitId) {
        this[ADMON_AD_UNIT_ID] = adUnitId;
        return this;
    }

    withAdGroupId(adGroupId) {
        this[ADMON_AD_GROUP_ID] = adGroupId;
        return this;
    }

    withAdGroupName(adGroupName) {
        this[ADMON_AD_GROUP_NAME] = adGroupName;
        return this;
    }

    withAdGroupPriority(adGroupPriority) {
        this[ADMON_AD_GROUP_PRIORITY] = adGroupPriority;
        return this;
    }

    withPrecision(precision) {
        this[ADMON_PRECISION] = precision;
        return this;
    }

    withPlacementId(placementId) {
        this[ADMON_PLACEMENT_ID] = placementId;
        return this;
    }

    withAdUnitName(adUnitName) {
        this[ADMON_AD_UNIT_NAME] = adUnitName;
        return this;
    }

    hasRequiredParams() {
        for (const key of requiredParams) {
            if (!this[key]) {
                return false;
            }
        }
        return true;
    }
}