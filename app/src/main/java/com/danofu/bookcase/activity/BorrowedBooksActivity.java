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
import com.danofu.bookcase.databaseobject.Reader;
import com.danofu.bookcase.manager.SQLiteManager;

import java.util.List;

import static com.danofu.bookcase.Constants.EXTRA_READER_ID;

public class BorrowedBooksActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed_books);

        LinearLayout contentWrapper = findViewById(R.id.contentWrapper);
        contentWrapper.removeAllViews();
        findViewById(R.id.noBorrowedBooks).setVisibility(View.INVISIBLE);

        String readerId = getIntent().getStringExtra(EXTRA_READER_ID);
        SQLiteManager database = new SQLiteManager(this);
        Reader reader = database.getReader(readerId);

        TextView readerFullName = findViewById(R.id.borrowedBooksReaderFullNameTexView);

        if (reader.getFullName() != null) readerFullName.setText(reader.getFullName());
        else readerFullName.setText("—");

        List<Book> books = database.getBorrowedBooks(readerId);

        if (books.size() != 0)
            for (int i = 0; i < books.size(); i++) {
                boolean isFirst = false;
                if (i == 0)
                    isFirst = true;

                contentWrapper.addView(ViewCreator.
                        createBorrowedBooksView(this, books.get(i), isFirst));
            }
        else
            findViewById(R.id.noBorrowedBooks).setVisibility(View.VISIBLE);

        database.close();
    }

}
