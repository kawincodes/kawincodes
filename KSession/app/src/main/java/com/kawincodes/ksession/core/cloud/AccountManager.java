package com.kawincodes.ksession.core.cloud;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kawincodes.ksession.AppGraph;
import com.kawincodes.ksession.core.cloud.model.AccountState;
import com.kawincodes.ksession.core.cloud.model.CfAccount;
import com.kawincodes.ksession.core.cloud.model.CfTunnel;
import com.kawincodes.ksession.core.cloud.model.CfZone;
import com.kawincodes.ksession.util.Callback;

import java.util.List;

/**
 * Cloudflare sign-in (browser login or API token), restored from CredentialStore on creation.
 * Callbacks run on the main thread.
 */
public final class AccountManager {

    private final AppGraph graph;
    private final MutableLiveData<AccountState> state = new MutableLiveData<>(AccountState.SIGNED_OUT);

    public AccountManager(AppGraph graph) {
        this.graph = graph;
    }

    public LiveData<AccountState> state() {
        return state;
    }

    public AccountState current() {
        AccountState s = state.getValue();
        return s == null ? AccountState.SIGNED_OUT : s;
    }

    public boolean isSignedIn() {
        return current().isSignedIn();
    }

    /** Starts a browser login; open {@code session.authorizeUrl}, then call {@link #awaitBrowserLogin}. */
    public BrowserLogin.Session beginBrowserLogin() {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /**
     * Polls for the certificate (up to ~10 minutes), stores it, verifies it and signs in with
     * mode BROWSER_CERT. Errors with a message if cancelled or timed out.
     */
    public void awaitBrowserLogin(BrowserLogin.Session session, Callback<AccountState> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    public void cancelBrowserLogin() {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** True while {@link #awaitBrowserLogin} is polling. */
    public boolean isBrowserLoginPending() {
        return false;
    }

    /**
     * Verifies an API token, picks the account (the given one, or the first available) and signs
     * in with mode API_TOKEN.
     */
    public void signInWithApiToken(String token, @Nullable String accountId, Callback<AccountState> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Accounts visible to the current credentials. */
    public void listAccounts(Callback<List<CfAccount>> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Switches the active account (API_TOKEN mode). */
    public void selectAccount(CfAccount account, Callback<AccountState> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Zones usable for routing (browser login: just the authorised zone). */
    public void listZones(Callback<List<CfZone>> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Non-deleted tunnels in the active account. */
    public void listRemoteTunnels(Callback<List<CfTunnel>> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Re-verifies the stored credentials and refreshes account/zone names. */
    public void refresh(@Nullable Callback<AccountState> cb) {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** Forgets the API token / cert (tunnel secrets are kept). */
    public void signOut() {
        throw new UnsupportedOperationException("TODO cloud");
    }

    /** API client for the current credentials, or null when signed out. */
    @Nullable
    public CloudflareApi api() {
        throw new UnsupportedOperationException("TODO cloud");
    }
}
