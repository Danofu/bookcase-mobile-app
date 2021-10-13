package com.danofu.bookcase;

public class Constants {

    public static final String XML_FILENAME = "books.xml";

    public static final String DATABASE_FILENAME = "bookcase.db";

    public static final String BOOK_STATUS_FREE = "free";
    public static final String BOOK_STATUS_BORROWED = "borrowed";
    public static final String BOOK_IMAGES_FOLDER_NAME = "book-images";

    public static final String READER_PHOTOS_FOLDER_NAME = "reader-photos";

    public static final String EXTRA_IMAGE_FILEPATH = "imageFilepath";
    public static final String EXTRA_BOOK_ID = "bookId";
    public static final String EXTRA_READER_ID = "readerId";

    public static final String ACTION_ADD_BOOK = "addBook";
    public static final String ACTION_EDIT_BOOK = "editBook";

    public static final String SHARED_PREFERENCES_NAME_MAIN_ACTIVITY = "mainActivity";
    public static final String SHARED_PREFERENCES_INTERFACE_STATE_NAME = "interfaceStateName";

    public static final String INTERFACE_STATE_BOOKS = "books";
    public static final String INTERFACE_STATE_READERS = "readers";

    public static final String MAP_KEY_READER_OBJECT = "readerObject";
    public static final String MAP_KEY_RETURN_DATE = "returnDate";
    public static final String MAP_KEY_BORROW_DATE = "borrowDate";
    public static final String MAP_KEY_PLANNED_RETURN_DATE = "plannedReturnDate";

    public static final String NOTIFICATION_CHANNEL_ID = "mainChannel";

    public static final int NOTIFICATION_MAIN_ID = 1;

    public static final int REQUEST_CODE_UPLOAD_IMAGE = 0;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CODE_READ_CONTACTS_PERMISSION = 2;
    public static final int REQUEST_CODE_CALL_PHONE_PERMISSION = 3;

    public static final double LOADING_TIP_TIME_DELAY_IN_SECONDS = .3;

}
