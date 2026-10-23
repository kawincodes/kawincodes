package com.kawincodes.ksession.core.cloud.model;

/** Current Cloudflare sign-in state. Immutable. */
public final class AccountState {
    public static final AccountState SIGNED_OUT =
            new AccountState(AuthMode.NONE, null, null, null, null, false, null);

    public final AuthMode mode;
    public final String accountId;
    public final String accountName;
    /** Browser login: the zone authorised during login. API token: null unless chosen. */
    public final String zoneId;
    public final String zoneName;
    /** True after the credentials were verified against the API. */
    public final boolean verified;
    /** Status or error text for the UI, or null. */
    public final String message;

    public AccountState(AuthMode mode, String accountId, String accountName, String zoneId,
                        String zoneName, boolean verified, String message) {
        this.mode = mode;
        this.accountId = accountId;
        this.accountName = accountName;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.verified = verified;
        this.message = message;
    }

    public boolean isSignedIn() {
        return mode != AuthMode.NONE;
    }

    public AccountState withMessage(String msg) {
        return new AccountState(mode, accountId, accountName, zoneId, zoneName, verified, msg);
    }
}
