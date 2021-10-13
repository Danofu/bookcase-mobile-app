package com.danofu.bookcase.contacts;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.danofu.bookcase.R;
import com.danofu.bookcase.databaseobject.Reader;
import com.danofu.bookcase.manager.ContactsManager;
import com.danofu.bookcase.manager.SQLiteManager;

import java.util.List;
import java.util.concurrent.Executor;

public class ContactsRepository {

    private Context context;
    private Executor executor;

    public ContactsRepository(Context context, Executor executor) {
        this.context = context;
        this.executor = executor;
    }

    public interface ContactsRepositoryCallback {
        interface Contacts extends ContactsRepositoryCallback {
            void onContactsSynced(boolean updated);
        }

        interface Readers extends ContactsRepositoryCallback {
            void onReadersRetrieved(List<Reader> readers);
        }
    }

    public void syncContacts(ContactsRepositoryCallback callback) {
        executor.execute(() -> {
            if (callback instanceof ContactsRepositoryCallback.Contacts) {
                boolean isUpdated = isContactsUpdated();
                ((ContactsRepositoryCallback.Contacts) callback).onContactsSynced(isUpdated);
            }

            if (callback instanceof ContactsRepositoryCallback.Readers) {
                List<Reader> readers = retrieveReaders();
                ((ContactsRepositoryCallback.Readers) callback).onReadersRetrieved(readers);
            }
        });
    }

    public List<Reader> retrieveReaders() {
        SQLiteManager database = new SQLiteManager(context);
        List<Reader> readers = database.getReaders();
        database.close();
        return readers;
    }

    public boolean isContactsUpdated() {
        return ContactsManager.isContactsUpdated(context);
    }

}
