package com.kawincodes.ksession.core.security;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Small secret store: values are encrypted with an AES-256-GCM key held in the Android Keystore
 * (alias "ksession_master") and saved in private SharedPreferences "ksession_secure".
 * Thread-safe. Never logs values.
 */
public final class CredentialStore {
    public static final String KEY_API_TOKEN = "cf.api_token";
    public static final String KEY_ORIGIN_CERT = "cf.origin_cert";
    public static final String KEY_ACCOUNT_ID = "cf.account_id";

    public CredentialStore(Context context) {
        // TODO cloud
    }

    public void put(String key, String value) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    @Nullable
    public String get(String key) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public boolean contains(String key) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public void remove(String key) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public void removeByPrefix(String prefix) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public void clearAll() {
        throw new UnsupportedOperationException("TODO cloud");
    }
}
