package com.danofu.bookcase.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.danofu.bookcase.R;
import com.danofu.bookcase.databaseobject.Book;
import com.danofu.bookcase.manager.SQLiteManager;
import com.danofu.bookcase.manager.XMLManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.danofu.bookcase.Constants.ACTION_ADD_BOOK;
import static com.danofu.bookcase.Constants.ACTION_EDIT_BOOK;
import static com.danofu.bookcase.Constants.BOOK_IMAGES_FOLDER_NAME;
import static com.danofu.bookcase.Constants.EXTRA_BOOK_ID;
import static com.danofu.bookcase.Constants.REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION;
import static com.danofu.bookcase.Constants.REQUEST_CODE_UPLOAD_IMAGE;

public class EditBookActivity extends Activity {

    private static final String TAG = "EditBookActivity";

    private Uri imageUri = null;
    private Book.BookStatus bookStatus = Book.BookStatus.FREE;

    private TextView uploadedImageTip;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_UPLOAD_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    imageUri = data.getData();

                    Button uploadImageBtn = findViewById(R.id.uploadImageButton);
                    uploadImageBtn.setBackground(ContextCompat.
                            getDrawable(this, R.drawable.bg_edit_book_remove_image_button));
                    uploadImageBtn.setText(getString(R.string.edit_book_remove_image_button));
                    uploadImageBtn.setOnClickListener(v -> removeImage());

                    putUploadedImageTip();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    uploadImage();
                else
                    Toast.makeText(this, R.string.toast_read_external_storage_permission,
                            Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        uploadedImageTip = findViewById(R.id.uploadedImageTip);
        findViewById(R.id.uploadImageButton).setOnClickListener(v -> uploadImage());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());

        Button saveBookButton = findViewById(R.id.saveBookButton);
        switch (getIntent().getAction()) {
            case ACTION_ADD_BOOK:
                saveBookButton.setText(R.string.edit_book_add_button);
                saveBookButton.setOnClickListener(v -> addBook());
                break;
            case ACTION_EDIT_BOOK:
                String id = getIntent().getStringExtra(EXTRA_BOOK_ID);

                findViewById(R.id.idWrapper).setVisibility(View.GONE);
                saveBookButton.setText(R.string.edit_book_save_button);
                saveBookButton.setOnClickListener(v -> saveBook());
                fillActivity(id);
                break;
        }
    }

    // update Book in xml file and database
    private void saveBook() {
        Book book = getBook();

        new SQLiteManager(this).updateBook(book).close();
        new XMLManager(this).updateBook(book);

        finish();
    }

    // fills edit texts and chang upload image button if image is already exists
    private void fillActivity(String id) {
        SQLiteManager database = new SQLiteManager(this);
        Book book = database.getBook(id);
        bookStatus = book.getStatus();

        String position = book.getId().split("x")[0];
        String shelf = book.getId().split("x")[1];

        ((EditText) findViewById(R.id.positionEditText)).setText(position);
        ((EditText) findViewById(R.id.shelfEditText)).setText(shelf);
        ((EditText) findViewById(R.id.bookNameEditText)).setText(book.getName());
        ((EditText) findViewById(R.id.authorEditText)).setText(book.getAuthor());
        ((EditText) findViewById(R.id.writingDateEditText)).setText(book.getWritingDate());
        ((EditText) findViewById(R.id.articleLinkEditText)).setText(book.getArticleLink());
        ((EditText) findViewById(R.id.descriptionEditText)).setText(book.getDescription());

        if (book.getImageLink() != null) {
            String imageFilename =
                    new File(getFilesDir() + "/" + book.getImageLink()).getName();
            uploadedImageTip.setText(imageFilename);

            Button uploadImageBtn = findViewById(R.id.uploadImageButton);
            uploadImageBtn.setBackground(ContextCompat.getDrawable(this,
                    R.drawable.bg_edit_book_remove_image_button));
            uploadImageBtn.setText(R.string.edit_book_remove_image_button);
            uploadImageBtn.setOnClickListener(view -> removeImage());
        }

        database.close();
    }

    // adds book to the database and xml storage if it's valid
    private void addBook() {
        if (isBookValid()) {
            Book book = getBook();

            new XMLManager(this).saveBook(book);
            new SQLiteManager(this).insertBook(book).close();
            finish();
        }
    }

    // returns Book with gathered information from activity
    private Book getBook() {
        EditText positionEditText = findViewById(R.id.positionEditText);
        EditText shelfEditText = findViewById(R.id.shelfEditText);
        String position = positionEditText.getText().toString().trim();
        String shelf = shelfEditText.getText().toString().trim();
        String id = position + "x" + shelf;

        String name = null, author = null, writingDate = null, description = null,
                articleLink = null, imageLink = null;

        // book name
        String editTextValue = ((EditText) findViewById(R.id.bookNameEditText)).
                getText().toString().trim();
        if (!editTextValue.equals(""))
            name = editTextValue;

        // author
        editTextValue = ((EditText) findViewById(R.id.authorEditText)).
                getText().toString().trim();
        if (!editTextValue.equals(""))
            author = editTextValue;

        // writing date
        editTextValue = ((EditText) findViewById(R.id.writingDateEditText)).
                getText().toString().trim();
        if (!editTextValue.equals(""))
            writingDate = editTextValue;

        // description
        editTextValue = ((EditText) findViewById(R.id.descriptionEditText)).
                getText().toString().trim();
        if (!editTextValue.equals(""))
            description = editTextValue;

        // article link
        editTextValue = ((EditText) findViewById(R.id.articleLinkEditText)).
                getText().toString().trim();
        if (!editTextValue.equals(""))
            articleLink = editTextValue;

        // image link
        String uploadedImageTipText = uploadedImageTip.getText().toString();
        if (!uploadedImageTipText.equals(getString(R.string.edit_book_uploaded_image_tip)))
            if (imageUri != null) {
                File outputImageFile = new File(getFilesDir(),
                        BOOK_IMAGES_FOLDER_NAME + "/" + uploadedImageTipText);
                if (!outputImageFile.exists()) {
                    if (saveImage(outputImageFile))
                        imageLink = BOOK_IMAGES_FOLDER_NAME + "/" + uploadedImageTipText;
                } else
                    imageLink = BOOK_IMAGES_FOLDER_NAME + "/" + uploadedImageTipText;
            } else
                imageLink = BOOK_IMAGES_FOLDER_NAME + "/" + uploadedImageTipText;

        return new Book(id, name, author, writingDate, description, articleLink, bookStatus,
                imageLink);
    }

    // checks if book valid
    // checks if position and shelf is not empty and book isn't already exists in database
    private boolean isBookValid() {
        EditText positionEditText = findViewById(R.id.positionEditText);
        EditText shelfEditText = findViewById(R.id.shelfEditText);
        String position = positionEditText.getText().toString().trim();
        String shelf = shelfEditText.getText().toString().trim();

        if (position.equals("") || shelf.equals("")) {
            Drawable bgEditTextRequired = ResourcesCompat.getDrawable(getResources(),
                    R.drawable.bg_edit_text_required, null);

            if (position.equals(""))
                positionEditText.setBackground(bgEditTextRequired);
            if (shelf.equals(""))
                shelfEditText.setBackground(bgEditTextRequired);

            Toast.makeText(this, getString(R.string.toast_required_fields),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        SQLiteManager database = new SQLiteManager(this);
        String bookId = position + "x" + shelf;

        if (database.isBookExists(bookId)) {
            Drawable bgEditTextRequired = ResourcesCompat.getDrawable(getResources(),
                    R.drawable.bg_edit_text_required, null);

            positionEditText.setBackground(bgEditTextRequired);
            shelfEditText.setBackground(bgEditTextRequired);
            Toast.makeText(this, getString(R.string.toast_book_already_exists),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        database.close();
        return true;
    }

    // shows to user an activity to chose an image to upload
    private void uploadImage() {
        // makes sure that app can read an image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !isReadExternalStoragePermissionGranted()) {
            askForReadExternalStoragePermission();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_UPLOAD_IMAGE);
    }

    // sets uploaded image name to upload image tip and saves image filepath
    private void putUploadedImageTip() {
        Cursor cursor = getContentResolver().query(
                imageUri,
                new String[]{MediaStore.Images.Media.DISPLAY_NAME, /* image filename */},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String imageFilename = cursor.getString(
                    cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            uploadedImageTip.setText(imageFilename);
            cursor.close();
        }
    }

    // removes image
    private void removeImage() {
        String imageFilename = uploadedImageTip.getText().toString();
        new File(getFilesDir(), BOOK_IMAGES_FOLDER_NAME + "/" + imageFilename).delete();

        uploadedImageTip.setText(getString(R.string.edit_book_uploaded_image_tip));
        imageUri = null;

        Button uploadImageBtn = findViewById(R.id.uploadImageButton);
        uploadImageBtn.setText(getString(R.string.edit_book_upload_image_button));
        uploadImageBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button));
        uploadImageBtn.setOnClickListener(view -> uploadImage());
    }

    // saves image to app files
    // and returns true if file was saved
    private boolean saveImage(File outputFile) {
        try (AssetFileDescriptor assetFileDescriptor = getContentResolver().
                openAssetFileDescriptor(imageUri, "r")) {
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();

            FileInputStream fileInputStream = assetFileDescriptor.createInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            byte[] bytes = new byte[fileInputStream.available()];

            fileInputStream.read(bytes);
            fileOutputStream.write(bytes);

            fileOutputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // asks for read external storage permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void askForReadExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
    }

    // checks if read external storage permission is granted
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isReadExternalStoragePermissionGranted() {
        return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
    }

}
