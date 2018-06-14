package com.ebay.salesstatsnicolasr.model;

public class Statistics {
    
    private final long id;
    private final String content;

    public Statistics(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
