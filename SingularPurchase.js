export class SingularPurchase {
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

export class SingularIOSPurchase extends SingularPurchase {
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

export class SingularAndroidPurchase extends SingularPurchase {
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
