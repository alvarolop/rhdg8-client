package org.alopezme.example.springtester.model;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoDoc("@Indexed")
public class Book {

    @ProtoDoc("@Field(index=Index.NO, store = Store.NO, analyze = Analyze.NO)")
    @ProtoField(number = 1, defaultValue = "0")
    public int id;
    @ProtoDoc("@Field(index=Index.YES, store = Store.YES, analyze = Analyze.NO)")
    @ProtoField(number = 2)
    public String title;
    @ProtoDoc("@Field(index=Index.YES, store = Store.YES, analyze = Analyze.YES)")
    @ProtoField(number = 3)
    public String author;
    @ProtoDoc("@Field(index=Index.NO, store = Store.NO, analyze = Analyze.NO)")
    @ProtoField(number = 4, defaultValue = "0")
    public int publicationYear;

    @ProtoFactory
    public Book(int id, String title, String author, int publicationYear) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("{");
        b.append("\"_type\":\"" + "org.alopezme.example.springtester.model.Book" + "\",");
        b.append("\"id\":\"").append(Integer.toString(id)).append("\",");
        b.append("\"title\":\"").append(title).append("\",");
        b.append("\"author\":\"").append(author).append("\",");
        b.append("\"publicationYear\":\"").append(Integer.toString(publicationYear)).append("\"");
        b.append("}"); // Close Book
        return b.toString();
    }

    public String toTextString() {
        return "Book " + id + ": " + title + " written by " + author + " and published in " + Integer.toString(publicationYear);
    }
}