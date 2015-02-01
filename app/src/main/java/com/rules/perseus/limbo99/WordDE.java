package com.rules.perseus.limbo99;

public class WordDE {
    private long id;
    private String word;

    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public String getWord() {

        return this.word;
    }

    public void setWord(String word) {

        this.word = word;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {

        return word;
    }
}
