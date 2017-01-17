package com.subkuro;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by tilarids on 1/17/17.
 */
public class VLCCompanion {
    SubtitlesDatabase database;

    public VLCCompanion(Connection con) throws SQLException {
        this.database = new SubtitlesDatabase(con);
    }

    public static void main(String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option db = new Option("db", "database", true, "words database");
        db.setRequired(true);
        options.addOption(db);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        String inputFilePath = cmd.getOptionValue("input");
        String dbName = cmd.getOptionValue("db");

        ASSFile subtitleFile = new ASSFile();
        try {
            subtitleFile.parseFile(inputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String connectionURL = "jdbc:postgresql://localhost:5432/" + dbName;
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection (connectionURL);
            VLCCompanion companion = new VLCCompanion(con);
            companion.startPolling(subtitleFile, con);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startPolling(ASSFile subtitleFile, Connection con) throws SQLException {
    }
}
