package com.danofu.bookcase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.danofu.bookcase.activity.BookBorrowHistoryActivity;
import com.danofu.bookcase.activity.BorrowBookActivity;
import com.danofu.bookcase.activity.BorrowedBooksActivity;
import com.danofu.bookcase.activity.EditBookActivity;
import com.danofu.bookcase.activity.FullScreenImageActivity;
import com.danofu.bookcase.activity.MainActivity;
import com.danofu.bookcase.databaseobject.Book;
import com.danofu.bookcase.databaseobject.Reader;
import com.danofu.bookcase.manager.SQLiteManager;
import com.danofu.bookcase.manager.XMLManager;

import java.io.File;
import java.util.Map;

import static com.danofu.bookcase.Constants.ACTION_EDIT_BOOK;
import static com.danofu.bookcase.Constants.EXTRA_BOOK_ID;
import static com.danofu.bookcase.Constants.EXTRA_IMAGE_FILEPATH;
import static com.danofu.bookcase.Constants.EXTRA_READER_ID;
import static com.danofu.bookcase.Constants.MAP_KEY_BORROW_DATE;
import static com.danofu.bookcase.Constants.MAP_KEY_PLANNED_RETURN_DATE;
import static com.danofu.bookcase.Constants.MAP_KEY_READER_OBJECT;
import static com.danofu.bookcase.Constants.MAP_KEY_RETURN_DATE;

public class ViewCreator {

    // creates book content view
    public static TableLayout createBookContentView(Activity activity,
                                                    Book book, boolean isFirst) {
        Resources resources = activity.getResources();
        int dp5 = convertPixelsToDP(5, activity);
        int dp10 = convertPixelsToDP(10, activity);

        TableLayout bookWrapper = createInfoBookWrapper(activity, book, isFirst);

        // <bookToolsWrapper>
        TableRow bookToolsWrapper = new TableRow(activity);
        TableLayout.LayoutParams bookToolsWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookToolsWrapperLP.setMargins(0, dp10, 0, 0);
        bookToolsWrapper.setLayoutParams(bookToolsWrapperLP);
        bookToolsWrapper.setPadding(0, dp5, 0, dp5);

        // <LinearLayout>
        LinearLayout linearLayout1 = new LinearLayout(activity);
        TableRow.LayoutParams linearLayout1LP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        linearLayout1.setLayoutParams(linearLayout1LP);
        linearLayout1.setOrientation(LinearLayout.HORIZONTAL);

        // <editBookTextView/>
        TextView editBookTV = new TextView(activity);
        LinearLayout.LayoutParams editBookTVLP = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);

        editBookTV.setLayoutParams(editBookTVLP);
        editBookTV.setGravity(Gravity.CENTER);
        editBookTV.setPadding(0, dp5, 0, dp5);
        editBookTV.setText(Html.fromHtml("<u>" +
                resources.getString(R.string.main_edit_book_text_view) + "</u>"));
        editBookTV.setTextColor(ContextCompat.getColor(activity, R.color.text_action));
        editBookTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        editBookTV.setTypeface(editBookTV.getTypeface(), Typeface.BOLD);

        editBookTV.setOnClickListener(view -> {
            Intent intent = new Intent(activity, EditBookActivity.class);

            intent.setAction(ACTION_EDIT_BOOK);
            intent.putExtra(EXTRA_BOOK_ID, book.getId());
            activity.startActivity(intent);
        });

        // <historyBookTextView/>
        TextView historyBookTV = new TextView(activity);
        LinearLayout.LayoutParams historyBookTVLP = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);

        historyBookTV.setLayoutParams(historyBookTVLP);
        historyBookTV.setGravity(Gravity.CENTER);
        historyBookTV.setPadding(0, dp5, 0, dp5);
        historyBookTV.setText(Html.fromHtml("<u>" +
                resources.getString(R.string.main_history_text_view) + "</u>"));
        historyBookTV.setTextColor(ContextCompat.getColor(activity, R.color.text_action));
        historyBookTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        historyBookTV.setTypeface(historyBookTV.getTypeface(), Typeface.BOLD);

        historyBookTV.setOnClickListener(view -> {
            Intent intent = new Intent(activity, BookBorrowHistoryActivity.class);

            intent.putExtra(EXTRA_BOOK_ID, book.getId());
            activity.startActivity(intent);
        });

        // <deleteBookTextView/>
        TextView deleteBookTV = new TextView(activity);
        LinearLayout.LayoutParams deleteBookTVLP = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);

        deleteBookTV.setLayoutParams(deleteBookTVLP);
        deleteBookTV.setGravity(Gravity.CENTER);
        deleteBookTV.setPadding(0, dp5, 0, dp5);
        deleteBookTV.setText(Html.fromHtml("<u>" + resources.
                getString(R.string.main_delete_book_text_view) + "</u>"));
        deleteBookTV.setTextColor(ContextCompat.getColor(activity, R.color.text_action_remove));
        deleteBookTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        deleteBookTV.setTypeface(deleteBookTV.getTypeface(), Typeface.BOLD);

        deleteBookTV.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            String bookName = (book.getName() != null) ? book.getName() : "—";
            builder.setMessage(resources.getString(R.string.main_dialog_message)).
                    setTitle(resources.getString(R.string.main_dialog_title) + ": \"" +
                            bookName + "\"");

            builder.setNegativeButton(R.string.main_dialog_delete, (dialog, which) -> {
                new XMLManager(activity).deleteBook(book.getId());
                new SQLiteManager(activity).deleteBook(book.getId()).close();

                ViewGroup parent = (ViewGroup) bookWrapper.getParent();
                parent.removeView(bookWrapper);

                if (parent.getChildCount() == 0) {
                    TextView noContent = activity.findViewById(R.id.noContent);
                    noContent.post(() -> {
                        noContent.setText(R.string.main_no_content_books);
                        noContent.setVisibility(View.VISIBLE);
                    });
                }

                dialog.cancel();
            });

            builder.setNeutralButton(R.string.main_dialog_cancel,
                    (dialog, which) -> dialog.cancel());

            AlertDialog alterDialog = builder.create();

            alterDialog.setOnShowListener(dialog -> {
                alterDialog.getButton(AlertDialog.BUTTON_NEUTRAL).
                        setTextColor(ContextCompat.getColor(activity, R.color.text_action));
                alterDialog.getButton(AlertDialog.BUTTON_NEGATIVE).
                        setTextColor(ContextCompat.getColor(activity, R.color.text_action_remove));
            });

            alterDialog.show();
        });

        // </LinearLayout>
        linearLayout1.addView(editBookTV);
        linearLayout1.addView(historyBookTV);
        linearLayout1.addView(deleteBookTV);

        // </bookToolsWrapper>
        bookToolsWrapper.addView(linearLayout1);

        // </bookWrapper>
        bookWrapper.addView(bookToolsWrapper);

        return bookWrapper;
    }

    public static TableLayout createInfoBookWrapper(Activity activity, Book book, boolean isFirst) {
        Resources resources = activity.getResources();
        int dp5 = convertPixelsToDP(5, activity);
        int dp6 = convertPixelsToDP(6, activity);
        int dp10 = convertPixelsToDP(10, activity);
        int dp11 = convertPixelsToDP(11, activity);

        // <bookWrapper>
        TableLayout bookWrapper = new TableLayout(activity);
        LinearLayout.LayoutParams bookWrapperLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (!isFirst)
            bookWrapperLP.setMargins(0, dp10, 0, 0);

        bookWrapper.setPadding(dp11, dp11, dp11, dp11);
        bookWrapper.setLayoutParams(bookWrapperLP);
        bookWrapper.setBackground(ContextCompat.getDrawable(activity,
                R.drawable.bg_content_wrapper));

        // <TableRow>
        TableRow tableRow = new TableRow(activity);
        TableLayout.LayoutParams tableRowLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        tableRow.setLayoutParams(tableRowLP);
        tableRow.setPadding(0, dp5, 0, dp5);

        // <bookInfoWrapper>
        TableLayout bookInfoWrapper = new TableLayout(activity);
        TableRow.LayoutParams bookInfoWrapperLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f);

        bookInfoWrapperLP.setMargins(0, 0, dp10, 0);
        bookInfoWrapper.setLayoutParams(bookInfoWrapperLP);

        // <bookNameWrapper>
        TableRow bookNameWrapper = new TableRow(activity);
        TableLayout.LayoutParams bookNameWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookNameWrapperLP.setMargins(0, 0, 0, dp10);
        bookNameWrapper.setLayoutParams(bookNameWrapperLP);

        // <bookNameLabel/>
        TextView bookNameLabel = new TextView(activity);
        TableRow.LayoutParams bookNameLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .7f);
        bookNameLabelLP.gravity = Gravity.CENTER_VERTICAL | Gravity.END;

        bookNameLabelLP.setMargins(0, 0, dp6, 0);
        bookNameLabel.setLayoutParams(bookNameLabelLP);
        bookNameLabel.setGravity(Gravity.END);
        bookNameLabel.setText(R.string.main_book_name_label);
        bookNameLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        bookNameLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bookNameLabel.setTypeface(bookNameLabel.getTypeface(), Typeface.BOLD);

        // <bookName/>
        TextView bookName = new TextView(activity);
        TableRow.LayoutParams bookNameLP = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.3f
        );
        bookNameLP.gravity = Gravity.CENTER_VERTICAL;

        bookName.setLayoutParams(bookNameLP);
        bookName.setTextColor(ContextCompat.getColor(activity, R.color.black));
        bookName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (book.getName() != null)
            bookName.setText(book.getName());
        else
            bookName.setText("—");

        // </bookNameWrapper>
        bookNameWrapper.addView(bookNameLabel);
        bookNameWrapper.addView(bookName);

        // <bookAuthorWrapper>
        TableRow bookAuthorWrapper = new TableRow(activity);
        TableLayout.LayoutParams bookAuthorWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookAuthorWrapperLP.setMargins(0, 0, 0, dp10);
        bookAuthorWrapper.setLayoutParams(bookAuthorWrapperLP);

        // <bookAuthorLabel/>
        TextView bookAuthorLabel = new TextView(activity);
        TableRow.LayoutParams bookAuthorLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .7f);
        bookAuthorLabelLP.gravity = Gravity.CENTER_VERTICAL | Gravity.END;

        bookAuthorLabelLP.setMargins(0, 0, dp6, 0);
        bookAuthorLabel.setLayoutParams(bookAuthorLabelLP);
        bookAuthorLabel.setGravity(Gravity.END);
        bookAuthorLabel.setText(R.string.main_book_author_label);
        bookAuthorLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        bookAuthorLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bookAuthorLabel.setTypeface(bookAuthorLabel.getTypeface(), Typeface.BOLD);

        // <bookAuthor/>
        TextView bookAuthor = new TextView(activity);
        TableRow.LayoutParams bookAuthorLP = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.3f
        );
        bookAuthorLP.gravity = Gravity.CENTER_VERTICAL;

        bookAuthor.setLayoutParams(bookAuthorLP);
        bookAuthor.setTextColor(ContextCompat.getColor(activity, R.color.black));
        bookAuthor.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (book.getAuthor() != null)
            bookAuthor.setText(book.getAuthor());
        else
            bookAuthor.setText("—");

        // </bookAuthorWrapper>
        bookAuthorWrapper.addView(bookAuthorLabel);
        bookAuthorWrapper.addView(bookAuthor);

        // <bookWritingDateWrapper>
        TableRow bookWritingDateWrapper = new TableRow(activity);
        TableLayout.LayoutParams bookWritingDateWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookWritingDateWrapper.setLayoutParams(bookWritingDateWrapperLP);

        // <bookWritingDateLabel/>
        TextView bookWritingDateLabel = new TextView(activity);
        TableRow.LayoutParams bookWritingDateLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .7f);
        bookWritingDateLabelLP.gravity = Gravity.CENTER_VERTICAL | Gravity.END;

        bookWritingDateLabelLP.setMargins(0, 0, dp6, 0);
        bookWritingDateLabel.setLayoutParams(bookWritingDateLabelLP);
        bookWritingDateLabel.setGravity(Gravity.END);
        bookWritingDateLabel.setText(R.string.main_book_writing_date_label);
        bookWritingDateLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        bookWritingDateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bookWritingDateLabel.setTypeface(bookWritingDateLabel.getTypeface(), Typeface.BOLD);

        // <bookWritingDate/>
        TextView bookWritingDate = new TextView(activity);
        TableRow.LayoutParams bookWritingDateLP = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.3f
        );
        bookWritingDateLP.gravity = Gravity.CENTER_VERTICAL;

        bookWritingDate.setLayoutParams(bookWritingDateLP);
        bookWritingDate.setTextColor(ContextCompat.getColor(activity, R.color.black));
        bookWritingDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (book.getWritingDate() != null)
            bookWritingDate.setText(book.getWritingDate());
        else
            bookWritingDate.setText("—");

        // </bookWritingDateWrapper>
        bookWritingDateWrapper.addView(bookWritingDateLabel);
        bookWritingDateWrapper.addView(bookWritingDate);

        // </bookInfoWrapper>
        bookInfoWrapper.addView(bookNameWrapper);
        bookInfoWrapper.addView(bookAuthorWrapper);
        bookInfoWrapper.addView(bookWritingDateWrapper);

        // <bookImage/>
        ImageView bookImage = new ImageView(activity);
        TableRow.LayoutParams bookImageLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, .5f);

        bookImage.setLayoutParams(bookImageLP);
        bookImage.setContentDescription(resources.getString(R.string.main_book_image));
        bookImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (book.getImageLink() != null) {
            File imageFile = new File(activity.getFilesDir(), book.getImageLink());
            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getPath());

            bookImage.setImageBitmap(imageBitmap);
            bookImage.setOnClickListener(view -> {
                Intent intent = new Intent(activity, FullScreenImageActivity.class);
                intent.putExtra(EXTRA_IMAGE_FILEPATH, imageFile.getAbsolutePath());
                activity.startActivity(intent);
            });
        } else
            bookImage.setImageDrawable(ContextCompat.getDrawable(activity,
                    R.drawable.ic_book_image_default));

        // </TableRow>
        tableRow.addView(bookInfoWrapper);
        tableRow.addView(bookImage);

        // <descriptionWrapper>
        TableRow descriptionWrapper = new TableRow(activity);
        TableLayout.LayoutParams descriptionWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        descriptionWrapper.setLayoutParams(descriptionWrapperLP);
        descriptionWrapper.setPadding(0, dp5, 0, dp5);

        // <description/>
        TextView description = new TextView(activity);
        TableRow.LayoutParams descriptionLP = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );

        description.setLayoutParams(descriptionLP);
        description.setEllipsize(TextUtils.TruncateAt.END);
        description.setMaxLines(5);
        description.setTextColor(ContextCompat.getColor(activity, R.color.black));
        description.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (book.getDescription() != null)
            description.setText(book.getDescription());
        else {
            description.setText(R.string.main_no_description);
            description.setTypeface(description.getTypeface(), Typeface.ITALIC);
            description.setGravity(Gravity.CENTER);
        }

        description.setOnClickListener(view -> {
            if (description.getMaxLines() == 5) {
                description.setEllipsize(null);
                description.setMaxLines(Integer.MAX_VALUE);
            } else {
                description.setEllipsize(TextUtils.TruncateAt.END);
                description.setMaxLines(5);
            }
        });

        // </descriptionWrapper>
        descriptionWrapper.addView(description);

        // <bookArticleLinkWrapper>
        TableRow bookArticleLinkWrapper = new TableRow(activity);
        TableLayout.LayoutParams bookArticleLinkWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookArticleLinkWrapper.setLayoutParams(bookArticleLinkWrapperLP);
        bookArticleLinkWrapper.setPadding(0, dp5, 0, dp5);

        // <LinearLayout1>
        LinearLayout linearLayout = new LinearLayout(activity);
        TableRow.LayoutParams linearLayoutLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        linearLayout.setLayoutParams(linearLayoutLP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // <bookArticleLinkLabel/>
        TextView bookArticleLinkLabel = new TextView(activity);
        TableRow.LayoutParams bookArticleLinkLabelLP = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bookArticleLinkLabelLP.gravity = Gravity.CENTER_VERTICAL;

        bookArticleLinkLabelLP.setMargins(0, 0, dp6, 0);
        bookArticleLinkLabel.setLayoutParams(bookArticleLinkLabelLP);
        bookArticleLinkLabel.setEllipsize(TextUtils.TruncateAt.END);
        bookArticleLinkLabel.setSingleLine(true);
        bookArticleLinkLabel.setText(R.string.main_book_article_link_label);
        bookArticleLinkLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        bookArticleLinkLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        bookArticleLinkLabel.setTypeface(bookArticleLinkLabel.getTypeface(), Typeface.BOLD);

        // <bookArticleLink/>
        TextView bookArticleLink = new TextView(activity);
        TableRow.LayoutParams bookArticleLinkLP = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        String articleLink = book.getArticleLink();

        bookArticleLink.setLayoutParams(bookArticleLinkLP);
        bookArticleLink.setTextColor(ContextCompat.getColor(activity, R.color.black));
        bookArticleLink.setLinkTextColor(ContextCompat.getColor(activity, R.color.link_color));
        bookArticleLink.setMovementMethod(LinkMovementMethod.getInstance());
        bookArticleLink.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (book.getArticleLink() != null)
            bookArticleLink.setText(Html.
                    fromHtml("<a href='" + articleLink + "'>" + articleLink + "</a>"));
        else
            bookArticleLink.setText("—");

        // </LinearLayout1>
        linearLayout.addView(bookArticleLinkLabel);
        linearLayout.addView(bookArticleLink);

        // </bookArticleLinkWrapper>
        bookArticleLinkWrapper.addView(linearLayout);

        // </bookWrapper>
        bookWrapper.addView(tableRow);
        bookWrapper.addView(descriptionWrapper);
        bookWrapper.addView(bookArticleLinkWrapper);

        return bookWrapper;
    }

    // creates reader content view
    @SuppressLint("SetTextI18n")
    public static LinearLayout createReaderContentView(MainActivity activity, Reader reader,
                                                       boolean isFirst) {
        Resources resources = activity.getResources();
        int dp5 = convertPixelsToDP(5, activity);
        int dp10 = convertPixelsToDP(10, activity);
        int dp11 = convertPixelsToDP(11, activity);

        // <readerWrapper>
        LinearLayout readerWrapper = new LinearLayout(activity);
        LinearLayout.LayoutParams readerWrapperLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (!isFirst)
            readerWrapperLP.setMargins(0, dp10, 0, 0);

        readerWrapper.setLayoutParams(readerWrapperLP);
        readerWrapper.setBackground(ContextCompat.
                getDrawable(activity, R.drawable.bg_content_wrapper));
        readerWrapper.setOrientation(LinearLayout.VERTICAL);
        readerWrapper.setPadding(dp11, dp11, dp11, dp11);

        // <linearLayout>
        LinearLayout linearLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams linearLayoutLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        linearLayout.setLayoutParams(linearLayoutLP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(0, dp5, 0, dp5);

        // <readerInfoWrapper>
        TableLayout readerInfoWrapper = new TableLayout(activity);
        LinearLayout.LayoutParams readerInfoWrapperLP = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f);

        readerInfoWrapperLP.setMargins(0, 0, dp10, 0);
        readerInfoWrapper.setLayoutParams(readerInfoWrapperLP);

        // <numberWrapper>
        TableRow numberWrapper = new TableRow(activity);
        TableLayout.LayoutParams numberWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        numberWrapperLP.setMargins(0, 0, 0, dp10);
        numberWrapper.setLayoutParams(numberWrapperLP);

        // <numberLabel/>
        TextView numberLabel = new TextView(activity);
        TableRow.LayoutParams numberLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .7f);

        numberLabelLP.gravity = Gravity.CENTER_VERTICAL;
        numberLabelLP.setMargins(0, 0, dp5, 0);
        numberLabel.setLayoutParams(numberLabelLP);
        numberLabel.setGravity(Gravity.END);
        numberLabel.setText(R.string.main_reader_number);
        numberLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        numberLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        numberLabel.setTypeface(numberLabel.getTypeface(), Typeface.BOLD);

        // <numberTextView/>
        TextView numberTV = new TextView(activity);
        TableRow.LayoutParams numberTVLP = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.3f
        );

        numberTVLP.gravity = Gravity.CENTER_VERTICAL;
        numberTV.setLayoutParams(numberTVLP);
        numberTV.setText(Html.fromHtml("<u>" + reader.getId() + "</u>"));
        numberTV.setTextColor(ContextCompat.getColor(activity, R.color.text_action));
        numberTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        numberTV.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !activity.isCallPhonePermissionGranted()) {
                activity.askForCallPhonePermission(reader.getId());
                return;
            }

            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + reader.getId()));
            activity.startActivity(intent);
        });

        // </numberWrapper>
        numberWrapper.addView(numberLabel);
        numberWrapper.addView(numberTV);

        // <fullNameWrapper>
        TableRow fullNameWrapper = new TableRow(activity);
        TableLayout.LayoutParams fullNameWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        fullNameWrapperLP.setMargins(0, 0, 0, dp10);
        fullNameWrapper.setLayoutParams(fullNameWrapperLP);

        // <fullNameLabel/>
        TextView fullNameLabel = new TextView(activity);
        TableRow.LayoutParams fullNameLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .7f);

        fullNameLabelLP.gravity = Gravity.CENTER_VERTICAL;
        fullNameLabelLP.setMargins(0, 0, dp5, 0);
        fullNameLabel.setLayoutParams(fullNameLabelLP);
        fullNameLabel.setGravity(Gravity.END);
        fullNameLabel.setText(R.string.main_reader_full_name);
        fullNameLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        fullNameLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        fullNameLabel.setTypeface(fullNameLabel.getTypeface(), Typeface.BOLD);

        // <fullNameTextView/>
        TextView fullNameTV = new TextView(activity);
        TableRow.LayoutParams fullNameTVLP = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.3f
        );

        fullNameTVLP.gravity = Gravity.CENTER_VERTICAL;
        fullNameTV.setLayoutParams(fullNameTVLP);
        fullNameTV.setTextColor(ContextCompat.getColor(activity, R.color.black));
        fullNameTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (reader.getFullName() != null)
            fullNameTV.setText(reader.getFullName());
        else
            fullNameTV.setText("—");

        // </fullNameWrapper>
        fullNameWrapper.addView(fullNameLabel);
        fullNameWrapper.addView(fullNameTV);

        // <addressWrapper>
        TableRow addressWrapper = new TableRow(activity);
        TableLayout.LayoutParams addressWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addressWrapperLP.setMargins(0, 0, 0, dp10);
        addressWrapper.setLayoutParams(addressWrapperLP);

        // <addressLabel/>
        TextView addressLabel = new TextView(activity);
        TableRow.LayoutParams addressLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .7f);

        addressLabelLP.gravity = Gravity.CENTER_VERTICAL;
        addressLabelLP.setMargins(0, 0, dp5, 0);
        addressLabel.setLayoutParams(addressLabelLP);
        addressLabel.setGravity(Gravity.END);
        addressLabel.setText(R.string.main_reader_address);
        addressLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        addressLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        addressLabel.setTypeface(addressLabel.getTypeface(), Typeface.BOLD);

        // <addressTextView/>
        TextView addressTV = new TextView(activity);
        TableRow.LayoutParams addressTVLP = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.3f
        );

        addressTVLP.gravity = Gravity.CENTER_VERTICAL;
        addressTV.setLayoutParams(addressTVLP);
        addressTV.setTextColor(ContextCompat.getColor(activity, R.color.black));
        addressTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (reader.getAddress() != null)
            addressTV.setText(reader.getAddress());
        else
            addressTV.setText("—");

        // </addressWrapper>
        addressWrapper.addView(addressLabel);
        addressWrapper.addView(addressTV);

        // <emailWrapper>
        TableRow emailWrapper = new TableRow(activity);
        TableLayout.LayoutParams emailWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        emailWrapper.setLayoutParams(emailWrapperLP);

        // <emailLabel/>
        TextView emailLabel = new TextView(activity);
        TableRow.LayoutParams emailLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .7f);

        emailLabelLP.gravity = Gravity.CENTER_VERTICAL;
        emailLabelLP.setMargins(0, 0, dp5, 0);
        emailLabel.setLayoutParams(emailLabelLP);
        emailLabel.setGravity(Gravity.END);
        emailLabel.setText(R.string.main_reader_email);
        emailLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        emailLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        emailLabel.setTypeface(emailLabel.getTypeface(), Typeface.BOLD);

        // <emailTextView/>
        TextView emailTV = new TextView(activity);
        TableRow.LayoutParams emailTVLP = new TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.3f
        );

        emailTVLP.gravity = Gravity.CENTER_VERTICAL;
        emailTV.setLayoutParams(emailTVLP);
        emailTV.setTextColor(ContextCompat.getColor(activity, R.color.black));
        emailTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (reader.getEmail() != null)
            emailTV.setText(reader.getEmail());
        else
            emailTV.setText("—");

        // </emailWrapper>
        emailWrapper.addView(emailLabel);
        emailWrapper.addView(emailTV);

        // </readerInfoWrapper>
        readerInfoWrapper.addView(numberWrapper);
        readerInfoWrapper.addView(fullNameWrapper);
        readerInfoWrapper.addView(addressWrapper);
        readerInfoWrapper.addView(emailWrapper);

        // <readerPhoto/>
        ImageView readerPhoto = new ImageView(activity);
        LinearLayout.LayoutParams readerPhotoLP = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, .5f);

        readerPhoto.setLayoutParams(readerPhotoLP);
        readerPhoto.setBackgroundColor(ContextCompat.getColor(activity, R.color.bg_image));
        readerPhoto.setContentDescription(resources.getString(R.string.main_reader_photo));
        readerPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (reader.getPhotoLink() != null) {
            File photoFile = new File(activity.getFilesDir(), reader.getPhotoLink());
            Bitmap photoBitmap = BitmapFactory.decodeFile(photoFile.getPath());

            readerPhoto.setImageBitmap(photoBitmap);
            readerPhoto.setOnClickListener(view -> {
                Intent intent = new Intent(activity, FullScreenImageActivity.class);
                intent.putExtra(EXTRA_IMAGE_FILEPATH, photoFile.getAbsolutePath());
                activity.startActivity(intent);
            });
        } else {
            readerPhoto.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
            readerPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            readerPhoto.setImageDrawable(ContextCompat.getDrawable(activity,
                    R.drawable.ic_reader_image_default));
        }

        // </linearLayout>
        linearLayout.addView(readerInfoWrapper);
        linearLayout.addView(readerPhoto);

        // <readerToolsWrapper>
        LinearLayout readerToolsWrapper = new LinearLayout(activity);
        LinearLayout.LayoutParams readerToolsWrapperLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        readerToolsWrapperLP.setMargins(0, dp10, 0, 0);
        readerToolsWrapper.setLayoutParams(readerToolsWrapperLP);
        readerToolsWrapper.setBaselineAligned(false);
        readerToolsWrapper.setGravity(Gravity.CENTER_VERTICAL);
        readerToolsWrapper.setOrientation(LinearLayout.HORIZONTAL);
        readerToolsWrapper.setPadding(0, dp5, 0, dp5);

        // <editReaderWrapper>
        FrameLayout editReaderWrapper = new FrameLayout(activity);
        LinearLayout.LayoutParams editReaderWrapperLP = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        editReaderWrapperLP.setMargins(0, 0, dp5, 0);
        editReaderWrapper.setLayoutParams(editReaderWrapperLP);

        // <editReader/>
        TextView editReader = new TextView(activity);
        FrameLayout.LayoutParams editReaderLP = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editReaderLP.gravity = Gravity.CENTER;

        editReader.setLayoutParams(editReaderLP);
        editReader.setGravity(Gravity.CENTER);
        editReader.setPadding(dp5, dp5, dp5, dp5);
        editReader.setText(Html.fromHtml("<u>" +
                resources.getString(R.string.main_reader_edit_text_view) + "</u>"));
        editReader.setTextColor(ContextCompat.getColor(activity, R.color.text_action));
        editReader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editReader.setTypeface(editReader.getTypeface(), Typeface.BOLD);

        editReader.setOnClickListener(v -> {
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
            };
            String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
            String[] selectionArgs = new String[]{reader.getId()};

            Cursor cursor = activity.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                long contactId = cursor.getLong(cursor.
                        getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String lookupKey = cursor.getString(cursor.
                        getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY));
                Uri contactUri = ContactsContract.Contacts.getLookupUri(contactId, lookupKey);

                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setDataAndType(contactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);

                cursor.close();
                activity.startActivity(intent);
            }
        });

        // <editReaderWrapper/>
        editReaderWrapper.addView(editReader);

        // <borrowedBooksWrapper>
        FrameLayout borrowedBooksWrapper = new FrameLayout(activity);
        LinearLayout.LayoutParams borrowedBooksWrapperLP = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        borrowedBooksWrapperLP.setMargins(0, 0, dp5, 0);
        borrowedBooksWrapper.setLayoutParams(borrowedBooksWrapperLP);

        // <borrowedBooks/>
        TextView borrowedBooks = new TextView(activity);
        FrameLayout.LayoutParams borrowedBooksLP = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        borrowedBooksLP.gravity = Gravity.CENTER;

        borrowedBooks.setLayoutParams(borrowedBooksLP);
        borrowedBooks.setGravity(Gravity.CENTER);
        borrowedBooks.setPadding(dp5, dp5, dp5, dp5);
        borrowedBooks.setText(Html.fromHtml("<u>" +
                resources.getString(R.string.main_reader_borrowed_books_text_view) + "</u>"));
        borrowedBooks.setTextColor(ContextCompat.getColor(activity, R.color.text_action));
        borrowedBooks.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        borrowedBooks.setTypeface(borrowedBooks.getTypeface(), Typeface.BOLD);

        borrowedBooks.setOnClickListener(v -> {
            Intent intent = new Intent(activity, BorrowedBooksActivity.class);
            intent.putExtra(EXTRA_READER_ID, reader.getId());
            activity.startActivity(intent);
        });

        // <borrowedBooksWrapper>
        borrowedBooksWrapper.addView(borrowedBooks);

        // <borrowBookWrapper>
        FrameLayout borrowBookWrapper = new FrameLayout(activity);
        LinearLayout.LayoutParams borrowBookWrapperLP = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        borrowBookWrapper.setLayoutParams(borrowBookWrapperLP);

        // <borrowBook/>
        TextView borrowBook = new TextView(activity);
        FrameLayout.LayoutParams borrowBookLP = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        borrowBookLP.gravity = Gravity.CENTER;

        borrowBook.setLayoutParams(borrowBookLP);
        borrowBook.setGravity(Gravity.CENTER);
        borrowBook.setPadding(dp5, dp5, dp5, dp5);
        borrowBook.setText(Html.fromHtml("<u>" +
                resources.getString(R.string.main_reader_borrow_book_text_view) + "</u>"));
        borrowBook.setTextColor(ContextCompat.getColor(activity, R.color.text_action));
        borrowBook.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        borrowBook.setTypeface(borrowBook.getTypeface(), Typeface.BOLD);

        borrowBook.setOnClickListener(v -> {
            Intent intent = new Intent(activity, BorrowBookActivity.class);
            intent.putExtra(EXTRA_READER_ID, reader.getId());
            activity.startActivity(intent);
        });

        // </borrowBookWrapper>
        borrowBookWrapper.addView(borrowBook);

        // </readersToolsWrapper>
        readerToolsWrapper.addView(editReaderWrapper);
        readerToolsWrapper.addView(borrowedBooksWrapper);
        readerToolsWrapper.addView(borrowBookWrapper);

        // </readerWrapper>
        readerWrapper.addView(linearLayout);
        readerWrapper.addView(readerToolsWrapper);

        return readerWrapper;
    }

    // creates borrow book wrapper
    public static TableLayout createBorrowBookView(Activity activity, Book book, Reader reader,
                                                   boolean isFirst) {
        Resources resources = activity.getResources();
        int dp5 = convertPixelsToDP(5, activity);
        int dp10 = convertPixelsToDP(10, activity);

        // <bookWrapper>
        TableLayout bookWrapper = createInfoBookWrapper(activity, book, isFirst);

        // <bookToolsWrapper>
        TableRow bookToolsWrapper = new TableRow(activity);
        TableLayout.LayoutParams bookToolsWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookToolsWrapperLP.setMargins(0, dp10, 0, 0);
        bookToolsWrapper.setLayoutParams(bookToolsWrapperLP);
        bookToolsWrapper.setPadding(0, dp5, 0, dp5);

        // <LinearLayout>
        LinearLayout linearLayout = new LinearLayout(activity);
        TableRow.LayoutParams linearLayoutLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        linearLayout.setLayoutParams(linearLayoutLP);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // <takeBookTextView/>
        TextView takeBookTV = new TextView(activity);
        LinearLayout.LayoutParams takeBookTVLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        takeBookTVLP.gravity = Gravity.CENTER_HORIZONTAL;
        takeBookTV.setLayoutParams(takeBookTVLP);
        takeBookTV.setGravity(Gravity.CENTER);
        takeBookTV.setPadding(dp5, dp5, dp5, dp5);
        takeBookTV.setText(Html.fromHtml("<u>" +
                resources.getString(R.string.borrow_book_take_button) + "</u>"));
        takeBookTV.setTextColor(ContextCompat.getColor(activity, R.color.text_action));
        takeBookTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        takeBookTV.setTypeface(takeBookTV.getTypeface(), Typeface.BOLD);

        takeBookTV.setOnClickListener(v -> {
            new SQLiteManager(activity).borrowBook(book.getId(), reader.getId()).close();
            ViewGroup parent = (ViewGroup) bookWrapper.getParent();
            parent.removeView(bookWrapper);

            if (parent.getChildCount() == 0) {
                TextView noFreeBooks = activity.findViewById(R.id.noFreeBooks);
                noFreeBooks.post(() -> noFreeBooks.setVisibility(View.VISIBLE));
            }

            new Handler(activity.getMainLooper()).post(() -> Toast.makeText(activity,
                    R.string.toast_borrowed_book, Toast.LENGTH_SHORT).show());
        });

        // </LinearLayout>
        linearLayout.addView(takeBookTV);

        // <bookToolsWrapper>
        bookToolsWrapper.addView(linearLayout);

        // </bookWrapper>
        bookWrapper.addView(bookToolsWrapper);

        return bookWrapper;
    }

    // creates borrowed books wrapper
    public static TableLayout createBorrowedBooksView(Activity activity, Book book,
                                                      boolean isFirst) {
        Resources resources = activity.getResources();
        int dp5 = convertPixelsToDP(5, activity);
        int dp10 = convertPixelsToDP(10, activity);

        // <bookWrapper>
        TableLayout bookWrapper = createInfoBookWrapper(activity, book, isFirst);

        // <bookToolsWrapper>
        TableRow bookToolsWrapper = new TableRow(activity);
        TableLayout.LayoutParams bookToolsWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookToolsWrapperLP.setMargins(0, dp10, 0, 0);
        bookToolsWrapper.setLayoutParams(bookToolsWrapperLP);
        bookToolsWrapper.setPadding(0, dp5, 0, dp5);

        // <LinearLayout>
        LinearLayout linearLayout = new LinearLayout(activity);
        TableRow.LayoutParams linearLayoutLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        linearLayout.setLayoutParams(linearLayoutLP);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // <returnBookTextView/>
        TextView returnBookTV = new TextView(activity);
        LinearLayout.LayoutParams returnBookTVLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        returnBookTVLP.gravity = Gravity.CENTER_HORIZONTAL;
        returnBookTV.setLayoutParams(returnBookTVLP);
        returnBookTV.setGravity(Gravity.CENTER);
        returnBookTV.setPadding(dp5, dp5, dp5, dp5);
        returnBookTV.setText(Html.fromHtml("<u>" +
                resources.getString(R.string.borrowed_books_return_button) + "</u>"));
        returnBookTV.setTextColor(ContextCompat.getColor(activity, R.color.text_action_remove));
        returnBookTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        returnBookTV.setTypeface(returnBookTV.getTypeface(), Typeface.BOLD);

        returnBookTV.setOnClickListener(v -> {
            new SQLiteManager(activity).returnBook(book.getId()).close();
            ViewGroup parent = (ViewGroup) bookWrapper.getParent();
            parent.removeView(bookWrapper);

            if (parent.getChildCount() == 0) {
                TextView noFreeBooks = activity.findViewById(R.id.noBorrowedBooks);
                noFreeBooks.post(() -> noFreeBooks.setVisibility(View.VISIBLE));
            }

            new Handler(activity.getMainLooper()).post(() -> Toast.makeText(activity,
                    R.string.toast_book_returned, Toast.LENGTH_SHORT).show());
        });

        // </LinearLayout>
        linearLayout.addView(returnBookTV);

        // <bookToolsWrapper>
        bookToolsWrapper.addView(linearLayout);

        // </bookWrapper>
        bookWrapper.addView(bookToolsWrapper);

        return bookWrapper;
    }

    // creates book borrow history view
    public static TableLayout
    createBookBorrowHistoryView(Activity activity, Map<String, Object> info, boolean isFirst) {
        Reader reader = (Reader) info.get(MAP_KEY_READER_OBJECT);
        int dp5 = convertPixelsToDP(5, activity);
        int dp10 = convertPixelsToDP(10, activity);
        int dp11 = convertPixelsToDP(11, activity);

        // <borrowHistoryWrapper>
        TableLayout borrowHistoryWrapper = new TableLayout(activity);
        LinearLayout.LayoutParams borrowHistoryWrapperLP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        if (!isFirst)
            borrowHistoryWrapperLP.setMargins(0, dp10, 0, 0);
        borrowHistoryWrapper.setLayoutParams(borrowHistoryWrapperLP);
        borrowHistoryWrapper.setBackground(ContextCompat.
                getDrawable(activity, R.drawable.bg_content_wrapper));
        borrowHistoryWrapper.setPadding(dp11, dp11, dp11, dp11);

        // <readerInfoWrapper>
        TableRow readerInfoWrapper = new TableRow(activity);
        TableLayout.LayoutParams readerInfoWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        readerInfoWrapperLP.setMargins(0, 0, 0, dp10);
        readerInfoWrapper.setLayoutParams(readerInfoWrapperLP);

        // <readerLabel/>
        TextView readerLabel = new TextView(activity);
        TableRow.LayoutParams readerLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        readerLabelLP.gravity = Gravity.CENTER_VERTICAL;
        readerLabelLP.setMargins(0, 0, dp5, 0);

        readerLabel.setLayoutParams(readerLabelLP);
        readerLabel.setGravity(Gravity.END);
        readerLabel.setText(R.string.book_borrow_history_reader_label);
        readerLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        readerLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        readerLabel.setTypeface(readerLabel.getTypeface(), Typeface.BOLD);

        // <readerFullNameTextView/>
        TextView readerFullName = new TextView(activity);
        TableRow.LayoutParams readerFullNameLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        readerFullNameLP.gravity = Gravity.CENTER_VERTICAL;

        readerFullName.setLayoutParams(readerFullNameLP);
        readerFullName.setTextColor(ContextCompat.getColor(activity, R.color.text_action));
        readerFullName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        if (reader != null && reader.getFullName() != null)
            readerFullName.setText(Html.fromHtml("<u>" + reader.getFullName() + "</u>"));
        else
            readerFullName.setText(Html.fromHtml("<u>—</u>"));

        readerFullName.setOnClickListener(v -> {
            if (reader != null) {
                Intent intent = new Intent(activity, BorrowedBooksActivity.class);
                intent.putExtra(EXTRA_READER_ID, reader.getId());
                activity.startActivity(intent);
            }
        });

        // </readerInfoWrapper>
        readerInfoWrapper.addView(readerLabel);
        readerInfoWrapper.addView(readerFullName);

        // <returnDateWrapper>
        TableRow returnDateWrapper = new TableRow(activity);
        TableLayout.LayoutParams returnDateWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        returnDateWrapperLP.setMargins(0, 0, 0, dp10);
        returnDateWrapper.setLayoutParams(returnDateWrapperLP);

        // <returnDateLabel/>
        TextView returnDateLabel = new TextView(activity);
        TableRow.LayoutParams returnDateLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        returnDateLabelLP.gravity = Gravity.CENTER_VERTICAL;
        returnDateLabelLP.setMargins(0, 0, dp5, 0);

        returnDateLabel.setLayoutParams(returnDateLabelLP);
        returnDateLabel.setGravity(Gravity.END);
        returnDateLabel.setText(R.string.book_borrow_history_return_date_label);
        returnDateLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        returnDateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        returnDateLabel.setTypeface(returnDateLabel.getTypeface(), Typeface.BOLD);

        // <returnDate/>
        TextView returnDate = new TextView(activity);
        TableRow.LayoutParams returnDateLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        returnDateLP.gravity = Gravity.CENTER_VERTICAL;

        returnDate.setLayoutParams(returnDateLP);
        returnDate.setText(String.valueOf(info.get(MAP_KEY_RETURN_DATE)));
        returnDate.setTextColor(ContextCompat.getColor(activity, R.color.black));
        returnDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        // </returnDateWrapper>
        returnDateWrapper.addView(returnDateLabel);
        returnDateWrapper.addView(returnDate);

        // <borrowDateWrapper>
        TableRow borrowDateWrapper = new TableRow(activity);
        TableLayout.LayoutParams borrowDateWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        borrowDateWrapperLP.setMargins(0, 0, 0, dp10);
        borrowDateWrapper.setLayoutParams(borrowDateWrapperLP);

        // <borrowDateLabel/>
        TextView borrowDateLabel = new TextView(activity);
        TableRow.LayoutParams borrowDateLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        borrowDateLabelLP.gravity = Gravity.CENTER_VERTICAL;
        borrowDateLabelLP.setMargins(0, 0, dp5, 0);

        borrowDateLabel.setLayoutParams(borrowDateLabelLP);
        borrowDateLabel.setGravity(Gravity.END);
        borrowDateLabel.setText(R.string.book_borrow_history_borrow_date_label);
        borrowDateLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        borrowDateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        borrowDateLabel.setTypeface(borrowDateLabel.getTypeface(), Typeface.BOLD);

        // <borrowDate/>
        TextView borrowDate = new TextView(activity);
        TableRow.LayoutParams borrowDateLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        borrowDateLP.gravity = Gravity.CENTER_VERTICAL;

        borrowDate.setLayoutParams(borrowDateLP);
        borrowDate.setText(String.valueOf(info.get(MAP_KEY_BORROW_DATE)));
        borrowDate.setTextColor(ContextCompat.getColor(activity, R.color.black));
        borrowDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        // </borrowDateWrapper>
        borrowDateWrapper.addView(borrowDateLabel);
        borrowDateWrapper.addView(borrowDate);

        // <plannedReturnDateWrapper>
        TableRow plannedReturnDateWrapper = new TableRow(activity);
        TableLayout.LayoutParams plannedReturnDateWrapperLP = new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        plannedReturnDateWrapper.setLayoutParams(plannedReturnDateWrapperLP);

        // <plannedReturnDateLabel/>
        TextView plannedReturnDateLabel = new TextView(activity);
        TableRow.LayoutParams plannedReturnDateLabelLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        plannedReturnDateLabelLP.gravity = Gravity.CENTER_VERTICAL;
        plannedReturnDateLabelLP.setMargins(0, 0, dp5, 0);

        plannedReturnDateLabel.setLayoutParams(plannedReturnDateLabelLP);
        plannedReturnDateLabel.setGravity(Gravity.END);
        plannedReturnDateLabel.setText(R.string.book_borrow_history_planned_return_date_label);
        plannedReturnDateLabel.setTextColor(ContextCompat.getColor(activity, R.color.black));
        plannedReturnDateLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        plannedReturnDateLabel.setTypeface(plannedReturnDateLabel.getTypeface(), Typeface.BOLD);

        // <borrowHistoryPlannedReturnDate/>
        TextView plannedReturnDate = new TextView(activity);
        TableRow.LayoutParams plannedReturnDateLP = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        plannedReturnDateLP.gravity = Gravity.CENTER_VERTICAL;

        plannedReturnDate.setLayoutParams(plannedReturnDateLP);
        plannedReturnDate.setText(String.valueOf(info.get(MAP_KEY_PLANNED_RETURN_DATE)));
        plannedReturnDate.setTextColor(ContextCompat.getColor(activity, R.color.black));
        plannedReturnDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        // </plannedReturnDateWrapper>
        plannedReturnDateWrapper.addView(plannedReturnDateLabel);
        plannedReturnDateWrapper.addView(plannedReturnDate);

        // </borrowHistoryWrapper>
        borrowHistoryWrapper.addView(readerInfoWrapper);
        borrowHistoryWrapper.addView(returnDateWrapper);
        borrowHistoryWrapper.addView(borrowDateWrapper);
        borrowHistoryWrapper.addView(plannedReturnDateWrapper);

        return borrowHistoryWrapper;
    }

    // converts pixels to dp measure
    public static int convertPixelsToDP(int pixels, Context context) {
        float dpScale = context.getResources().getDisplayMetrics().density;
        return (int) (pixels * dpScale + .5f);
    }

}
