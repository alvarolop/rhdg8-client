package com.alopezme.datagridtester.model;

import com.alopezme.datagrid_tester.model.BookDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookMapper {
    
    BookMapper INSTANCE = Mappers.getMapper( BookMapper.class );
 
    BookDto bookToBookDto(Book book);

    Book bookDtoToBook(BookDto bookDto);
}
