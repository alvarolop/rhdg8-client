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

@RestController
@RequestMapping("book")
public class BookJavaController {

    @Autowired
    @Qualifier("BookServiceJavaImpl")
    private BookService bookRepository;

    Logger logger = LoggerFactory.getLogger(BookJavaController.class);


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
     * GET BULK
     */
    @GetMapping("/bulk/{maxKey}")
    public void getBulk(
            @PathVariable(value = "maxKey") int maxKey) {
        for (int id = 0; id < maxKey; id++) {
            bookRepository.findById(id);
        }
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
        int limitKey = 2679;
        readFileToLoadCache(Math.min(maxKey,limitKey));
        for (int id = limitKey; id < maxKey; id++) {
            Book book = new Book(id,"Coding from home" ,"Álvaro López Medina",2021);
            bookRepository.insert(id,book);
            logger.debug("PUT - " + book.toString());
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