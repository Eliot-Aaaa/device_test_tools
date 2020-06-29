package com.example.devicetest.module;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.example.devicetest.R;

public class WebPageActivity extends Activity {

    WebView webView;
    EditText edit_url;
    ImageButton turn_to_page;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_web);
        edit_url = findViewById(R.id.edit_url);
        turn_to_page = findViewById(R.id.turn_to_page);
        webView = findViewById(R.id.webview);

        turn_to_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = edit_url.getText().toString();
                if (!url.isEmpty())
                {
                    if ( (url.length() > 7 && url.substring(0, 7).equals("http://")) || (url.length() > 8 && url.substring(0, 8).equals("https://")) )
                        url = url;
                    else
                        url = "http://" + url;
                    webView.loadUrl(url);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }
}
