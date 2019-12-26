export default class SingularPurchase {
    constructor(revenue, currency) {
        this._values = {
            r: revenue,
            pcc: currency,
            is_revenue_event: true
        };
    }

    getPurchaseValues() {
        return this._values;
    }
}

export default class SingularIOSPurchase extends SingularPurchase {
    constructor(revenue, currency, productId, transactionId, receipt) {
        super(revenue, currency);
        this._values = {
            ...this._values,
            pk: productId,
            pti: transactionId,
            ptr: receipt,
        };
    }
}

export default class SingularAndroidPurchase extends SingularPurchase {
    constructor(revenue, currency, receipt, signature) {
        super(revenue, currency);
        this._values = {
            ...this._values,
            receipt: receipt,
            receipt_signature: signature,
            ptr: receipt,
        };
    }
}
