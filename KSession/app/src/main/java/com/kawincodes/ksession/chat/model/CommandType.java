package com.kawincodes.ksession.chat.model;

/** Everything the chat assistant understands. Argument keys are listed per command. */
public enum CommandType {
    HELP,
    GREETING,
    /** Overview of engine, account, tunnels. */
    STATUS,
    /** List saved tunnels. */
    LIST,
    /** Start a quick tunnel. args: target (origin URL or port; default pocket server / 8080), name? */
    QUICK,
    /** args: name ("all" allowed) */
    START,
    /** args: name ("all" allowed) */
    STOP,
    /** args: name */
    RESTART,
    /** Create a named tunnel via the API. args: name, hostname, service */
    CREATE,
    /** Save a connector token. args: token, name? */
    ADD_TOKEN,
    /** args: name */
    DELETE,
    /** Open the live log console. args: name */
    LOGS,
    /** Show the public URL with QR. args: name */
    SHARE,
    /** Open a URL in the browser. args: url */
    OPEN,
    /** Browser login to Cloudflare. */
    LOGIN,
    /** Sign in with an API token. args: token */
    API_TOKEN,
    LOGOUT,
    /** Who am I / account details. */
    ACCOUNT,
    /** List zones. */
    ZONES,
    /** List tunnels on Cloudflare. */
    REMOTE_LIST,
    /** Import a remote tunnel as a TOKEN profile. args: name */
    IMPORT,
    /** Engine status. */
    ENGINE,
    /** Install/update cloudflared in-app. args: version? */
    ENGINE_INSTALL,
    /** Start the built-in Pocket Server. args: port? */
    SERVER_START,
    SERVER_STOP,
    /** Set protocol. args: protocol (auto|quic|http2) */
    PROTOCOL,
    /** Navigate: args: screen (radar|tunnels|cloud|chat) */
    NAVIGATE,
    CLEAR,
    ABOUT,
    UNKNOWN
}
