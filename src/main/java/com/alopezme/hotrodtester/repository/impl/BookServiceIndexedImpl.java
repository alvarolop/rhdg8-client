package com.alopezme.hotrodtester.repository.impl;

import com.alopezme.hotrodtester.configuration.CacheNames;
import com.alopezme.hotrodtester.model.Book;
import com.alopezme.hotrodtester.repository.BookService;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@EnableCaching
@CacheConfig(cacheNames = CacheNames.INDEXED_CACHE_NAME)
@Service(value="BookServiceIndexedImpl")
public class BookServiceIndexedImpl implements BookService {

    @Autowired
    @Qualifier("indexedBooksCache")
    private RemoteCache<Integer, Book> booksCache;

    Logger logger = LoggerFactory.getLogger(BookServiceIndexedImpl.class);

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
        QueryFactory queryFactory = Search.getQueryFactory(booksCache);
        Query<Book> myQuery = queryFactory.create(query);
        return myQuery.execute().list();
    }

    @Override
    public List<Object[]> queryObject(String query){
        QueryFactory queryFactory = Search.getQueryFactory(booksCache);
        Query<Object[]> myQuery = queryFactory.create(query);
        return myQuery.execute().list();
    }
}