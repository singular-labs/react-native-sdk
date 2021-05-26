export class SingularAdData {

    constructor(adPlatform, currency, revenue) {
        this._values = {
            ad_platform: adPlatform,
            ad_currency: currency,
            ad_revenue: revenue,
            is_admon_revenue: true,
            is_revenue_event: true,
            ad_mediation_platform: adPlatform
        };
    }

    getAdDataValues() {
        return this._values;
    }

    setNetworkName(networkName) {
        this._values = {
            ...this._values,
            ad_mediation_platform: networkName
        };
    }

    setAdType(adType) {
        this._values = {
            ...this._values,
            ad_type: adType
        };
    }

    setGroupType(adGroupType) {
        this._values = {
            ...this._values,
            ad_group_type: adGroupType
        };
    }

    setImpressionId(impressionId) {
        this._values = {
            ...this._values,
            ad_impression_id: impressionId
        };
    }

    setAdPlacementName(adPlacementName) {
        this._values = {
            ...this._values,
            ad_placement_name: adPlacementName
        };
    }

    setAdUnitId(adUnitId) {
        this._values = {
            ...this._values,
            ad_unit_id: adUnitId
        };
    }

    setAdGroupId(adGroupId) {
        this._values = {
            ...this._values,
            ad_group_id: adGroupId
        };
    }

    setAdGroupName(adGroupName) {
        this._values = {
            ...this._values,
            ad_group_name: adGroupName
        };
    }

    setAdGroupPriority(adGroupPriority) {
        this._values = {
            ...this._values,
            ad_group_priority: adGroupPriority
        };
    }

    setPrecision(precision) {
        this._values = {
            ...this._values,
            ad_precision: precision
        };
    }

    setPlacementId(placementId) {
        this._values = {
            ...this._values,
            ad_placement_id: placementId
        };
    }

    hasRequiredParams() {
        if (!this._values.ad_currency || !this._values.ad_platform || !this._values.ad_revenue) {
            return false;
        }
        return true;
    }
}