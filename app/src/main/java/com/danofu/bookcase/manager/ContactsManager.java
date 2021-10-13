package com.danofu.bookcase.manager;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.danofu.bookcase.R;
import com.danofu.bookcase.databaseobject.Reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.danofu.bookcase.Constants.NOTIFICATION_CHANNEL_ID;
import static com.danofu.bookcase.Constants.NOTIFICATION_MAIN_ID;
import static com.danofu.bookcase.Constants.READER_PHOTOS_FOLDER_NAME;

public class ContactsManager {

    public static List<Reader> getContactsAsReaders(Context context) {
        List<Reader> readers = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.CONTACT_ID,
                        ContactsContract.Data.HAS_PHONE_NUMBER,
                },
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst())
            do {
                int numberAmount = cursor.getInt(
                        cursor.getColumnIndex(ContactsContract.Data.HAS_PHONE_NUMBER));
                if (numberAmount <= 0)
                    continue;

                int contactId = cursor.getInt(cursor.getColumnIndex(
                        ContactsContract.Data.CONTACT_ID));
                String selection = ContactsContract.Data.MIMETYPE + " = ? AND " +
                        ContactsContract.Data.CONTACT_ID + " = ?";

                // gets contact number
                Cursor cCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        selection,
                        new String[]{
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                String.valueOf(contactId)
                        },
                        null
                );

                String number = null;
                if (cCursor != null & cCursor.moveToFirst()) {
                    number = cCursor.getString(cCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    cCursor.close();
                }

                // gets contact first and last names
                cCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{
                                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
                        },
                        selection,
                        new String[]{
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                                String.valueOf(contactId),
                        },
                        null
                );

                String firstName = null, lastName = null;
                if (cCursor != null && cCursor.moveToFirst()) {
                    firstName = cCursor.getString(cCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                    lastName = cCursor.getString(cCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                    cCursor.close();
                }

                // gets contact address
                cCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{
                                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
                        },
                        selection,
                        new String[]{
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                                String.valueOf(contactId)
                        },
                        null
                );

                String address = null;
                if (cCursor != null && cCursor.moveToFirst()) {
                    address = cCursor.getString(cCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                    cCursor.close();
                }

                // gets contact email
                cCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS},
                        selection,
                        new String[]{
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                                String.valueOf(contactId)
                        },
                        null
                );

                String email = null;
                if (cCursor != null && cCursor.moveToFirst()) {
                    email = cCursor.getString(cCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Email.ADDRESS));
                    cCursor.close();
                }

                // gets contact photo file id
                cCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID},
                        selection,
                        new String[]{
                                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE,
                                String.valueOf(contactId)
                        },
                        null
                );

                Integer photoFileId = null;
                if (cCursor != null && cCursor.moveToFirst()) {
                    photoFileId = cCursor.getInt(cCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Photo.PHOTO_FILE_ID));
                    cCursor.close();
                }

                // saves contact display photo
                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                        contactId);
                Uri displayPhotoUri = Uri.withAppendedPath(contactUri,
                        ContactsContract.Contacts.Photo.DISPLAY_PHOTO);

                String photoLink = null;
                try (AssetFileDescriptor assetFileDescriptor =
                             contentResolver.openAssetFileDescriptor(displayPhotoUri, "r")) {
                    File outputPhotoFile = new File(context.getFilesDir(),
                            READER_PHOTOS_FOLDER_NAME + "/" + photoFileId + ".jpg");

                    outputPhotoFile.getParentFile().mkdirs();
                    outputPhotoFile.createNewFile();

                    FileInputStream fileInputStream = assetFileDescriptor.createInputStream();
                    FileOutputStream fileOutputStream = new FileOutputStream(outputPhotoFile);
                    byte[] bytes = new byte[fileInputStream.available()];

                    fileInputStream.read(bytes);
                    fileOutputStream.write(bytes);

                    photoLink = READER_PHOTOS_FOLDER_NAME + "/" + outputPhotoFile.getName();
                    fileOutputStream.close();
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                readers.add(new Reader(number, firstName, lastName, address, email, photoLink));
            } while (cursor.moveToNext());

        if (cursor != null)
            cursor.close();

        return readers;
    }

    // checks if contacts were updated
    // and update them
    public static boolean isContactsUpdated(Context context) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_book_image_default)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.notification_readers_updating))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(context.getString(R.string.notification_readers_updating)))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_MAIN_ID, builder.build());

        SQLiteManager database = new SQLiteManager(context);
        List<Reader> newReaders = getContactsAsReaders(context);
        List<Reader> oldReaders = database.getReaders();
        boolean isUpdated = false;

        for (Reader newReader : newReaders) {
            Reader oldReader = database.getReader(newReader.getId());

            if (!newReader.equals(oldReader))
                isUpdated = true;
        }

        for (Reader oldReader : oldReaders) {
            boolean isExists = false;

            for (Reader newReader : newReaders)
                if (oldReader.getId().equals(newReader.getId())) {
                    isExists = true;
                    break;
                }

            if (!isExists) {
                isUpdated = true;
                break;
            }
        }

        if (isUpdated)
            database.syncReaders(newReaders, oldReaders);

        notificationManager.cancel(NOTIFICATION_MAIN_ID);
        database.close();
        return isUpdated;
    }

}