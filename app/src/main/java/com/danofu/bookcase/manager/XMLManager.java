package com.danofu.bookcase.manager;

import android.content.Context;

import com.danofu.bookcase.databaseobject.Book;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.danofu.bookcase.Constants.BOOK_STATUS_BORROWED;
import static com.danofu.bookcase.Constants.BOOK_STATUS_FREE;
import static com.danofu.bookcase.Constants.XML_FILENAME;

public class XMLManager {

    private final String xmlFilepath;

    private Document document;

    public XMLManager(Context context) {
        xmlFilepath = context.getFilesDir() + "/" + XML_FILENAME;
        document = getDocument();
    }

    // returns list of books
    public List<Book> getBooks() {
        List<Book> books = new ArrayList<>();

        if (document == null)
            return books;

        NodeList bookNodes = document.getDocumentElement().getChildNodes();

        for (int i = 0; i < bookNodes.getLength(); i++) {
            Node book = bookNodes.item(i);

            if (book.getNodeType() != Node.TEXT_NODE)
                books.add(getBook(book));
        }

        return books;
    }

    // returns Book Object by given book Node
    private Book getBook(Node bookNode) {
        Book book = new Book();

        String id = bookNode.getAttributes().getNamedItem("id").getNodeValue();
        book.setId(id);

        NodeList bookParams = bookNode.getChildNodes();
        for (int i = 0; i < bookParams.getLength(); i++) {
            Node bookParam = bookParams.item(i);

            if (bookParam.getNodeType() != Node.TEXT_NODE) {
                String nodeValue = bookParam.getChildNodes().item(0).getTextContent();

                switch (bookParam.getNodeName()) {
                    case "name":
                        book.setName(nodeValue);
                        break;
                    case "author":
                        book.setAuthor(nodeValue);
                        break;
                    case "writing-date":
                        book.setWritingDate(nodeValue);
                        break;
                    case "article-link":
                        book.setArticleLink(nodeValue);
                        break;
                    case "description":
                        book.setDescription(nodeValue);
                        break;
                    case "status":
                        Book.BookStatus status = null;

                        switch (nodeValue) {
                            case BOOK_STATUS_FREE:
                                status = Book.BookStatus.FREE;
                                break;
                            case BOOK_STATUS_BORROWED:
                                status = Book.BookStatus.BORROWED;
                                break;
                        }

                        book.setStatus(status);
                        break;
                    case "image-link":
                        book.setImageLink(nodeValue);
                        break;
                }
            }
        }

        return book;
    }

    // updates existing book in xml file
    public void updateBook(Book book) {
        if (document == null)
            return;

        Element bookElement = document.getElementById(book.getId());
        NodeList bookNodes = bookElement.getChildNodes();

        for (int i = 0; i < bookNodes.getLength(); i++) {
            Node bookNode = bookNodes.item(i);

            if (bookNode.getNodeType() != Node.TEXT_NODE)
                switch (bookNode.getNodeName()) {
                    case "name":
                        bookNode.setTextContent(book.getName());
                        break;
                    case "author":
                        bookNode.setTextContent(book.getAuthor());
                        break;
                    case "writing-date":
                        bookNode.setTextContent(book.getWritingDate());
                        break;
                    case "article-link":
                        bookNode.setTextContent(book.getArticleLink());
                        break;
                    case "description":
                        bookNode.setTextContent(book.getDescription());
                        break;
                    case "status":
                        bookNode.setTextContent(book.getStatusStr());
                        break;
                    case "image-uri":
                        bookNode.setTextContent(book.getImageLink());
                        break;
                }
        }

        updateDocument();
    }

    // saves book to xml file
    public void saveBook(Book book) {
        if (document == null)
            return;

        // <book>
        Element bookElement = document.createElement("book");

        bookElement.setAttribute("id", book.getId());

        // <name></name>
        Element name = document.createElement("name");

        name.setTextContent(book.getName());

        // <author></author>
        Element author = document.createElement("author");

        author.setTextContent(book.getAuthor());

        // <writing-date></writing-date>
        Element writingDate = document.createElement("writing-date");

        writingDate.setTextContent(book.getWritingDate());

        // <article-link></article-link>
        Element articleLink = document.createElement("article-link");

        articleLink.setTextContent(book.getArticleLink());

        // <description></description>
        Element description = document.createElement("description");

        description.setTextContent(book.getDescription());

        // <status></status>
        Element status = document.createElement("status");

        status.setTextContent(book.getStatusStr());

        // <image-link></image-link>
        Element imageUri = document.createElement("image-link");

        imageUri.setTextContent(book.getImageLink());

        // </book>
        bookElement.appendChild(name);
        bookElement.appendChild(author);
        bookElement.appendChild(writingDate);
        bookElement.appendChild(articleLink);
        bookElement.appendChild(description);
        bookElement.appendChild(status);
        bookElement.appendChild(imageUri);
        document.getDocumentElement().appendChild(bookElement);
        updateDocument();
    }

    // deletes book from xml file by given id
    public void deleteBook(String id) {
        Element bookNode = document.getElementById(id);
        if (bookNode != null) {
            bookNode.getParentNode().removeChild(bookNode);
            updateDocument();
        }
    }

    // creates new document and saves it in xml file
    private void createDocument() {
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = document.createElement("books");

            document.appendChild(root);
            updateDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    // returns document from xml file
    private Document getDocument() {
        File xmlFile = new File(xmlFilepath);

        if (!xmlFile.exists())
            createDocument();

        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    // updates document in xml file or creates file if on exists and saves document
    private void updateDocument() {
        document.normalize();

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(xmlFilepath);

            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

}
