package com.alopezme.hotrodtester.repository.impl;

import com.alopezme.hotrodtester.configuration.CacheNames;
import com.alopezme.hotrodtester.model.Book;
import com.alopezme.hotrodtester.repository.BookService;
import org.infinispan.client.hotrod.RemoteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.*;

@EnableCaching
@CacheConfig(cacheNames = CacheNames.BOOKS_CACHE_NAME)
@Service(value="BookServiceJavaImpl")
public class BookServiceJavaImpl implements BookService {

    @Autowired
    @Qualifier("serializationBooksCache")
    private RemoteCache<Integer, Book> booksCache;

    Logger logger = LoggerFactory.getLogger(BookServiceJavaImpl.class);

    @Cacheable(key="#id")
    @Override
    public Book findById(int id){
        return null;
    }

    @CachePut(key="#id")
    @Override
    public Book insert(int id, Book book){
        return book;
    }

    @CacheEvict(key="#id")
    @Override
    public void delete(int id){
    }

    @Override
    public boolean bulkRemove(Set<Integer> keys){
        return booksCache.keySet().removeAll(keys);
    }

    @Override
    public void deleteAll(){
        booksCache.clear();
    }

    @Override
    public int getSize(){
        return booksCache.size();
    }

    @Override
    public String getKeys(){
        return booksCache.keySet().toString();
    }

    @Override
    public String getValues(){
        return booksCache.values().toString();
    }

    @Override
    public List<Book> query(String query){
        // Method not supported for Java Serialization implementation
        return new ArrayList<Book>();
    }

    @Override
    public List<Object[]> queryObject(String query){
        // Method not supported for Java Serialization implementation
        return new ArrayList<Object[]>();
    }
}