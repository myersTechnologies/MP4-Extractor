package com.myerstechnologies.extractor.VideoExtractionAPI.extractor.mkv;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public abstract class MKVExtractor {
    private Activity context;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";
    private String url;
    private boolean isLoaded;
    private Timer timerObj;
    private Thread worker;
    public MKVExtractor(Activity context) {
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
                    boolean contains = false;
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        if(request.getUrl().toString().contains("pubdirecte")||url.contains("//pubdirecte.com/script/pop.php")||request.getUrl().toString().contains("https://offerimage.com/www/")
                                ||request.getUrl().toString().contains("https://littlecdn.com/contents/") || request.getUrl().toString().contains("h//cdn.betgorebysson.club/fac.php")){
                            InputStream textStream = new ByteArrayInputStream("".getBytes());
                            return getTextWebResource(textStream);
                        } else {
                            if (request.getUrl().toString().contains(".mp4")) {
                                if (!javascriptFile[0].contains(".mp4")) {
                                    javascriptFile[0] = request.getUrl().toString();
                                    contains = true;
                                    if (contains) {
                                        onExtractionComplete(javascriptFile[0]);
                                        isLoaded = true;
                                    } else {
                                        onExtractionError(javascriptFile[0]);
                                        isLoaded = true;
                                    }

                                    timerObj.cancel();
                                    worker.interrupt();
                                }
                            }
                            if (request.getUrl().toString().contains(".m3u8") && request.getUrl().toString().contains("master.m3u8") ) {
                                Log.d("FNISHED", request.getUrl().toString());
                                if (!javascriptFile[0].contains(".m3u8")) {
                                    javascriptFile[0] = request.getUrl().toString();
                                    contains = true;
                                    if (contains){
                                        onExtractionComplete(javascriptFile[0]);
                                        isLoaded = true;
                                    } else {
                                        onExtractionError(javascriptFile[0]);
                                        isLoaded = true;
                                    }

                                    timerObj.cancel();
                                    worker.interrupt();
                                }
                            }
                        }
                        return super.shouldInterceptRequest(view, request);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);

                    }
                });

                webView.loadUrl(url);


            }
        });
        return javascriptFile[0];

    }
    protected abstract void onExtractionComplete(String mp4);

    protected abstract void onExtractionError(String link);


    private WebResourceResponse getTextWebResource(InputStream data) {
        return new WebResourceResponse("text/plain", "UTF-8", data);
    }
}
