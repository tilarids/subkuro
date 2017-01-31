package com.subkuro;

import org.apache.commons.cli.*;
import org.jdom2.JDOMException;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import java.io.*;
import java.util.concurrent.*;

/**
 * Created by tilarids on 1/17/17.
 */
public class VLCCompanion {
    SubtitlesDatabase database;

    public VLCCompanion(String databaseName) throws FileNotFoundException {
        this.database = new SubtitlesDatabase(databaseName);
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


        Option sourceLanguageOpt = new Option("s", "source_language", true, "source language");
        sourceLanguageOpt.setRequired(false);
        options.addOption(sourceLanguageOpt);

        Option targetLanguageOpt = new Option("t", "target_language", true, "target language");
        targetLanguageOpt.setRequired(false);
        options.addOption(targetLanguageOpt);

        // TODO(tilarids): Detect this automatically?
        Option styleNameOpt = new Option("n", "style_name", true, "style name");
        styleNameOpt.setRequired(false);
        options.addOption(styleNameOpt);

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

        new NativeDiscovery().discover();

        String sourceLanguage = cmd.getOptionValue("source_language", "ja");
        String targetLanguage = cmd.getOptionValue("target_language", "en");
        String oldStyleName = cmd.getOptionValue("style_name", "*Default");;

        ASSFile subtitleFile = new ASSFile(sourceLanguage, targetLanguage, oldStyleName);
        try {
            subtitleFile.parseFile(inputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        VLCCompanion companion = null;
        try {
            companion = new VLCCompanion(dbName);
            companion.startPolling(subtitleFile, mediaFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
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

        @Override
        public void run() {
            this.frame.updateUI();
        }
    }

    private void startPolling(ASSFile subtitleFile, String mediaFilePath) throws InterruptedException, ExecutionException, IOException, JDOMException {
        subtitleFile.parseTranslatedDialogue();
        CompanionFrame frame = new CompanionFrame(this.database, mediaFilePath, subtitleFile);
        frame.setVisible(true);


        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Poller(subtitleFile, database, frame), 0, 200, TimeUnit.MILLISECONDS);
        frame.startPlaying();
        Thread.sleep(Long.MAX_VALUE);
    }
}
