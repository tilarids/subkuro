package com.subkuro;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Created by tilarids on 1/17/17.
 */
public class SubtitlesDatabase {
    private String databaseName;

    HashSet<String> skipWords = new HashSet<>();
    SubtitlesDatabase(String databaseName) throws FileNotFoundException {
        this.databaseName = databaseName;
        readAllSkipWords(databaseName);
    }

    private void readAllSkipWords(String databaseName) throws FileNotFoundException {
        File file = new File(databaseName);
        if (!file.exists()) {
            // don't try to read it.
            return;
        }
        try (Scanner s = new Scanner(file).useDelimiter("\\Z")) {
            String contents = s.next();
            JSONObject object = new JSONObject(contents);
            JSONArray words = object.getJSONArray("skip_words");
            for(int i = 0; i < words.length(); ++i) {
                skipWords.add(words.getString(i));
            }
        }
    }

    public boolean shouldSkip(String word) {
        return skipWords.contains(word);
    }

    public void writeDatabase() throws IOException {
        JSONObject json = new JSONObject();
        json.put("skip_words", skipWords);
        FileWriter writer = new FileWriter(this.databaseName);
        json.write(writer);
        writer.flush();
    }

    public void storeSkipWord(String foreignPart) throws IOException {
        skipWords.add(foreignPart);
        writeDatabase();
    }
}
