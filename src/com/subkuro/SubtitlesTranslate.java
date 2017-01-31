// Mostly copied from https://github.com/lkuza2/java-speech-api
package com.subkuro;

import org.apache.commons.cli.*;

import java.io.*;

public class SubtitlesTranslate {

    public static void main(String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(true);
        options.addOption(output);

        Option hideTranslation = new Option("h", "hide_translation", false, "should hide translations from the subtitles");
        hideTranslation.setRequired(false);
        options.addOption(hideTranslation);

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
        String outputFilePath = cmd.getOptionValue("output");
        boolean showTranslation = true;
        if (cmd.hasOption("hide_translation")) {
            showTranslation = false;
        }

        String sourceLanguage = cmd.getOptionValue("source_language", "ja");
        String targetLanguage = cmd.getOptionValue("target_language", "en");
        String oldStyleName = cmd.getOptionValue("style_name", "*Default");;

        try {
            processFile(inputFilePath,
                        outputFilePath,
                        showTranslation,
                        sourceLanguage, targetLanguage,
                        oldStyleName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(String inputFilePath,
                                    String outputFilePath,
                                    boolean showTranslation,
                                    String sourceLanguage,
                                    String targetLanguage,
                                    String oldStyleName) throws IOException {
        ASSFile f = new ASSFile(sourceLanguage, targetLanguage, oldStyleName);
        f.parseFile(inputFilePath);
        f.addStyles(showTranslation);
        f.updateDialogue();
        f.writeFile(outputFilePath);
    }
}
