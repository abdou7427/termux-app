package com.termux.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TermuxActivity extends AppCompatActivity {

    private WebView dynAgentWebView;
    private WebView dynInternetWebView;
    private LinearLayout dynInternetLayout;
    private EditText dynUrlBar;
    private TextView dynTitleBar;
    private FrameLayout dynMainContainer;
    private SharedPreferences dynPrefs;
    private int dynActiveView = 0; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termux);
        injectAgenticOSDynamicUI();
    }

    public void injectAgenticOSDynamicUI() {
        try {
            ViewGroup rootView = findViewById(R.id.activity_termux_root_view);
            if (rootView == null) return;

            dynPrefs = getSharedPreferences("AgenticPrefs", Context.MODE_PRIVATE);

            LinearLayout mainLayout = new LinearLayout(this);
            mainLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setBackgroundColor(Color.parseColor("#121212"));

            RelativeLayout topBar = new RelativeLayout(this);
            topBar.setLayoutParams(new LinearLayout.LayoutParams(-1, (int) (56 * getResources().getDisplayMetrics().density)));
            topBar.setBackgroundColor(Color.parseColor("#1F1F1F"));
            topBar.setPadding((int) (8 * getResources().getDisplayMetrics().density), (int) (8 * getResources().getDisplayMetrics().density), (int) (8 * getResources().getDisplayMetrics().density), (int) (8 * getResources().getDisplayMetrics().density));

            dynTitleBar = new TextView(this);
            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(-2, -2);
            titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            dynTitleBar.setLayoutParams(titleParams);
            dynTitleBar.setText("🤖 AGENT CHAT");
            dynTitleBar.setTextColor(Color.parseColor("#00FF66"));
            dynTitleBar.setTextSize(18);
            dynTitleBar.setTypeface(Typeface.DEFAULT_BOLD);
            topBar.addView(dynTitleBar);

            ImageButton btnMenu = new ImageButton(this);
            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams((int) (40 * getResources().getDisplayMetrics().density), (int) (40 * getResources().getDisplayMetrics().density));
            btnParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            btnMenu.setLayoutParams(btnParams);
            btnMenu.setBackgroundColor(Color.TRANSPARENT);
            btnMenu.setImageResource(android.R.drawable.ic_menu_more);
            btnMenu.setOnClickListener(v -> showDynamicPopupMenu(v));
            topBar.addView(btnMenu);

            mainLayout.addView(topBar);

            dynMainContainer = new FrameLayout(this);
            dynMainContainer.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));

            View originalTerminal = findViewById(R.id.terminal_view);
            if (originalTerminal != null) {
                ViewGroup parent = (ViewGroup) originalTerminal.getParent();
                if (parent != null) parent.removeView(originalTerminal);
                dynMainContainer.addView(originalTerminal);
            }

            dynAgentWebView = new WebView(this);
            dynAgentWebView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            setupWebSettings(dynAgentWebView);
            dynAgentWebView.setWebViewClient(new WebViewClient());
            dynAgentWebView.loadUrl("http://localhost:5000");
            dynMainContainer.addView(dynAgentWebView);

            dynInternetLayout = new LinearLayout(this);
            dynInternetLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
            dynInternetLayout.setOrientation(LinearLayout.VERTICAL);

            dynUrlBar = new EditText(this);
            dynUrlBar.setLayoutParams(new LinearLayout.LayoutParams(-1, (int) (45 * getResources().getDisplayMetrics().density)));
            dynUrlBar.setBackgroundColor(Color.parseColor("#2D2D2D"));
            dynUrlBar.setHint("Enter URL or search...");
            dynUrlBar.setTextColor(Color.WHITE);
            dynUrlBar.setHintTextColor(Color.GRAY);
            dynUrlBar.setSingleLine(true);
            dynUrlBar.setImeOptions(EditorInfo.IME_ACTION_GO);

            dynInternetWebView = new WebView(this);
            dynInternetWebView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            setupWebSettings(dynInternetWebView);
            dynInternetWebView.setWebViewClient(new WebViewClient());

            dynUrlBar.setOnEditorActionListener((v, actionId, event) -> {
                String url = dynUrlBar.getText().toString();
                if (!url.startsWith("http")) url = "https://google.com" + url;
                dynInternetWebView.loadUrl(url);
                return true;
            });

            dynInternetLayout.addView(dynUrlBar);
            dynInternetLayout.addView(dynInternetWebView);
            dynMainContainer.addView(dynInternetLayout);

            mainLayout.addView(dynMainContainer);

            rootView.removeAllViews();
            rootView.addView(mainLayout);

            int savedBootView = dynPrefs.getInt("DefaultView", 0);
            switchDynamicInterface(savedBootView);

        } catch (Exception e) {
            Log.e("AgenticOSInjection", "UI Injection Failed", e);
        }
    }

    private void setupWebSettings(WebView wv) {
        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
    }

    private void showDynamicPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "🤖 Agent Chat");
        popup.getMenu().add(0, 2, 0, "🌐 Agent Browser");
        popup.getMenu().add(0, 3, 0, "💻 Termux Terminal");
        popup.getMenu().add(0, 4, 0, "🔄 Force Refresh Web");
        popup.getMenu().add(0, 5, 0, "⚙️ Set Current View as Default Boot");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) { switchDynamicInterface(0); return true; }
            if (id == 2) { switchDynamicInterface(1); return true; }
            if (id == 3) { switchDynamicInterface(2); return true; }
            if (id == 4) {
                if (dynAgentWebView != null) dynAgentWebView.reload();
                if (dynInternetWebView != null) dynInternetWebView.reload();
                return true;
            }
            if (id == 5) {
                dynPrefs.edit().putInt("DefaultView", dynActiveView).apply();
                Toast.makeText(this, "Boot view saved successfully!", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void switchDynamicInterface(int viewCode) {
        dynActiveView = viewCode;
        if (dynAgentWebView != null) dynAgentWebView.setVisibility(viewCode == 0 ? View.VISIBLE : View.GONE);
        if (dynInternetLayout != null) dynInternetLayout.setVisibility(viewCode == 1 ? View.VISIBLE : View.GONE);
        
        View tv = findViewById(R.id.terminal_view);
        if (tv != null) tv.setVisibility(viewCode == 2 ? View.VISIBLE : View.GONE);

        if (dynTitleBar != null) {
            if (viewCode == 0) dynTitleBar.setText("🤖 AGENT CHAT");
            if (viewCode == 1) dynTitleBar.setText("🌐 AGENT BROWSER");
            if (viewCode == 2) dynTitleBar.setText("💻 TERMINAL PRO");
        }
    }
}
