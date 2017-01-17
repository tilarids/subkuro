package com.subkuro;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import java.io.*;
import java.util.ArrayList;
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
    Tokenizer tokenizer = new Tokenizer();
    //Translate translate = TranslateOptions.getDefaultInstance().getService();
    GTranslate translate = new GTranslate();
    KanaToRomaji kr = new KanaToRomaji();
    static private String leftStyleName = "Default - left";
    static private String topStyleName = "Default - top";
    static private String oldStyleName = "*Default";

    public void parseFile(String inputFilePath) throws IOException {
        String currentSection = "";
        sections.put(currentSection, new Section(""));
        FileInputStream fis = new FileInputStream(inputFilePath);
        UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
        ubis.skipBOM();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                ubis, ubis.getBOM().toString()))) {
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

    void addStyle() {
        Section stylesSection = sections.get("[V4+ Styles]");
        assert stylesSection != null;
        stylesSection.lines.add("Style: Default - top,Arial,26,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,2,8,10,10,10,1");
        stylesSection.lines.add("Style: Default - left,Arial,26,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,2,4,10,10,10,1");
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
            Phrases newPhrase = getNewPhrases(matcher.group(4));
            newLines.add(matcher.group(1) + leftStyleName + matcher.group(3) + newPhrase.mainPhrase);
            newLines.add(matcher.group(1) + topStyleName + matcher.group(3) + newPhrase.romanization);
            System.out.println("T: " + newPhrase.mainPhrase);
        }
        eventsSection.lines = newLines;
    }

    private Phrases getNewPhrases(String group) throws IOException {
        ArrayList<String> tokens = new ArrayList<>();
        ArrayList<String> readingForms = new ArrayList<>();

        for (Token token : tokenizer.tokenize(group)) {
            String surfaceForm = token.getSurface();
            if (surfaceForm.length() == 1) {
                if (Character.isWhitespace(surfaceForm.codePointAt(0)) || surfaceForm == "\u3000" || surfaceForm == "…") {
                    continue;
                }
            }
            String reading = token.getReading();

            readingForms.add(reading);
            tokens.add(surfaceForm);
        }

        for (int i = 0; i < tokens.size() - 1; ++i) {
            if (readingForms.get(i).endsWith("ッ")) {
                tokens.set(i, tokens.get(i) + tokens.get(i + 1));
                readingForms.set(i, readingForms.get(i) + readingForms.get(i + 1));
                tokens.remove(i + 1);
                readingForms.remove(i + 1);
            }
            readingForms.set(i, kr.convert(readingForms.get(i)));
        }
        readingForms.set(tokens.size() - 1, kr.convert(readingForms.get(tokens.size() - 1)));

        ArrayList<GTranslate.TranslationResult> translations = translate.translate(tokens);
        StringBuilder sb = new StringBuilder(group.length() * 4);
        for (int i = 0; i < translations.size(); ++i) {
            if (tokens.get(i).trim().isEmpty()) {
                continue;
            }
            sb.append(tokens.get(i));
            if (readingForms.get(i) != null) {
                sb.append("{\\i1}(");
                sb.append(readingForms.get(i));
                sb.append("){\\i0}=");
            }
            sb.append(translations.get(i).translation);
            sb.append(";\\N");
        }

        GTranslate.TranslationResult full = translate.translate(group);
        sb.append("\\N");
        sb.append(full.translation);

        return new Phrases(sb.toString(), full.romanization);
    }

    private class Phrases {
        private final String mainPhrase;
        private final String romanization;

        public Phrases(String s, String romanization) {
            this.mainPhrase = s;
            this.romanization = romanization;
        }
    }
}
