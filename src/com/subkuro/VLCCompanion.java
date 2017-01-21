package com.subkuro;

import com.sun.jna.NativeLibrary;
import org.apache.commons.cli.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.*;

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

        Option media = new Option("m", "media", true, "media file path");
        media.setRequired(true);
        options.addOption(media);

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
        String mediaFilePath = cmd.getOptionValue("media");

        NativeLibrary.addSearchPath("vlc", "/Applications/VLC.app/Contents/MacOS/lib");

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
            companion.startPolling(subtitleFile, mediaFilePath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    class Poller implements Runnable {
        private final ASSFile subtitleFile;
        private final SubtitlesDatabase database;
        private final CompanionFrame frame;

        Poller(ASSFile subtitleFile, SubtitlesDatabase database, CompanionFrame frame) {
            this.subtitleFile = subtitleFile;
            this.database = database;
            this.frame = frame;
        }

        String getTime() throws IOException, JDOMException {
            URL url = new URL ("http://localhost:8080/requests/status.xml");
            String encoding = java.util.Base64.getEncoder().encodeToString(":1234".getBytes("utf-8"));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in   =
                    new BufferedReader (new InputStreamReader(content));
            String line;
            StringBuilder sb = new StringBuilder();
            String inline = "";
            while ((inline = in.readLine()) != null) {
                sb.append(inline);
            }

            SAXBuilder builder = new SAXBuilder();

            Document document = (Document) builder.build(new ByteArrayInputStream(sb.toString().getBytes()));
            return document.getRootElement().getChildText("time");
        }
        @Override
        public void run() {
            //                int time = Integer.decode(getTime());
//                PhraseTranslator.Phrases phrase = subtitleFile.getPhraseAtTime(time);
            this.frame.updateUI(subtitleFile);
        }
    }

    private void startPolling(ASSFile subtitleFile, String mediaFilePath) throws SQLException, InterruptedException, ExecutionException, IOException, JDOMException {
        subtitleFile.parseTranslatedDialogue();
        CompanionFrame frame = new CompanionFrame(this.database, mediaFilePath);
        frame.setVisible(true);


        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Poller(subtitleFile, database, frame), 0, 200, TimeUnit.MILLISECONDS);
        frame.startPlaying();
        Thread.sleep(Long.MAX_VALUE);
    }
}
