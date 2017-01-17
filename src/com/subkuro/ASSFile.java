package com.subkuro;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tilarids on 1/17/17.
 */
class ASSFile {
    static class Section {
        Section(String name) {
            this.name = name;
            this.lines = new ArrayList<>();
        }

        ArrayList<String> lines;
        String name;
    }

    HashMap<String, Section> sections = new LinkedHashMap();
    HashMap<String, PhraseTranslator.Phrases> phrases = new LinkedHashMap();
    ArrayList<String> phrasesIndex = null;

    PhraseTranslator translator = new PhraseTranslator();
    static private String leftStyleName = "Default - left";
    static private String topStyleName = "Default - top";
    static private String oldStyleName = "*Default";
    static private String rightStyleName = "Default - right";

    public void parseFile(String inputFilePath) throws IOException {
        String currentSection = "";
        sections.put(currentSection, new Section(""));
        FileInputStream fis = new FileInputStream(inputFilePath);
        UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
        ubis.skipBOM();
        String encoding = ubis.getBOM().toString();
        if (encoding == "NONE") {
            encoding = "UTF-8";  // default is UTF-8.
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ubis, encoding))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line;
                    sections.put(currentSection, new Section(currentSection));
                } else {
                    sections.get(currentSection).lines.add(line);
                }
            }
        }
    }

    void addStyles() {
        Section stylesSection = sections.get("[V4+ Styles]");
        assert stylesSection != null;
        stylesSection.lines.add("Style: Default - top,Arial,26,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,2,8,10,10,10,1");
        stylesSection.lines.add("Style: Default - left,Arial,26,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,2,4,10,10,10,1");
        stylesSection.lines.add("Style: Default - right,Arial,26,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,2,6,10,10,10,1");
    }

    void writeFile(String outputFilePath) throws IOException {
        File file = new File(outputFilePath);

        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }

        Writer writer =
                new OutputStreamWriter(
                        new FileOutputStream(file.getAbsoluteFile()), "UTF-8");
        BufferedWriter bw = new BufferedWriter(writer);

        for (Section section : sections.values()) {
            if (section.name.isEmpty()) {
                continue;
            }
            bw.write(section.name + "\n");
            for (String line : section.lines) {
                bw.write(line + "\n");
            }
        }

        bw.close();
    }

    public void updateDialogue() throws IOException {
        Section eventsSection = sections.get("[Events]");
        assert eventsSection != null;
        ArrayList<String> newLines = new ArrayList<>();

        Pattern pattern = Pattern.compile("(Dialogue: \\d+,\\d+:\\d+:\\d+.\\d+,\\d+:\\d+:\\d+.\\d+,)([^,]+)(,[^,]+,\\d+,\\d+,\\d+,,)(.*)");
        for (String line : eventsSection.lines) {
            newLines.add(line);

            if (!line.startsWith("Dialogue:")) {
                continue;
            }
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) {
                throw new RuntimeException("Can't find a Dialogue line: \n" + line);
            }

            if (matcher.group(2).compareTo(oldStyleName) != 0) {
                continue;
            }
            PhraseTranslator.Phrases newPhrase = translator.translatePhrase(matcher.group(4));
            StringBuilder mainPhrase = new StringBuilder(matcher.group(4).length() * 2);
            for (String foreignToken : newPhrase.foreignTokens) {
                mainPhrase.append(foreignToken);
                mainPhrase.append("\\N");
            }

            StringBuilder translatedPhrase = new StringBuilder(matcher.group(4).length() * 2);
            for (String translatedToken : newPhrase.translatedTokens) {
                translatedPhrase.append(translatedToken);
                translatedPhrase.append("\\N");
            }
            translatedPhrase.append("\\N\\N");
            translatedPhrase.append(newPhrase.translation);

            newLines.add(matcher.group(1) + leftStyleName + matcher.group(3) + mainPhrase.toString());
            newLines.add(matcher.group(1) + rightStyleName + matcher.group(3) + translatedPhrase.toString());
            newLines.add(matcher.group(1) + topStyleName + matcher.group(3) + newPhrase.romanization);
            System.out.println("T: " + mainPhrase.toString());
        }
        eventsSection.lines = newLines;
    }

    public void parseTranslatedDialogue() {
        Section eventsSection = sections.get("[Events]");
        assert eventsSection != null;
        ArrayList<String> newLines = new ArrayList<>();

        Pattern pattern = Pattern.compile("Dialogue: \\d+,(\\d+:\\d+:\\d+.\\d+,\\d+:\\d+:\\d+.\\d+,)([^,]+)(,[^,]+,\\d+,\\d+,\\d+,,)(.*)");
        for (String line : eventsSection.lines) {
            if (!line.startsWith("Dialogue:")) {
                continue;
            }
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) {
                throw new RuntimeException("Can't find a Dialogue line: \n" + line);
            }
            PhraseTranslator.Phrases phrase = phrases.get(matcher.group(1));
            if (phrase == null) {
                phrase = new PhraseTranslator.Phrases("", "", "");
                phrases.put(matcher.group(1), phrase);
            }
            if (matcher.group(2).compareTo(oldStyleName) == 0) {
                phrase.original = matcher.group(4);
            } else if (matcher.group(2).compareTo(leftStyleName) == 0) {
                String[] parts = matcher.group(4).split("\\\\N");
                for (String part : parts) {
                    String[] split = part.split("\\{\\\\i1\\}");
                    phrase.foreignTokens.add(split[0]);
                    phrase.readingFormTokens.add(split[1].substring(1, split[1].length() - 7));
                }
            } else if (matcher.group(2).compareTo(rightStyleName) == 0) {
                String[] partsOuter = matcher.group(4).split("\\\\N\\\\N");
                String[] partsInner = partsOuter[0].split("\\\\N");
                for (String part : partsInner) {
                    phrase.translatedTokens.add(part);
                }
                phrase.translation = partsOuter[1];
            } else if (matcher.group(2).compareTo(topStyleName) == 0) {
                phrase.romanization = matcher.group(4);
            }
        }
        phrasesIndex = new ArrayList<>();
        phrasesIndex.addAll(phrases.keySet());
        Collections.sort(phrasesIndex);
    }

    PhraseTranslator.Phrases getPhraseAtTime(int time) {
        if (phrasesIndex == null) {
            throw new RuntimeException("No phrases index");
        }

        int index = Collections.binarySearch(phrasesIndex, getStringTime(time));

        if (index < 0) {
            index = -index - 2;
        }
        return phrases.get(phrasesIndex.get(index));
    }

    private String getStringTime(int time) {
        int hours = time / 3600;
        time -= hours * 3600;
        int minutes = time / 60;

        time -= minutes * 60;
        int seconds = time;

        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
}
