package com.myerstechnologies.extractor.VideoExtractionAPI.helpers;

import android.app.Activity;
import android.util.SparseArray;
import android.widget.Toast;

import com.myerstechnologies.extractor.VideoExtractionAPI.extractor.mkv.MKVExtractor;
import com.myerstechnologies.extractor.VideoExtractionAPI.extractor.mystream.MyStreamExtractor;
import com.myerstechnologies.extractor.VideoExtractionAPI.extractor.tv.TVM3U8Extractor;
import com.myerstechnologies.extractor.VideoExtractionAPI.extractor.upvid.UpVidExtractor;
import com.myerstechnologies.extractor.VideoExtractionAPI.extractor.vidoza.VidozaExtractor;
import com.myerstechnologies.extractor.VideoExtractionAPI.extractor.youtube.VideoMeta;
import com.myerstechnologies.extractor.VideoExtractionAPI.extractor.youtube.YouTubeExtractor;
import com.myerstechnologies.extractor.VideoExtractionAPI.extractor.youtube.YtFile;

public abstract class ExtractionHelper {
    private Activity context;

    public ExtractionHelper(Activity context) {
        this.context = context;
    }
    public void execute(String video){
        getVideoMP4Link(video);

    }

    private void getVideoMP4Link(String video) {
        String type = "none";
        if (video.contains("youtube")){
            type = "youtube";
        }
        if (video.contains("mystream")||video.contains("vudeo")||video.contains("mstream")){
            type = "mystream";
        }

        if (video.contains("upvid")){
            type = "upvid";
        }
        if (video.contains("nxload")){
            type = "nxload";
        }
        if (video.contains("vidoza")){
            type = "vidoza";
        }
        if (video.contains(".php")){
            type = "php";
        }
        if (video.contains(".m3u8")){
            type = "m3u";
        }
        switch (type) {
            case "youtube":
                runYoutube(video);
                break;
            case "mystream":
                runMyStream(video);
                break;
            case "upvid":
                runUpvid(video);
                break;
            case "mkv":
                runMKV(video);
                break;
            case "vidoza":
                runVidoza(video);
                break;
            case "php":
                runPHP(video);
                break;
            case "m3u":
                runM3U(video);
                break;
        }
    }

    private void runM3U(String video) {
      donne(video);
    }

    private void runPHP(String videop) {
        if (videop.contains(".php")) {
            if (!videop.contains("channelstream.club")) {
                new TVM3U8Extractor(context) {
                    @Override
                    protected void onExtractionComplete(String m3u8) {
                        donne(m3u8);
                    }

                    @Override
                    protected void onExtractionError(String link) {

                    }
                }.extract(videop);
            } else {
                String newVideo = videop.replace("channelstream.club", "embed-channel.stream");
                donne(newVideo);

            }
        }
    }

    private void runVidoza(String videop) {
        new VidozaExtractor(context){

            @Override
            protected void onExtractionComplete(String mp4) {
                donne(mp4);
            }

            @Override
            protected void onExtractionError(String link) {
                Toast.makeText(context, link, Toast.LENGTH_SHORT).show();
            }
        }.extract(videop);
    }

    private void runMKV(String videop) {
        new MKVExtractor(context){

            @Override
            protected void onExtractionComplete(String mp4) {
                donne(mp4);
            }

            @Override
            protected void onExtractionError(String link) {
                Toast.makeText(context, "Error Loading Video", Toast.LENGTH_SHORT).show();
            }
        }.extract(videop);
    }

    private void runUpvid(String videop) {
        new UpVidExtractor(context) {

            @Override
            protected void onExtractionComplete(String mp4) {
                donne(mp4);
            }

            @Override
            protected void onExtractionError(String link) {
                Toast.makeText(context, "Error Loading Video", Toast.LENGTH_SHORT).show();
            }
        }.extract(videop);

    }

    private void runMyStream(String videop) {
        new MyStreamExtractor(context){

            @Override
            protected void onExtractionComplete(String mp4) {
                donne(mp4);
            }

            @Override
            protected void onExtractionError(String link) {
                Toast.makeText(context, link, Toast.LENGTH_SHORT).show();
            }
        }.extract(videop);
    }

    private void runYoutube(String videop) {
        final String youtubeLink = videop;
        new YouTubeExtractor(context) {
            @Override
            protected void onExtractionError(String link) {
                if (!videop.contains("embed")) {
                    String[] nameSplit = youtubeLink.split("v=");
                    String finalName = nameSplit[1];
                    link = link + finalName;
                } else {
                    link = videop;
                }
                donne(link);
            }

            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = 18;
                    String downloadUrl = youtubeLink;
                    try {
                        downloadUrl = ytFiles.get(itag).getUrl();
                    } catch (NullPointerException e){
                        for (int i = 1; i < 1000; i++) {
                            try {
                                downloadUrl = ytFiles.get(i).getUrl();
                                break;
                            } catch (NullPointerException e0){
                            }

                        }
                    }

                    donne(downloadUrl);

                }
            }
        }.extract(youtubeLink, true, true);
    }

    public abstract void donne(String mp4);
}
