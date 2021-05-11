package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import com.alopezme.hotrodtester.repository.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("query")
public class QueriesController {

    @Autowired
    @Qualifier("BookServiceProtoImpl")
    private BookService bookRepository;

    Logger logger = LoggerFactory.getLogger(QueriesController.class);

    /**
     * LOAD CACHE
     */

    @GetMapping("/load")
    public String loadBooksCache() {
        readFileToLoadCache(2679);
        return "Books cache now contains " + bookRepository.getSize() + " entries";
    }

    @GetMapping("/reduced-load")
    public String reducedLoadBooksCache() {
        readFileToLoadCache(100);
        return "Books cache now contains " + bookRepository.getSize() + " entries";
    }


    /**
     * GET
     */
    @GetMapping("/{id}")
    public String getByID(
            @PathVariable(value = "id") int id) {
        return bookRepository.findById(id).toString();
    }

    /**
     * GET ALL
     */
    @GetMapping("/keys")
    public String getKeys() {
        return bookRepository.getKeys();
    }

    @GetMapping("/entries")
    public String getEntries() {
        return bookRepository.getValues();
    }



    /**
     * PUT
     */
    @PutMapping("/{id}")
    public void putById(
            @PathVariable(value = "id") int id) {
        Book book = new Book(id,"Coding from home" ,"Álvaro López Medina",2021);
        bookRepository.insert(id,book);
    }

    /**
     * PUT BULK
     */
    @PutMapping("/bulk/{maxKey}")
    public void putBulk(
            @PathVariable(value = "maxKey") int maxKey) {
        readFileToLoadCache(2679);
        for (int id = 0; id < maxKey; id++) {
            Book book = new Book(id,"Coding from home" ,"Álvaro López Medina",2021);
            bookRepository.insert(id,book);
            logger.info("PUT - " + book.toString());
        }
    }


    /**
     * REMOVE
     */
    @DeleteMapping("/{id}")
    public void removeById(
            @PathVariable(value = "id") int id) {
        bookRepository.delete(id);
    }

    /**
     * REMOVE ALL
     */
    @DeleteMapping("/")
    public void removeById() {
        bookRepository.deleteAll();
    }






    /***
     * QUERIES: GET
     */

    @GetMapping("/query/title")
    public String queryByTitle()  {
        return bookRepository.query("FROM com.alopezme.hotrodtester.model.Book WHERE title='The Iliad'").toString();
    }

    @GetMapping("/query/author")
    public String queryByAuthor()  {
        return bookRepository.query("FROM com.alopezme.hotrodtester.model.Book WHERE author:'Homer'").toString();
    }







    /***
     * QUERIES: REMOVE
     */

    @GetMapping("/query/remove-01")
    public String queryRemove01()  {

        List<Book> list = bookRepository.query("FROM com.alopezme.hotrodtester.model.Book WHERE title='The Iliad'");
        logger.info("Removing ... " + list.toString());
        for (Book book : list ) {
            bookRepository.delete(book.getId());
        }
        return list.toString();
    }

    @GetMapping("/query/remove-02")
    public String queryRemove02()  {

        List<Object[]> list = bookRepository.queryObject("SELECT id FROM com.alopezme.hotrodtester.model.Book WHERE author:'Homer'");
        List<Integer> result = new ArrayList<Integer>();
        for (Object[] book : list ) {
            logger.info("Removing book " + Integer.toString((Integer)book[0]));
            bookRepository.delete((Integer)book[0]);
            result.add((Integer)book[0]);
        }
        return result.toString();
    }

    @GetMapping("/query/remove-03")
    public String queryRemove03()  {
        List<Object[]> list = bookRepository.queryObject("SELECT id FROM com.alopezme.hotrodtester.model.Book WHERE author:'Homer'");
        Set<Integer> listToRemove = list.stream()
                .map(row -> (Integer) row[0])
                .collect(Collectors.toSet());

        if (listToRemove.isEmpty())
            return "The remove operation does not contain any entry.";

        boolean status = bookRepository.bulkRemove(listToRemove);
        if (status)
            return listToRemove.toString();
        else
            return "Remove operation failed." + System.lineSeparator();
    }


    private void readFileToLoadCache(int limit) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/books.csv")))) {
            String line;
            int iteration = 0;
            while ((line = br.readLine()) != null && iteration < limit) {
                String[] values = line.split(",");
                Book book = new Book(Integer.valueOf(values[0].trim()), values[1].trim(), values[2].trim(), Integer.valueOf(values[3].trim()));
                logger.debug("PUT : " + book.toString());
                bookRepository.insert(book.getId(), book);
                iteration++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
