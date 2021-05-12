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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EnableCaching
@CacheConfig(cacheNames = CacheNames.PROTO_CACHE_NAME)
@Service(value = "BookServiceProtoImpl")
public class BookServiceProtoImpl implements BookService {

    @Autowired
    @Qualifier("protostreamBooksCache")
    private RemoteCache<Integer, Book> booksCache;

    Logger logger = LoggerFactory.getLogger(BookServiceProtoImpl.class);

    @Override
    public Book findById(int id){
        return booksCache.get(id);
    }

    @Override
    public Book insert(int id, Book book){
        return booksCache.put(id, book);
    }

    @Override
    public void delete(int id){
        booksCache.remove(id);
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