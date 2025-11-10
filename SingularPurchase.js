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

    toSpecObject() {
        return {
            revenue: this._values.r,
            currency: this._values.pcc,
        };
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

    toSpecObject() {
        return {
            ...super.toSpecObject(),
            productId: this._values.pk,
            transactionId: this._values.pti,
            receipt: this._values.ptr
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

    toSpecObject() {
        return {
            ...super.toSpecObject(),
            receipt: this._values.receipt,
            receipt_signature: this._values.receipt_signature
        };
    }
}
