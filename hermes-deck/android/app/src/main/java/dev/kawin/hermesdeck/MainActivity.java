package dev.kawin.hermesdeck;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.activity.OnBackPressedCallback;

import com.getcapacitor.BridgeActivity;

import dev.kawin.hermesdeck.plugins.HermesHttpPlugin;
import dev.kawin.hermesdeck.plugins.SshPlugin;
import dev.kawin.hermesdeck.plugins.TermuxPlugin;
import dev.kawin.hermesdeck.plugins.VaultPlugin;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(SshPlugin.class);
        registerPlugin(HermesHttpPlugin.class);
        registerPlugin(TermuxPlugin.class);
        registerPlugin(VaultPlugin.class);
        super.onCreate(savedInstanceState);

        // Screens are hash routes, so the system back gesture walks the WebView history first.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                WebView webView = getBridge() == null ? null : getBridge().getWebView();
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
