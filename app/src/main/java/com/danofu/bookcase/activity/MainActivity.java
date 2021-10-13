package com.danofu.bookcase.activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.danofu.bookcase.InsertContentTread;
import com.danofu.bookcase.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.danofu.bookcase.Constants.ACTION_ADD_BOOK;
import static com.danofu.bookcase.Constants.INTERFACE_STATE_BOOKS;
import static com.danofu.bookcase.Constants.INTERFACE_STATE_READERS;
import static com.danofu.bookcase.Constants.NOTIFICATION_CHANNEL_ID;
import static com.danofu.bookcase.Constants.REQUEST_CODE_CALL_PHONE_PERMISSION;
import static com.danofu.bookcase.Constants.REQUEST_CODE_READ_CONTACTS_PERMISSION;
import static com.danofu.bookcase.Constants.SHARED_PREFERENCES_INTERFACE_STATE_NAME;
import static com.danofu.bookcase.Constants.SHARED_PREFERENCES_NAME_MAIN_ACTIVITY;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    public TextView loadingTip, noContent;
    public LinearLayout contentWrapper;

    public ExecutorService executor;
    public Runnable showLoadingTip;
    private InsertContentTread insertContentTread;
    private String phoneToCall;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_CONTACTS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED)
                    insertContent(INTERFACE_STATE_READERS);
                else
                    Toast.makeText(this, R.string.toast_read_contacts_permission,
                            Toast.LENGTH_LONG).show();
                break;
            case REQUEST_CODE_CALL_PHONE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneToCall));
                    startActivity(intent);
                } else
                    Toast.makeText(this, R.string.toast_phone_call_permission, Toast.LENGTH_SHORT).
                            show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (executor == null)
            executor = Executors.newFixedThreadPool(1);

        insertContent(getCurrentInterface());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        findViewById(R.id.booksButton).setOnClickListener(v ->
                insertContent(INTERFACE_STATE_BOOKS));
        findViewById(R.id.readersButton).setOnClickListener(v ->
                insertContent(INTERFACE_STATE_READERS));

        loadingTip = findViewById(R.id.loadingTip);
        noContent = findViewById(R.id.noContent);
        contentWrapper = findViewById(R.id.contentWrapper);

        showLoadingTip = () -> loadingTip.setVisibility(View.VISIBLE);
    }

    // inserts content based on given interface state
    private void insertContent(String interfaceState) {
        if (interfaceState.equals(INTERFACE_STATE_READERS) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !isReadContactsPermissionGranted()) {
            askForReadContactsPermission();
            return;
        }

        // remembers interface state
        SharedPreferences.Editor editor =
                getSharedPreferences(SHARED_PREFERENCES_NAME_MAIN_ACTIVITY, MODE_PRIVATE).edit();
        editor.putString(SHARED_PREFERENCES_INTERFACE_STATE_NAME, interfaceState).
                apply();
        editor.commit();

        toggleInterface(interfaceState);

        stopInsertContentThread();
        insertContentTread = new InsertContentTread(this, interfaceState);
        insertContentTread.start();
    }

    public void removeOldContent() {
        contentWrapper.removeAllViews();
        noContent.setVisibility(View.GONE);
    }

    private void stopInsertContentThread() {
        if (insertContentTread != null && insertContentTread.isRunning())
            insertContentTread.stop();
    }

    public String getCurrentInterface() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(SHARED_PREFERENCES_NAME_MAIN_ACTIVITY, MODE_PRIVATE);
        return sharedPreferences.
                getString(SHARED_PREFERENCES_INTERFACE_STATE_NAME, INTERFACE_STATE_BOOKS);
    }

    // add content button on click listener
    // starts an activity due to button text; may start `EditBookActivity` or open contacts activity
    public void onAddContentButton(View view) {
        Button btn = (Button) view;
        String btnText = btn.getText().toString();

        if (btnText.equals(getString(R.string.main_add_content_book))) {
            Intent intent = new Intent(this, EditBookActivity.class);
            intent.setAction(ACTION_ADD_BOOK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            startActivity(intent);
        }
    }

    // toggles interface
    public void toggleInterface(String interfaceState) {
        Button addContentBnt = findViewById(R.id.addContentButton);
        Button booksButton = findViewById(R.id.booksButton);
        Button readersButton = findViewById(R.id.readersButton);

        switch (interfaceState) {
            case INTERFACE_STATE_BOOKS:
                booksButton.setEnabled(false);
                readersButton.setEnabled(true);
                addContentBnt.setText(R.string.main_add_content_book);
                break;
            case INTERFACE_STATE_READERS:
                booksButton.setEnabled(true);
                readersButton.setEnabled(false);
                addContentBnt.setText(R.string.main_add_content_reader);
                break;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.main_notification_channel_name);
            String description = getString(R.string.main_notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // asks for read contacts permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void askForReadContactsPermission() {
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_CODE_READ_CONTACTS_PERMISSION);
    }

    // checks if read contacts permission is granted
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isReadContactsPermissionGranted() {
        return checkSelfPermission(Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED;
    }

    // asks for call phone permission
    public void askForCallPhonePermission(String phoneNumber) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            phoneToCall = phoneNumber;
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CODE_CALL_PHONE_PERMISSION);
        }
    }

    // checks if call phone permission is granted
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isCallPhonePermissionGranted() {
        return checkSelfPermission(Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED;
    }

}
