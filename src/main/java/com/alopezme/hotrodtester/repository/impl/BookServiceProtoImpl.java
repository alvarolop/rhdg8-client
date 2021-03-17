package com.alopezme.hotrodtester.repository.impl;

import com.alopezme.hotrodtester.model.Book;
import com.alopezme.hotrodtester.repository.BookService;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service(value="BookServiceProtoImpl")
public class BookServiceProtoImpl implements BookService {

    @Autowired
    private RemoteCache<Integer, Book> indexedBooksCache;

    Logger logger = LoggerFactory.getLogger(BookServiceProtoImpl.class);

    @Override
    public Book findById(int id){
        return indexedBooksCache.get(id);
    }

    @Override
    public Book insert(int id, Book book){
        return indexedBooksCache.put(id, book);
    }

    @Override
    public void delete(int id){
        indexedBooksCache.remove(id);
    }

    @Override
    public int getSize(){
        return indexedBooksCache.size();
    }

    @Override
    public String getKeys(){
        return indexedBooksCache.keySet().toString();
    }

    @Override
    public String getValues(){
        return indexedBooksCache.values().toString();
    }

    @Override
    public boolean bulkRemove(Set<Integer> keys){
        return indexedBooksCache.keySet().removeAll(keys);
    }

    @Override
    public List<Book> query(String query){
        QueryFactory queryFactory = Search.getQueryFactory(indexedBooksCache);
        Query<Book> myQuery = queryFactory.create(query);
        return myQuery.execute().list();
    }

    @Override
    public List<Object[]> queryObject(String query){
        QueryFactory queryFactory = Search.getQueryFactory(indexedBooksCache);
        Query<Object[]> myQuery = queryFactory.create(query);
        return myQuery.execute().list();
    }
}