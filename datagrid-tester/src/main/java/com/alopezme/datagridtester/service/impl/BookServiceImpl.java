package com.alopezme.datagridtester.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.alopezme.datagridtester.model.Book;
import com.alopezme.datagridtester.service.BookService;

import org.infinispan.client.hotrod.RemoteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    @Autowired
    RemoteCache<Integer,Book> remoteBookCache;
    
    @Override
    public List<Book> getBooks() {

        return remoteBookCache.values().stream().collect(Collectors.toList());
    }

    @Override
    public Book save(Book book) {
        
        return remoteBookCache.put(book.getId(), book);
    }
    
}
