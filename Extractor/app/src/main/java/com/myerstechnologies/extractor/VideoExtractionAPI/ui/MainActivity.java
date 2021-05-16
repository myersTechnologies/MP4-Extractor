package com.myerstechnologies.extractor.VideoExtractionAPI.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.myerstechnologies.extractor.R;
import com.myerstechnologies.extractor.VideoExtractionAPI.helpers.ExtractionHelper;

public class MainActivity extends AppCompatActivity {

    private TextView link;
    private EditText rawLink;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        link = findViewById(R.id.link_text);
        rawLink = findViewById(R.id.edit_text);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(rawLink.getText())){
                    String video = rawLink.getText().toString();
                    new ExtractionHelper(MainActivity.this){
                        @Override
                        public void donne(String mp4) {
                            link.setText(mp4);
                        }
                    }.execute(video);
                }
            }
        });
    }


}