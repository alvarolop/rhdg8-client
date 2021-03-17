package com.alopezme.hotrodtester.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.io.Serializable;

@ProtoDoc("@Indexed")
@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
@JsonPropertyOrder({"id","title","author","publicationYear"})
public class Book implements Serializable {

    @ProtoDoc("@Field(index=Index.NO, store = Store.NO, analyze = Analyze.NO)")
    @ProtoField(number = 1, defaultValue = "0")
    @JsonProperty("id")
    public int id;

    @ProtoDoc("@Field(index=Index.YES, store = Store.YES, analyze = Analyze.NO)")
    @ProtoField(number = 2)
    public String title;

    @ProtoDoc("@Field(index=Index.YES, store = Store.YES, analyze = Analyze.YES)")
    @ProtoField(number = 3)
    public String author;

    @ProtoDoc("@Field(index=Index.NO, store = Store.NO, analyze = Analyze.NO)")
    @ProtoField(number = 4, defaultValue = "0")
    @JsonProperty("publicationYear")
    public int publicationYear;

    @ProtoFactory
    public Book(int id, String title, String author, int publicationYear) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
    }

    @SneakyThrows
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
