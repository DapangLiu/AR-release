package edu.asu.artag.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.squareup.picasso.Picasso;

import edu.asu.artag.R;

public class ImagePreview extends AppCompatActivity {

    private View mImageView;
    private View mButton;
    private String mEmail;
    private String mTagID;
    private String mImageURL;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);


        Intent intent = getIntent();
        mEmail = intent.getStringExtra("email");
        mTagID = intent.getStringExtra("tag_id");
        mImageURL = intent.getStringExtra("tagImage");


        mImageView = findViewById(R.id.imageView);
        mButton = findViewById(R.id.collectButton);

        OnShowcaseEventListener showcaseEventListener = new OnShowcaseEventListener() {
            @Override
            public void onShowcaseViewHide(ShowcaseView showcaseView) {

            }

            @Override
            public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                Picasso.with(ImagePreview.this)
                        .load(mImageURL)
                        .into((ImageView) mImageView);

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ImagePreview.this, CollectCameraActivity.class);
                        intent.putExtra("email", mEmail);
                        intent.putExtra("tag_id",mTagID);
                        intent.putExtra("tagImage",mImageURL);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onShowcaseViewShow(ShowcaseView showcaseView) {

            }

            @Override
            public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

            }
        };


        Target viewTarget = new ViewTarget(R.id.collectButton, this);
        new ShowcaseView.Builder(this)
                .setTarget(viewTarget)
                .setContentTitle("Click this button to collect your tag!")
                .setContentText(R.string.string_imagePreview)
                .setShowcaseEventListener(showcaseEventListener)
                .build();

    }
}
