package com.danofu.bookcase.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.danofu.bookcase.databaseobject.Book;
import com.danofu.bookcase.databaseobject.Reader;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.danofu.bookcase.Constants.BOOK_STATUS_BORROWED;
import static com.danofu.bookcase.Constants.BOOK_STATUS_FREE;
import static com.danofu.bookcase.Constants.DATABASE_FILENAME;
import static com.danofu.bookcase.Constants.MAP_KEY_BORROW_DATE;
import static com.danofu.bookcase.Constants.MAP_KEY_PLANNED_RETURN_DATE;
import static com.danofu.bookcase.Constants.MAP_KEY_READER_OBJECT;
import static com.danofu.bookcase.Constants.MAP_KEY_RETURN_DATE;

public class SQLiteManager {

    private static final String TAG = "SQLiteManager";

    private SQLiteDatabase database;
    private Context context;

    public SQLiteManager(Context context) {
        this.context = context;
        database = initDatabase(context);
    }

    // database init
    // opens database and creates tables if they aren't exist
    private SQLiteDatabase initDatabase(Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(DATABASE_FILENAME, 0,
                null);

        // creates books table if not exists
        database.execSQL("CREATE TABLE IF NOT EXISTS books" +
                // book_id is `PPxSS` type; where `P` means position, `S` means number of shelf
                "(book_id TEXT PRIMARY KEY," +
                "book_name TEXT," +
                "author TEXT," +
                "writing_date TEXT," +
                "description TEXT," +
                "article_link TEXT," +
                "status TEXT," +
                "image TEXT);"
        );

        // creates readers table if not exists
        database.execSQL("CREATE TABLE IF NOT EXISTS readers" +
                "(reader_id TEXT PRIMARY KEY," + // reader_id is mobile number of his
                "first_name TEXT," +
                "last_name TEXT," +
                "address TEXT," +
                "email TEXT," +
                "photo_link TEXT);"
        );

        // creates borrow histories table if not exists
        database.execSQL("CREATE TABLE IF NOT EXISTS borrow_histories" +
                "(borrow_history_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "borrow_date DATE," +
                "planned_return_date DATE," +
                "return_date DATE," +
                "book_id TEXT," +
                "reader_id TEXT," +
                "FOREIGN KEY(book_id) REFERENCES books(book_id)," +
                "FOREIGN KEY(reader_id) REFERENCES reader(reader_id));"
        );

        return database;
    }

    // synchronizes new readers with old readers in database
    public void syncReaders(List<Reader> newReaders, List<Reader> oldReaders) {
        for (Reader newReader : newReaders) {
            Reader oldReader = getReader(newReader.getId());

            if (oldReader == null) insertReader(newReader);
            else if (!oldReader.equals(newReader)) {
                if (oldReader.getPhotoLink() != null)
                    new File(context.getFilesDir(), oldReader.getPhotoLink()).delete();
                updateReader(newReader);
            }
        }

        for (Reader oldReader : oldReaders) {
            boolean haveToBeDeleted = true;

            for (Reader newReader : newReaders)
                if (newReader.getId().equals(oldReader.getId())) {
                    haveToBeDeleted = false;
                    break;
                }

            if (haveToBeDeleted) {
                deleteReader(oldReader.getId());
                break;
            }
        }

    }

    // deletes reader
    public void deleteReader(String id) {
        Reader reader = getReader(id);
        if (reader.getPhotoLink() != null)
            new File(context.getFilesDir(), getReader(id).getPhotoLink()).delete();
        database.execSQL("DELETE FROM readers WHERE reader_id = ?", new String[]{id});
    }

    // updates reader
    public void updateReader(Reader reader) {
        String[] values = new String[]{
                reader.getFirstName(),
                reader.getLastName(),
                reader.getAddress(),
                reader.getEmail(),
                reader.getPhotoLink(),
                reader.getId()
        };

        database.execSQL("UPDATE readers SET " +
                "first_name = ?," +
                "last_name = ?," +
                "address = ?," +
                "email = ?," +
                "photo_link = ?" +
                "WHERE reader_id = ?", values);

    }

    // adds reader to database
    public void insertReader(Reader reader) {
        String[] values = new String[]{
                reader.getId(),
                reader.getFirstName(),
                reader.getLastName(),
                reader.getAddress(),
                reader.getEmail(),
                reader.getPhotoLink()
        };


        database.execSQL("INSERT INTO readers VALUES(?, ?, ?, ?, ?, ?)", values);
    }

    // returns reader by given id
    public Reader getReader(String id) {
        Cursor cursor = database.rawQuery("SELECT * FROM readers WHERE reader_id = ?",
                new String[]{id});

        if (cursor != null && cursor.moveToFirst()) {
            String firstName = cursor.getString(cursor.getColumnIndex("first_name"));
            String lastName = cursor.getString(cursor.getColumnIndex("last_name"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            String email = cursor.getString(cursor.getColumnIndex("email"));
            String photoLink = cursor.getString(cursor.getColumnIndex("photo_link"));

            return new Reader(id, firstName, lastName, address, email, photoLink);
        }

        if (cursor != null)
            cursor.close();

        return null;
    }

    // returns list of readers
    public List<Reader> getReaders() {
        List<Reader> readers = new ArrayList<>();
        Cursor cursor = database.rawQuery(
                "SELECT * FROM readers ORDER BY last_name, first_name", null);

        if (cursor != null && cursor.moveToFirst())
            do {
                String id = cursor.getString(cursor.getColumnIndex("reader_id"));
                String firstName = cursor.
                        getString(cursor.getColumnIndex("first_name"));
                String lastName = cursor.getString(cursor.getColumnIndex("last_name"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String email = cursor.getString(cursor.getColumnIndex("email"));
                String photoLink = cursor.
                        getString(cursor.getColumnIndex("photo_link"));

                readers.add(new Reader(id, firstName, lastName, address, email, photoLink));
            } while (cursor.moveToNext());

        if (cursor != null)
            cursor.close();

        return readers;
    }

    // synchronizes database with books in xml file
    // for debugging purposes
    private SQLiteManager syncBooks() {
        XMLManager xmlManager = new XMLManager(context);
        List<Book> newBooks = xmlManager.getBooks();
        List<Book> oldBooks = getBooks();

        for (Book newBook : newBooks) {
            Book oldBook = getBook(newBook.getId());

            if (oldBook == null) insertBook(newBook);
            else if (!oldBook.equals(newBook)) updateBook(newBook);
        }

        for (Book oldBook : oldBooks) {
            boolean haveToBeDeleted = true;

            for (Book newBook : newBooks)
                if (newBook.getId().equals(oldBook.getId())) {
                    haveToBeDeleted = false;
                    break;
                }

            if (haveToBeDeleted)
                deleteBook(oldBook.getId());
        }

        return this;
    }

    // inserts book to database
    public SQLiteManager insertBook(Book book) {
        String[] values = new String[]{
                book.getId(),
                book.getName(),
                book.getAuthor(),
                book.getWritingDate(),
                book.getDescription(),
                book.getArticleLink(),
                book.getStatusStr(),
                book.getImageLink()
        };

        database.execSQL("INSERT INTO books VALUES(?, ?, ?, ?, ?, ?, ?, ?)", values);
        Log.v(TAG, "Book inserted !\nBook Object: '" + book + "'");
        return this;
    }

    // updates book in database
    public SQLiteManager updateBook(Book book) {
        String[] values = new String[]{
                book.getName(),
                book.getAuthor(),
                book.getWritingDate(),
                book.getDescription(),
                book.getArticleLink(),
                book.getStatusStr(),
                book.getImageLink(),
                book.getId()
        };

        database.execSQL("UPDATE books SET " +
                "book_name = ?," +
                "author = ?," +
                "writing_date = ?," +
                "description = ?," +
                "article_link = ?," +
                "status = ?," +
                "image = ?" +
                "WHERE book_id = ?", values);

        return this;
    }

    // deletes book from database
    public SQLiteManager deleteBook(String id) {
        Book book = getBook(id);
        if (book.getImageLink() != null)
            new File(context.getFilesDir(), getBook(id).getImageLink()).delete();

        database.execSQL("DELETE FROM books WHERE book_id = ?", new String[]{id});
        deleteBookHistory(id);
        return this;
    }

    public void deleteBookHistory(String id) {
        database.execSQL("DELETE FROM borrow_histories WHERE book_id = ?", new String[]{id});
    }

    // borrows book
    public SQLiteManager borrowBook(String bookId, String readerId) {
        long monthsInMillis = 2592000000L;
        long currentTimeMillis = System.currentTimeMillis();
        Date borrowDate = new Date(currentTimeMillis);
        Date plannedReturnDate = new Date(currentTimeMillis + 2 * monthsInMillis);

        ContentValues contentValues = new ContentValues();
        contentValues.put("book_id", bookId);
        contentValues.put("reader_id", readerId);
        contentValues.put("borrow_date", borrowDate.toString());
        contentValues.put("planned_return_date", plannedReturnDate.toString());

        database.insert("borrow_histories", null, contentValues);
        database.execSQL("UPDATE books SET status = ? WHERE book_id = ?",
                new String[]{BOOK_STATUS_BORROWED, bookId});
        return this;
    }

    // does thing that reader returns book
    public SQLiteManager returnBook(String bookId) {
        Date returnDate = new Date(System.currentTimeMillis());

        database.execSQL("UPDATE borrow_histories SET return_date = ? " +
                        "WHERE book_id = ? AND return_date IS NULL",
                new String[]{returnDate.toString(), bookId});
        database.execSQL("UPDATE books SET status = ? WHERE book_id = ?",
                new String[]{BOOK_STATUS_FREE, bookId});
        return this;
    }


    // returns list of books from database with given status
    public List<Book> getBooksWhere(Book.BookStatus status) {
        List<Book> books = new ArrayList<>();
        String bookStatus = null;
        switch (status) {
            case FREE:
                bookStatus = BOOK_STATUS_FREE;
                break;
            case BORROWED:
                bookStatus = BOOK_STATUS_BORROWED;
                break;
        }

        Cursor cursor = database.rawQuery("SELECT * FROM books " +
                "WHERE status = ? ORDER BY book_id", new String[]{bookStatus});

        if (cursor != null && cursor.moveToFirst())
            do {
                String id = cursor.getString(cursor.getColumnIndex("book_id")),
                        name = cursor.getString(cursor.getColumnIndex("book_name")),
                        author = cursor.getString(cursor.getColumnIndex("author")),
                        writingDate = cursor.
                                getString(cursor.getColumnIndex("writing_date")),
                        description = cursor.
                                getString(cursor.getColumnIndex("description")),
                        articleLink = cursor.
                                getString(cursor.getColumnIndex("article_link")),
                        imageLink = cursor.getString(cursor.getColumnIndex("image"));
                books.add(new Book(id, name, author, writingDate, description, articleLink, status,
                        imageLink));
            } while (cursor.moveToNext());

        if (cursor != null)
            cursor.close();

        return books;
    }

    public List<Book> getBorrowedBooks(String readerId) {
        List<Book> books = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT b.book_id, book_name, author, " +
                        "writing_date, description, article_link, image " +
                        "FROM borrow_histories bh " +
                        "INNER JOIN books b " +
                        "ON b.book_id = bh.book_id " +
                        "INNER JOIN readers r " +
                        "ON r.reader_id = bh.reader_id " +
                        "WHERE r.reader_id = ? AND return_date IS NULL",
                new String[]{readerId});

        if (cursor != null && cursor.moveToFirst())
            do {
                String id = cursor.getString(cursor.getColumnIndex("book_id")),
                        name = cursor.getString(cursor.getColumnIndex("book_name")),
                        author = cursor.getString(cursor.getColumnIndex("author")),
                        writingDate = cursor.
                                getString(cursor.getColumnIndex("writing_date")),
                        description = cursor.
                                getString(cursor.getColumnIndex("description")),
                        articleLink = cursor.
                                getString(cursor.getColumnIndex("article_link")),
                        imageLink = cursor.getString(cursor.getColumnIndex("image"));

                books.add(new Book(id, name, author, writingDate, description, articleLink,
                        Book.BookStatus.BORROWED, imageLink));
            } while (cursor.moveToNext());

        if (cursor != null)
            cursor.close();

        return books;
    }

    // returns list of books from database
    public List<Book> getBooks() {
        List<Book> books = new ArrayList<>();
        Cursor cursor = database.rawQuery(
                "SELECT * FROM books ORDER BY book_id", null);

        if (cursor != null && cursor.moveToFirst())
            do {
                String id = cursor.getString(cursor.getColumnIndex("book_id")),
                        name = cursor.getString(cursor.getColumnIndex("book_name")),
                        author = cursor.getString(cursor.getColumnIndex("author")),
                        writingDate = cursor.
                                getString(cursor.getColumnIndex("writing_date")),
                        description = cursor.
                                getString(cursor.getColumnIndex("description")),
                        articleLink = cursor.
                                getString(cursor.getColumnIndex("article_link")),
                        status = cursor.getString(cursor.getColumnIndex("status")),
                        imageLink = cursor.getString(cursor.getColumnIndex("image"));
                Book.BookStatus bookStatus = null;

                switch (status) {
                    case BOOK_STATUS_FREE:
                        bookStatus = Book.BookStatus.FREE;
                        break;
                    case BOOK_STATUS_BORROWED:
                        bookStatus = Book.BookStatus.BORROWED;
                        break;
                }

                books.add(new Book(id, name, author, writingDate, description, articleLink,
                        bookStatus, imageLink));
            } while (cursor.moveToNext());

        if (cursor != null)
            cursor.close();

        return books;
    }

    // returns book by given id
    public Book getBook(String id) {
        Cursor cursor = database.rawQuery("SELECT * FROM books WHERE book_id = ?",
                new String[]{id});

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex("book_name")),
                    author = cursor.getString(cursor.getColumnIndex("author")),
                    writingDate = cursor.
                            getString(cursor.getColumnIndex("writing_date")),
                    description = cursor.
                            getString(cursor.getColumnIndex("description")),
                    articleLink = cursor.
                            getString(cursor.getColumnIndex("article_link")),
                    status = cursor.getString(cursor.getColumnIndex("status")),
                    imageLink = cursor.getString(cursor.getColumnIndex("image"));
            Book.BookStatus bookStatus = null;

            switch (status) {
                case BOOK_STATUS_FREE:
                    bookStatus = Book.BookStatus.FREE;
                    break;
                case BOOK_STATUS_BORROWED:
                    bookStatus = Book.BookStatus.BORROWED;
                    break;
            }

            cursor.close();
            return new Book(id, name, author, writingDate, description, articleLink, bookStatus,
                    imageLink);
        }

        return null;
    }

    // returns info for book history activity
    public List<Map<String, Object>> getBookHistoryInfo(String bookId) {
        List<Map<String, Object>> information = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT r.reader_id, borrow_date, return_date, " +
                        "planned_return_date " +
                        "FROM borrow_histories bh " +
                        "INNER JOIN readers r " +
                        "ON r.reader_id = bh.reader_id " +
                        "INNER JOIN books b " +
                        "ON b.book_id = bh.book_id " +
                        "WHERE b.book_id = ? " +
                        "ORDER BY return_date, r.reader_id",
                new String[]{bookId});

        if (cursor != null && cursor.moveToFirst())
            do {
                Map<String, Object> info = new HashMap<>();
                String readerId = cursor.getString(cursor.getColumnIndex("reader_id"));
                info.put(MAP_KEY_READER_OBJECT, getReader(readerId));

                String returnDate = cursor.
                        getString(cursor.getColumnIndex("return_date"));
                if (returnDate == null)
                    returnDate = "—";
                info.put(MAP_KEY_RETURN_DATE, returnDate);

                String borrowDate = cursor.
                        getString(cursor.getColumnIndex("borrow_date"));
                info.put(MAP_KEY_BORROW_DATE, borrowDate);

                String plannedReturnDate = cursor.
                        getString(cursor.getColumnIndex("planned_return_date"));
                info.put(MAP_KEY_PLANNED_RETURN_DATE, plannedReturnDate);

                information.add(info);
            } while (cursor.moveToNext());

        if (cursor != null)
            cursor.close();

        return information;
    }

    // checks if book exists in database by given id
    public boolean isBookExists(String id) {
        Cursor cursor = database.rawQuery("SELECT book_id FROM books WHERE book_id = ?",
                new String[]{id});

        if (cursor != null && cursor.moveToFirst())
            if (cursor.getString(cursor.getColumnIndex("book_id")).equals(id)) {
                cursor.close();
                return true;
            }

        return false;
    }

    public void close() {
        database.close();
    }

}
