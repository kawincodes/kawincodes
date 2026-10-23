package com.kawincodes.ksession.chat.model;

import org.json.JSONException;
import org.json.JSONObject;

/** Rich attachment rendered under a bot message. */
public final class ChatCard {
    public enum Kind {
        /** Live tunnel card bound to {@link #profileId}; the UI renders current status from TunnelController. */
        TUNNEL,
        /** A public URL with copy/open/QR affordances. */
        LINK,
        /** Progress of a long operation (engine download, login wait). */
        PROGRESS,
        /** Key/value summary (account, engine, server status). {@link #subtitle} holds "key: value" lines. */
        INFO
    }

    public final Kind kind;
    public final String title;
    public final String subtitle;
    public final String url;
    public final String profileId;
    /** 0-100, or -1 for indeterminate. Only for PROGRESS. */
    public final int progress;
    /** Lucide icon key, may be null. */
    public final String icon;

    public ChatCard(Kind kind, String title, String subtitle, String url, String profileId, int progress, String icon) {
        this.kind = kind;
        this.title = title;
        this.subtitle = subtitle;
        this.url = url;
        this.profileId = profileId;
        this.progress = progress;
        this.icon = icon;
    }

    public static ChatCard tunnel(String profileId, String title) {
        return new ChatCard(Kind.TUNNEL, title, null, null, profileId, 0, "radar");
    }

    public static ChatCard link(String title, String url) {
        return new ChatCard(Kind.LINK, title, null, url, null, 0, "globe");
    }

    public static ChatCard progress(String title, String subtitle, int progress) {
        return new ChatCard(Kind.PROGRESS, title, subtitle, null, null, progress, "download");
    }

    public static ChatCard info(String title, String lines, String icon) {
        return new ChatCard(Kind.INFO, title, lines, null, null, 0, icon);
    }

    public JSONObject toJson() {
        try {
            JSONObject o = new JSONObject();
            o.put("kind", kind.name());
            o.putOpt("title", title);
            o.putOpt("subtitle", subtitle);
            o.putOpt("url", url);
            o.putOpt("profileId", profileId);
            o.put("progress", progress);
            o.putOpt("icon", icon);
            return o;
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ChatCard fromJson(JSONObject o) {
        Kind k;
        try {
            k = Kind.valueOf(o.optString("kind", Kind.INFO.name()));
        } catch (IllegalArgumentException e) {
            k = Kind.INFO;
        }
        return new ChatCard(k, o.optString("title", null), o.optString("subtitle", null),
                o.optString("url", null), o.optString("profileId", null), o.optInt("progress", 0),
                o.optString("icon", null));
    }
}
