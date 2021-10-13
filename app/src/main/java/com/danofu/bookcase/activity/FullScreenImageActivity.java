package com.danofu.bookcase.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.danofu.bookcase.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.danofu.bookcase.Constants.EXTRA_IMAGE_FILEPATH;

public class FullScreenImageActivity extends Activity {

    private static final String TAG = "FullScreenImageActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        Bitmap imageBitmap =
                BitmapFactory.decodeFile(getIntent().getStringExtra(EXTRA_IMAGE_FILEPATH));
        ((PhotoView) findViewById(R.id.fullScreenImageView)).setImageBitmap(imageBitmap);
    }

}
