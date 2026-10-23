package com.kawincodes.ksession.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kawincodes.ksession.AppGraph;
import com.kawincodes.ksession.chat.model.ChatAction;
import com.kawincodes.ksession.chat.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * "Kay", the KSession assistant: parses chat input into commands, runs them against the app
 * services, and answers with messages, cards and action chips. Also posts proactive messages for
 * tunnel events (online, URL ready, errors). History persists in filesDir/chat.json (last 300).
 * Main-thread API.
 */
public final class ChatEngine {

    /** UI requests the engine cannot do itself (navigation, browser, dialogs). */
    public interface UiDelegate {
        /** Select a bottom-nav tab: R.id.nav_chat / nav_radar / nav_tunnels / nav_cloud. */
        void navigate(int navId);

        /** Open a URL (Custom Tab). */
        void openUrl(String url);

        /** Open the live log console for a profile. */
        void openLogs(String profileId);

        /** Show a QR code for a URL. */
        void showQr(String title, String url);

        /** Open the create-tunnel sheet (type name "QUICK"/"TOKEN"/"MANAGED" or null). */
        void openCreateTunnel(String presetType);
    }

    private final AppGraph graph;
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> typing = new MutableLiveData<>(false);

    public ChatEngine(AppGraph graph) {
        this.graph = graph;
    }

    public LiveData<List<ChatMessage>> messages() {
        return messages;
    }

    /** True while the assistant is "thinking" (long operations). */
    public LiveData<Boolean> typing() {
        return typing;
    }

    /** Attach the visible chat screen (or null when it goes away). */
    public void setUiDelegate(UiDelegate delegate) {
        throw new UnsupportedOperationException("TODO chat");
    }

    /** Handles text typed by the user. */
    public void submit(String text) {
        throw new UnsupportedOperationException("TODO chat");
    }

    /** Runs a tapped action chip. */
    public void runAction(ChatAction action) {
        throw new UnsupportedOperationException("TODO chat");
    }

    /** Suggestion chips for the input bar, based on current state. */
    public List<ChatAction> suggestions() {
        throw new UnsupportedOperationException("TODO chat");
    }

    public void clear() {
        throw new UnsupportedOperationException("TODO chat");
    }
}
