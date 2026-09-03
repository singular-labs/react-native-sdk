export class SingularUserDetails {
    email;
    phoneNumber;
    emailSTD;
    emailNoDots;
    phoneE164;
    phoneDigits;

    /** Cleartext email — the SDK normalizes and hashes it. */
    setEmail(email) {
        return this._put('email', email);
    }

    /** Cleartext phone number — the SDK normalizes and hashes it. */
    setPhoneNumber(phoneNumber) {
        return this._put('phoneNumber', phoneNumber);
    }

    /** Pre-hashed email. Sent as-is. */
    setEmailSTD(hashedEmail) {
        return this._put('emailSTD', hashedEmail);
    }

    /** Pre-hashed email. Sent as-is. */
    setEmailNoDots(hashedEmail) {
        return this._put('emailNoDots', hashedEmail);
    }

    /** Pre-hashed phone. Sent as-is. */
    setPhoneE164(hashedPhone) {
        return this._put('phoneE164', hashedPhone);
    }

    /** Pre-hashed phone. Sent as-is. */
    setPhoneDigits(hashedPhone) {
        return this._put('phoneDigits', hashedPhone);
    }

    _put(key, value) {
        const invalid = typeof value !== 'string'
            || value.length === 0
            || value.toLowerCase() === 'null'
            || value.toLowerCase() === 'undefined';

        if (invalid) {
            delete this[key];
        } else {
            this[key] = value;
        }

        return this;
    }
}
