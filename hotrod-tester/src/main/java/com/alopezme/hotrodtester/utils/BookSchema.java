package com.alopezme.hotrodtester.utils;

import com.alopezme.hotrodtester.model.Book;
import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {
                Book.class
        },
        schemaFileName = "book.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "org.alopezme.springtester")
public interface BookSchema extends SerializationContextInitializer {
}