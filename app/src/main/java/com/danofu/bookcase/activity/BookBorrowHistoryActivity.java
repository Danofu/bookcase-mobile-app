package com.danofu.bookcase.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.danofu.bookcase.R;
import com.danofu.bookcase.ViewCreator;
import com.danofu.bookcase.databaseobject.Book;
import com.danofu.bookcase.manager.SQLiteManager;

import java.util.List;
import java.util.Map;

import static com.danofu.bookcase.Constants.EXTRA_BOOK_ID;

public class BookBorrowHistoryActivity extends Activity {

    private static final String TAG = "BookBorrowHistoryActivity";

    private String bookId;

    private LinearLayout borrowHistoryContentWrapper;
    private TextView noBookHistory;

    @Override
    protected void onResume() {
        super.onResume();

        bookId = getIntent().getStringExtra(EXTRA_BOOK_ID);
        borrowHistoryContentWrapper = findViewById(R.id.borrowHistoryContentWrapper);
        borrowHistoryContentWrapper.removeAllViews();
        noBookHistory = findViewById(R.id.noBookHistory);
        noBookHistory.setVisibility(View.INVISIBLE);

        insertHistory();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_borrow_history);
    }

    // inserts book history
    private void insertHistory() {
        SQLiteManager database = new SQLiteManager(this);
        List<Map<String, Object>> info = database.getBookHistoryInfo(bookId);
        Book book = database.getBook(bookId);

        String position = bookId.split("x")[0];
        String shelf = bookId.split("x")[1];
        ((TextView) findViewById(R.id.bookPosition)).setText(position);
        ((TextView) findViewById(R.id.bookShelf)).setText(shelf);
        TextView bookHistoryBookName = findViewById(R.id.bookHistoryBookName);
        if (book.getName() != null) bookHistoryBookName.setText(book.getName());
        else bookHistoryBookName.setText("—");
        TextView bookStatus = findViewById(R.id.bookStatus);
        switch (book.getStatus()) {
            case FREE:
                bookStatus.setText(R.string.book_borrow_history_status_free);
                break;
            case BORROWED:
                bookStatus.setText(R.string.book_borrow_history_status_borrowed);
                break;
            default:
                bookStatus.setText("—");
        }

        if (info.size() != 0)
            for (int i = 0; i < info.size(); i++) {
                boolean isFirst = false;
                if (i == 0) isFirst = true;
                borrowHistoryContentWrapper.addView(ViewCreator.
                        createBookBorrowHistoryView(this, info.get(i), isFirst));
            }
        else
            noBookHistory.setVisibility(View.VISIBLE);

        database.close();
    }

}
