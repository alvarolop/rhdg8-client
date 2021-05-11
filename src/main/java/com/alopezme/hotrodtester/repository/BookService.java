package com.alopezme.hotrodtester.repository;

import com.alopezme.hotrodtester.model.Book;

import java.util.List;
import java.util.Set;

public interface BookService {

    public Book findById(int id);

    public Book insert(int id, Book book);

    public void delete(int id);

    public boolean bulkRemove(Set<Integer> keys);

    public void deleteAll();

    public int getSize();

    public String getKeys();

    public String getValues();

    public List<Book> query(String query);

    public List<Object[]> queryObject(String query);

}
