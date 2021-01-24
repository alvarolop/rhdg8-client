package com.alopezme.datagridtester.controller;

import java.util.List;

import javax.validation.Valid;

import com.alopezme.datagrid_tester.api.BookApi;
import com.alopezme.datagrid_tester.model.BookDto;
import com.alopezme.datagridtester.model.Book;
import com.alopezme.datagridtester.model.BookMapper;
import com.alopezme.datagridtester.service.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController implements BookApi {

    @Autowired
    BookService bookService;

    @Override
    public ResponseEntity<Void> createBook(@Valid BookDto bookDto) {

        Book book = BookMapper.INSTANCE.bookDtoToBook(bookDto);

        bookService.save(book);
        return null;
    }

    @Override
    public ResponseEntity<List<BookDto>> getBookEntries() {

        List<Book> books = bookService.getBooks();
        // TODO from domain to dto

        return null;
    }

   

   


    
    
}
