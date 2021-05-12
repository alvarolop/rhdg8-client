package com.alopezme.hotrodtester.configuration;

public final class CacheNames {

    public static final String BOOKS_CACHE_NAME = "books-serialization";
    public static final String PROTO_CACHE_NAME = "books-protostream";
    public static final String INDEXED_CACHE_NAME = "books-indexed";
    public static final String TRANSACTIONAL_CACHE_NAME = "books-transactional";
    static final String TESTER_CACHE_NAME = "tester";
    static final String SESSIONS_CACHE_NAME = "sessions";

    // The name of the Scripts cache.
    public static final String SCRIPTS_METADATA_CACHE_NAME = "___script_cache";
    // The name of the Protobuf definitions cache.
    public static final String PROTOBUF_METADATA_CACHE_NAME = "___protobuf_metadata";

    private CacheNames() {

    }
}