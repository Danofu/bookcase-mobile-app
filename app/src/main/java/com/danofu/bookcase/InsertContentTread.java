package com.danofu.bookcase;

import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.danofu.bookcase.activity.MainActivity;
import com.danofu.bookcase.contacts.ContactsRepository;
import com.danofu.bookcase.databaseobject.Book;
import com.danofu.bookcase.databaseobject.Reader;
import com.danofu.bookcase.manager.SQLiteManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.danofu.bookcase.Constants.INTERFACE_STATE_BOOKS;
import static com.danofu.bookcase.Constants.INTERFACE_STATE_READERS;
import static com.danofu.bookcase.Constants.LOADING_TIP_TIME_DELAY_IN_SECONDS;

public class InsertContentTread implements Runnable {

    private Thread worker;
    private Handler handler;
    private MainActivity activity;
    private String interfaceState;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    public InsertContentTread(MainActivity activity, String interfaceState) {
        this.activity = activity;
        this.interfaceState = interfaceState;
        handler = new Handler(activity.getMainLooper());
    }

    public void start() {
        worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        isRunning.set(false);
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public void run() {
        isRunning.set(true);
        handler.post(() -> activity.removeOldContent());
        handler.postDelayed(activity.showLoadingTip,
                (long) (LOADING_TIP_TIME_DELAY_IN_SECONDS * 1000));

        SQLiteManager database = new SQLiteManager(activity);
        switch (interfaceState) {
            case INTERFACE_STATE_BOOKS:
                insertBooks(database.getBooks());

                handler.removeCallbacks(activity.showLoadingTip);
                handler.post(() -> activity.loadingTip.setVisibility(View.GONE));
                break;
            case INTERFACE_STATE_READERS:
                insertReaders(database.getReaders());

                handler.removeCallbacks(activity.showLoadingTip);
                handler.post(() -> activity.loadingTip.setVisibility(View.GONE));

                syncReadersIfUpdated();
                break;
        }

        database.close();
    }

    private void insertBooks(List<Book> books) {
        if (isRunning.get()) {
            handler.post(() -> activity.removeOldContent());
            if (books.size() != 0)
                for (int i = 0; i < books.size(); i++) {
                    Book book = books.get(i);
                    if (i != 0)
                        handler.post(() -> activity.contentWrapper.addView(ViewCreator.
                                createBookContentView(activity, book, false)));
                    else
                        handler.post(() -> activity.contentWrapper.addView(ViewCreator.
                                createBookContentView(activity, book, true)));
                }
            else
                handler.post(() -> {
                    activity.noContent.setText(R.string.main_no_content_books);
                    activity.noContent.setVisibility(View.VISIBLE);
                });
        }
        isRunning.set(false);
    }

    private void insertReaders(List<Reader> readers) {
        if (isRunning.get()) {
            handler.post(() -> activity.removeOldContent());
            if (readers.size() != 0)
                for (int i = 0; i < readers.size(); i++) {
                    Reader reader = readers.get(i);
                    if (i != 0)
                        handler.post(() -> activity.contentWrapper.addView(ViewCreator.
                                createReaderContentView(activity, reader, false)));
                    else
                        handler.post(() -> activity.contentWrapper.addView(ViewCreator.
                                createReaderContentView(activity, reader, true)));
                }
            else
                handler.post(() -> {
                    activity.noContent.setText(R.string.main_no_content_readers);
                    activity.noContent.setVisibility(View.VISIBLE);
                });
        }

        handler.removeCallbacks(activity.showLoadingTip);
        handler.post(() -> activity.loadingTip.setVisibility(View.GONE));
    }

    private void syncReadersIfUpdated() {
        ContactsRepository contactsRepository = new ContactsRepository(activity, activity.executor);

        ContactsRepository.ContactsRepositoryCallback.Readers onReadersRetrieved = readers -> {
            if (activity.getCurrentInterface().equals(INTERFACE_STATE_READERS)) {
                isRunning.set(true);
                handler.postDelayed(activity.showLoadingTip,
                        (long) (LOADING_TIP_TIME_DELAY_IN_SECONDS * 1000));
                insertReaders(readers);
            }
        };
        ContactsRepository.ContactsRepositoryCallback.Contacts onContactsUpdate = updated -> {
            if (updated)
                contactsRepository.syncContacts(onReadersRetrieved);
            isRunning.set(false);
        };

        contactsRepository.syncContacts(onContactsUpdate);
    }

}
