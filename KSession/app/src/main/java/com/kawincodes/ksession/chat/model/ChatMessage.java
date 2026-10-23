package com.kawincodes.ksession.chat.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One chat entry. Immutable; to update a message (e.g. progress) the engine replaces it with a
 * new instance carrying the same {@link #id}.
 */
public final class ChatMessage {
    public enum Role { USER, BOT, SYSTEM }

    public final long id;
    public final Role role;
    /** Text with light markup: **bold**, `code`, and bare https:// links. */
    public final String text;
    public final long timestamp;
    /** Optional rich card, may be null. */
    public final ChatCard card;
    public final List<ChatAction> actions;

    public ChatMessage(long id, Role role, String text, long timestamp, ChatCard card, List<ChatAction> actions) {
        this.id = id;
        this.role = role;
        this.text = text == null ? "" : text;
        this.timestamp = timestamp;
        this.card = card;
        this.actions = Collections.unmodifiableList(actions == null ? new ArrayList<>() : new ArrayList<>(actions));
    }

    public ChatMessage withCard(ChatCard newCard) {
        return new ChatMessage(id, role, text, timestamp, newCard, actions);
    }

    public ChatMessage withText(String newText) {
        return new ChatMessage(id, role, newText, timestamp, card, actions);
    }

    public ChatMessage withActions(List<ChatAction> newActions) {
        return new ChatMessage(id, role, text, timestamp, card, newActions);
    }

    public JSONObject toJson() {
        try {
            JSONObject o = new JSONObject();
            o.put("id", id);
            o.put("role", role.name());
            o.put("text", text);
            o.put("ts", timestamp);
            if (card != null) o.put("card", card.toJson());
            if (!actions.isEmpty()) {
                JSONArray a = new JSONArray();
                for (ChatAction action : actions) a.put(action.toJson());
                o.put("actions", a);
            }
            return o;
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ChatMessage fromJson(JSONObject o) {
        Role r;
        try {
            r = Role.valueOf(o.optString("role", Role.BOT.name()));
        } catch (IllegalArgumentException e) {
            r = Role.BOT;
        }
        JSONObject c = o.optJSONObject("card");
        List<ChatAction> actions = new ArrayList<>();
        JSONArray a = o.optJSONArray("actions");
        if (a != null) {
            for (int i = 0; i < a.length(); i++) {
                JSONObject ao = a.optJSONObject(i);
                if (ao != null) actions.add(ChatAction.fromJson(ao));
            }
        }
        return new ChatMessage(o.optLong("id"), r, o.optString("text", ""), o.optLong("ts"),
                c == null ? null : ChatCard.fromJson(c), actions);
    }
}
