export class SingularPurchase {
    _values;

    constructor(revenue, currency) {
        this._values.r = revenue;
        this._values.pcc = currency;
        this._values.is_revenue_event = true;
    }

    getPurchaseValues() {
        return this._values;
    }
}

export class SingularIOSPurchase extends SingularPurchase {
    constructor(revenue, currency, productId, transactionId, receipt) {
        super(revenue, currency);
        this._values.pk = productId;
        this._values.pti = transactionId;
        this._values.ptr = receipt;
    }
}

export class SingularAndroidPurchase extends SingularPurchase {
    constructor(revenue, currency, receipt, signature) {
        super(revenue, currency);
        this._values.receipt = receipt;
        this._values.receipt_signature = signature;
    }
}
