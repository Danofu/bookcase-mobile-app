package com.danofu.bookcase.activity;

import android.annotation.SuppressLint;
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

public class BorrowBookActivity extends Activity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_book);

        LinearLayout contentWrapper = findViewById(R.id.contentWrapper);
        contentWrapper.removeAllViews();
        findViewById(R.id.noFreeBooks).setVisibility(View.INVISIBLE);

        String readerId = getIntent().getStringExtra(EXTRA_READER_ID);
        SQLiteManager database = new SQLiteManager(this);
        Reader reader = database.getReader(readerId);

        TextView readerFullName = findViewById(R.id.borrowBookReaderFullNameTexView);

        if (reader.getFullName() != null) readerFullName.setText(reader.getFullName());
        else readerFullName.setText("—");

        List<Book> books = database.getBooksWhere(Book.BookStatus.FREE);

        if (books.size() != 0)
            for (int i = 0; i < books.size(); i++) {
                boolean isFirst = false;
                if (i == 0)
                    isFirst = true;

                contentWrapper.addView(ViewCreator.
                        createBorrowBookView(this, books.get(i), reader, isFirst));
            }
        else
            findViewById(R.id.noFreeBooks).setVisibility(View.VISIBLE);

        database.close();
    }

}
