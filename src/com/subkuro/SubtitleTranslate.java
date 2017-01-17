// Mostly copied from https://github.com/lkuza2/java-speech-api
package com.subkuro;

import org.apache.commons.cli.*;

import java.io.*;

public class SubtitleTranslate {

    public static void main(String[] args) {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(true);
        options.addOption(output);

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

        try {
            processFile(inputFilePath, outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(String inputFilePath, String outputFilePath) throws IOException {
        ASSFile f = new ASSFile();
        f.parseFile(inputFilePath);
        f.addStyle();
        f.updateDialogue();
        f.writeFile(outputFilePath);
    }
}
