package com.myerstechnologies.extractor.VideoExtractionAPI.extractor.upvid;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.evgenii.jsevaluator.JsEvaluator;

import java.util.Timer;
import java.util.TimerTask;

public abstract class UpVidExtractor{
    private Activity context;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";
    private String url;
    private boolean isLoaded;
    private Thread worker;
    private  Timer timerObj;
    public UpVidExtractor(Activity context) {
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

                            if (!realUrlLink.contains("upvid.biz") && !realUrlLink.contains("-_-")) {
                                if (request.getUrl().toString().contains("upvid.biz") && request.getUrl().toString().contains("-_-")
                                        && !request.getUrl().toString().contains("google-analytics")) {
                                    if (!request.getUrl().toString().contains("==.html")) {
                                        realUrl[0] = request.getUrl().toString();
                                        url = realUrl[0];
                                        getStreamMP4();
                                    }
                                }
                            } else {
                                if (request.getUrl().toString().contains("upvid.biz") && request.getUrl().toString().contains("-_-")
                                        && !request.getUrl().toString().contains("google-analytics")) {
                                    if (request.getUrl().toString().contains("==.html")) {
                                        if (!found[0]) {
                                            Log.d("REQUEST HERE", request.getUrl().toString());
                                            realUrl[0] = request.getUrl().toString();
                                            url = realUrl[0];
                                            getStreamMP4();
                                            found[0] = true;
                                        }
                                    }
                                }
                            }
                            return super.shouldInterceptRequest(view, request);
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            webView.loadUrl("about:blank");
                            jsEvaluator.destroy();
                        }
                    });
                    webView.loadUrl(realUrl[0]);
                }
            });
        return realUrl[0];
    }

    private String getStreamMP4(){
        isLoaded = false;
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
                    public void onPageFinished(WebView view, String url1) {

                        super.onPageFinished(view, url1);
                        Log.d("finished", "FINISHED");
                        webView.evaluateJavascript(
                                "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String html) {
                                        // code here
                                        if (html.contains(".mp4")){
                                            String[] parser = html.split("tsource\\.setAttribute\\('src', '");
                                            for (int i = 0; i < parser.length; i++){
                                                if (parser[i].contains(".mp4")) {

                                                    javascriptFile[0] = parser[i].split("'\\)")[0];
                                                    Log.d("FILE MP4", javascriptFile[0]);
                                                }
                                            }
                                            webView.loadUrl("about:blank");
                                            isLoaded = true;
                                            timerObj.cancel();
                                            if (javascriptFile[0].contains(".mp4")){
                                                onExtractionComplete(javascriptFile[0]);
                                            } else {
                                                onExtractionError("Error");
                                            }
                                            jsEvaluator.destroy();
                                            worker.interrupt();
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
