package com.alopezme.hotrodtester.controller;

import com.alopezme.hotrodtester.model.Book;
import com.alopezme.hotrodtester.repository.BookService;
import com.alopezme.hotrodtester.repository.impl.BookServiceJavaImpl;
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
public class BookController {

    @Autowired
    @Qualifier("BookServiceJavaImpl")
    private BookService bookRepository;

    Logger logger = LoggerFactory.getLogger(BookController.class);


    /**
     * LOAD CACHE
     */

    @GetMapping("/load")
    public String loadBooksCache() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/books.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                Book book = new Book(Integer.valueOf(values[0].trim()), values[1].trim(), values[2].trim(), Integer.valueOf(values[3].trim()));
                logger.info("PUT : " + book.toString());
                bookRepository.insert(book.getId(), book);
            }
        }
        return "Books cache now contains " + bookRepository.getSize() + " entries";
    }

    @GetMapping("/reduced-load")
    public String reducedLoadBooksCache() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/books.csv")))) {
            String line;
            int iteration = 0;
            while ((line = br.readLine()) != null && iteration < 100) {
                String[] values = line.split(",");
                Book book = new Book(Integer.valueOf(values[0].trim()), values[1].trim(), values[2].trim(), Integer.valueOf(values[3].trim()));
                logger.info("PUT : " + book.toString());
                bookRepository.insert(book.getId(), book);
                iteration++;
            }
        }
        return "Books cache now contains " + bookRepository.getSize() + " entries";
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
     * GET
     */
    @GetMapping("/{id}")
    public String getByID(
            @PathVariable(value = "id") int id) {
        return bookRepository.findById(id).toString();
    }

    /**
     * REMOVE
     */
    @DeleteMapping("/{id}")
    public void removeById(
            @PathVariable(value = "id") int id) {
        bookRepository.delete(id);
    }


}