package com.alopezme.hotrodtester.repository.impl;

import com.alopezme.hotrodtester.model.Book;
import com.alopezme.hotrodtester.repository.BookService;
import org.infinispan.client.hotrod.RemoteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.*;

@EnableCaching
@CacheConfig(cacheNames="books")
@Service(value="BookServiceJavaImpl")
public class BookServiceJavaImpl implements BookService {

    @Autowired
    private RemoteCache<Integer, Book> defaultBooksCache;

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
        return defaultBooksCache.keySet().removeAll(keys);
    }

    @Override
    public void deleteAll(){
//        Iterator<Integer> iterator =  defaultBooksCache.keySet().iterator();
//        while (iterator.hasNext()){
//            defaultBooksCache.remove(iterator.next());
//        }
        defaultBooksCache.clear();
        return;
    }

    @Override
    public int getSize(){
        return defaultBooksCache.size();
    }

    @Override
    public String getKeys(){
        return defaultBooksCache.keySet().toString();
    }

    @Override
    public String getValues(){
        return defaultBooksCache.values().toString();
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