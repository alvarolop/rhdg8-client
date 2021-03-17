package com.alopezme.hotrodtester.repository;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@CacheConfig(cacheNames="books")
@Repository
public class BookRepository {

    @Autowired
    private SpringRemoteCacheManager cacheManager;

    private String CACHE_NAME="books";

    Logger logger = LoggerFactory.getLogger(BookRepository.class);

    @Cacheable(key="#id")
    public Book findById(int id){
        return null;
    }

    @CachePut(key="#id")
    public Book insert(int id, Book c){
        return c;
    }

    @CacheEvict(key="#id")
    public void delete(int id){
    }

    public String getKeys(){
        return cacheManager.getCache(CACHE_NAME).getNativeCache().keySet().toString();
    }

    public String getValues(){
        return cacheManager.getCache(CACHE_NAME).getNativeCache().values().toString();
    }

    public boolean bulkRemove(Set<Integer> keys){
        return cacheManager.getCache(CACHE_NAME).getNativeCache().keySet().removeAll(keys);
    }


    public List<Book> query(String query){
//        RemoteCache<String, Integer> cache = cacheManager.getNativeCacheManager().getCache(CACHE_NAME);
        QueryFactory queryFactory = Search.getQueryFactory(cacheManager.getNativeCacheManager().getCache(CACHE_NAME));
        Query<Book> myQuery = queryFactory.create(query);
        return myQuery.execute().list();
    }

    public List<Object[]> queryObject(String query){
//        RemoteCache<String, Integer> cache = cacheManager.getNativeCacheManager().getCache(CACHE_NAME);
        QueryFactory queryFactory = Search.getQueryFactory(cacheManager.getNativeCacheManager().getCache(CACHE_NAME));
        Query<Object[]> myQuery = queryFactory.create(query);
        return myQuery.execute().list();
    }
}