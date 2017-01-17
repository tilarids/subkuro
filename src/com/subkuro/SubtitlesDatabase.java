package com.subkuro;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by tilarids on 1/17/17.
 */
public class SubtitlesDatabase {
    HashSet<String> skipWords = new HashSet<>();
    SubtitlesDatabase(Connection con) throws SQLException {
        readAllSkipWords(con);
    }

    private void readAllSkipWords(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select id, data from skip_words");

        while (rs.next()) {
            skipWords.add(rs.getString("id"));
        };
    }

    public boolean shouldSkip(String word) {
        return skipWords.contains(word);
    }

}
