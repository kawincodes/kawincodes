package com.kawincodes.ksession.chat.model;

import org.json.JSONException;
import org.json.JSONObject;

/** A tappable chip/button under a bot message. Tapping it runs {@link #command} as if typed. */
public final class ChatAction {
    public enum Style { PRIMARY, SECONDARY, DANGER }

    public final String label;
    /** Command text, e.g. "/stop demo" or "/open https://x.trycloudflare.com". */
    public final String command;
    /** Lucide icon key (see {@code ui.common.Icons}), e.g. "play"; may be null. */
    public final String icon;
    public final Style style;

    public ChatAction(String label, String command, String icon, Style style) {
        this.label = label;
        this.command = command;
        this.icon = icon;
        this.style = style == null ? Style.SECONDARY : style;
    }

    public static ChatAction of(String label, String command, String icon) {
        return new ChatAction(label, command, icon, Style.SECONDARY);
    }

    public JSONObject toJson() {
        try {
            JSONObject o = new JSONObject();
            o.put("label", label);
            o.put("command", command);
            o.putOpt("icon", icon);
            o.put("style", style.name());
            return o;
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ChatAction fromJson(JSONObject o) {
        Style s;
        try {
            s = Style.valueOf(o.optString("style", Style.SECONDARY.name()));
        } catch (IllegalArgumentException e) {
            s = Style.SECONDARY;
        }
        return new ChatAction(o.optString("label"), o.optString("command"), o.optString("icon", null), s);
    }
}
