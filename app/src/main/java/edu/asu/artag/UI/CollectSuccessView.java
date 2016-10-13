package edu.asu.artag.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import edu.asu.artag.R;

public class CollectSuccessView extends AppCompatActivity {

    private String mImageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_success_view);

        Intent intent = getIntent();
        mImageURL = intent.getStringExtra("ImageURL");

        ImageView mImageView = (ImageView) findViewById(R.id.collected_tag);

        Picasso.with(this)
                .load(mImageURL)
                .resize(400,600)
                .into(mImageView);

    }
}
