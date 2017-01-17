package com.subkuro;

import org.json.JSONObject;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by tilarids on 1/17/17.
 */
public class SubtitlesDatabase {
    private final Connection connection;
    HashSet<String> skipWords = new HashSet<>();
    SubtitlesDatabase(Connection con) throws SQLException {
        this.connection = con;
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

    public void storeSkipWord(String foreignPart) throws SQLException {
        skipWords.add(foreignPart);

        PreparedStatement pstmt = connection.prepareStatement("insert into skip_words(id, data) values (?, ?)");
        pstmt.setString(1, foreignPart);
        PGobject jsonObject = new PGobject();
        jsonObject.setType("json");
        JSONObject obj = new JSONObject();
        obj.put("word", foreignPart);
        jsonObject.setValue(obj.toString());
        pstmt.setObject(2, jsonObject);
        pstmt.execute();
    }
}
