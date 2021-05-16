package com.myerstechnologies.extractor.VideoExtractionAPI.extractor.uqload;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.evgenii.jsevaluator.JsEvaluator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public abstract class UqloadExtractor {
    private Activity context;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";
    private String url;
    private Thread worker;
    private boolean isLoaded;
    private Timer timerObj;
    public UqloadExtractor(Activity context) {
        this.context = context;
    }


    public void extract(String myStreamLink) {
        this.url = myStreamLink;
        isLoaded = false;
        worker = new Thread(new Runnable(){

            @Override
            public void run() {
                if (worker.isInterrupted()){
                    onExtractionError("Error");
                }
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getStreamMP4();
                    }
                });
            }


        });
        setTimeOut(url, 5000);
        worker.run();

    }

    private void setTimeOut(String link, int timeWait){

        timerObj = new Timer();
        TimerTask timerTaskObj = new TimerTask() {
            public void run() {
                if (!isLoaded){
                    extract(link);
                } else {
                    timerObj.cancel();
                }
            }
        };
        timerObj.schedule(timerTaskObj, timeWait, timeWait);

    }


    private String getStreamMP4(){
        final String[] video = {""};
        final String[] image = {""};
        final String[] javascriptFile = {"nothing"};
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
                    public WebResourceResponse shouldInterceptRequest(WebView view,     String url) {
                        if (url.contains(".jpg")){
                            image[0] = url;
                        }

                        return super.shouldInterceptRequest(view, url);
                    }
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        webView.evaluateJavascript(
                                "(function() { return (document.getElementsByTagName('video').src); })();",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String html) {
                                        // code here
                                        if (html.contains(".mp4")) {
                                            String[] parser = html.split(Pattern.quote("\\"));
                                            for (int i = 0; i < parser.length; i++) {
                                                if (parser[i].contains("v.mp4")) {
                                                    String videoElement = "<video data-html5-video=\"\" src=\"" + parser[i].split("\"")[1] +"\" poster=\"" +image[0] + "\" preload=\"none\"><style class=\"clappr-style\">[data-html5-video]{position:absolute;height:100%;width:100%;display:block}</style></video>";
                                                    Log.d("FNISHED", videoElement);

                                                }
                                            }
                                        }
                                    }

                                });

                    }
                });

                webView.loadUrl(url);


            }
        });
        return javascriptFile[0];

    }
    protected abstract void onExtractionComplete(String mp4);

    protected abstract void onExtractionError(String link);

}
