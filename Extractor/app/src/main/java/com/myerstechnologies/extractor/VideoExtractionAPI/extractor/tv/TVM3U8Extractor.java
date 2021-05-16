package com.myerstechnologies.extractor.VideoExtractionAPI.extractor.tv;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.evgenii.jsevaluator.JsEvaluator;

import java.util.Timer;
import java.util.TimerTask;


public abstract class TVM3U8Extractor {
    private Activity context;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";
    private String url;
    private boolean isLoaded;
    private Thread worker;
    private Timer timerObj;
    public TVM3U8Extractor(Activity context) {
        this.context = context;
    }

    public void extract(String myStreamLink)
    {
        this.url = myStreamLink;
        worker = new Thread(new Runnable(){

            @Override
            public void run() {
                if (worker.isInterrupted()){
                    Log.d("Interrupted", "Interrupted Task");
                }
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getRealUrl(myStreamLink);
                    }
                });
            }
        });
        setTimeOut(url, 10000);
        worker.run();
    }
    private void setTimeOut(String link, int timeWait){

        timerObj = new Timer();
        TimerTask timerTaskObj = new TimerTask() {
            public void run() {
                //perform your action here
                if (!isLoaded){
                    extract(link);
                } else {
                    timerObj.cancel();
                }
            }
        };
        timerObj.schedule(timerTaskObj, timeWait, timeWait);

    }


    private String getRealUrl(String realUrlLink){
        final String[] realUrl = {realUrlLink};
        final boolean[] found = {false};
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                JsEvaluator jsEvaluator = new JsEvaluator(context);
                WebView webView = jsEvaluator.getWebView();
                webView.getSettings().setUserAgentString(USER_AGENT);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setSaveFormData(true);
                webView.getSettings().setAllowContentAccess(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setAllowFileAccessFromFileURLs(true);
                webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                webView.getSettings().setSupportZoom(true);
                webView.setClickable(true);
                webView.setWebChromeClient(new WebChromeClient());
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                            if (request.getUrl().toString().contains(".m3u8")) {
                                onExtractionComplete(request.getUrl().toString());
                                Log.d("M3U", request.getUrl().toString());
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        webView.loadUrl("about:blank");
                                        timerObj.cancel();
                                        jsEvaluator.destroy();
                                        worker.interrupt();
                                    }
                                });

                                isLoaded = true;
                            }

                        return super.shouldInterceptRequest(view, request);
                    }
                });
                webView.loadUrl(realUrl[0]);
            }
        });
        return realUrl[0];
    }
    protected abstract void onExtractionComplete(String m3u8);

    protected abstract void onExtractionError(String link);
}
