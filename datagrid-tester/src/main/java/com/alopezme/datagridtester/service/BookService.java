package com.alopezme.datagridtester.service;

import java.util.List;

import com.alopezme.datagridtester.model.Book;

public interface BookService {

    public List<Book> getBooks();

    public Book save(Book book);
    
}
