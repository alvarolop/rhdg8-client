package org.alopezme.example.springtester.queries;

import org.alopezme.example.springtester.model.Book;
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