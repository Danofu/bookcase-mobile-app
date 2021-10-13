package com.danofu.bookcase.databaseobject;

import androidx.annotation.NonNull;

import static com.danofu.bookcase.Constants.BOOK_STATUS_BORROWED;
import static com.danofu.bookcase.Constants.BOOK_STATUS_FREE;

public class Book {

    private String id, name, author, writingDate, description, articleLink, imageLink;
    private BookStatus status;

    public enum BookStatus {
        FREE, BORROWED
    }

    public Book() {
    }

    public Book(String id, String name, String author, String writingDate, String description,
                String articleLink, BookStatus status, String imageLink) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.writingDate = writingDate;
        this.description = description;
        this.articleLink = articleLink;
        this.status = status;
        this.imageLink = imageLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;

        return compareFields(name, book.name) &&
                compareFields(author, book.author) &&
                compareFields(writingDate, book.writingDate) &&
                compareFields(description, book.description) &&
                compareFields(articleLink, book.articleLink) &&
                compareFields(status, book.status) &&
                compareFields(imageLink, book.imageLink);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getWritingDate() {
        return writingDate;
    }

    public void setWritingDate(String writingDate) {
        this.writingDate = writingDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArticleLink() {
        return articleLink;
    }

    public void setArticleLink(String articleLink) {
        this.articleLink = articleLink;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public BookStatus getStatus() {
        return status;
    }

    public String getStatusStr() {
        switch (status) {
            case FREE:
                return BOOK_STATUS_FREE;
            case BORROWED:
                return BOOK_STATUS_BORROWED;
            default:
                return null;
        }
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    private boolean compareFields(String field1, String field2) {
        if (field1 == null) return field2 == null;
        else return field1.equals(field2);
    }

    private boolean compareFields(BookStatus field1, BookStatus field2) {
        if (field1 == null) return field2 == null;
        else return field1.equals(field2);
    }

    @NonNull
    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", writingDate='" + writingDate + '\'' +
                ", description='" + description + '\'' +
                ", articleLink='" + articleLink + '\'' +
                ", status='" + status + '\'' +
                ", imageLink='" + imageLink + '\'' +
                '}';
    }

}
