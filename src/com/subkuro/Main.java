package com.subkuro;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.apache.commons.cli.*;
import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static class KanaToRomaji {
        Map<String, String> m = new HashMap<String, String>();

        // Constructor
        public KanaToRomaji() {
            m.put("ア", "a");
            m.put("イ", "i");
            m.put("ウ", "u");
            m.put("エ", "e");
            m.put("オ", "o");
            m.put("カ", "ka");
            m.put("キ", "ki");
            m.put("ク", "ku");
            m.put("ケ", "ke");
            m.put("コ", "ko");
            m.put("サ", "sa");
            m.put("シ", "shi");
            m.put("ス", "su");
            m.put("セ", "se");
            m.put("ソ", "so");
            m.put("タ", "ta");
            m.put("チ", "chi");
            m.put("ツ", "tsu");
            m.put("テ", "te");
            m.put("ト", "to");
            m.put("ナ", "na");
            m.put("ニ", "ni");
            m.put("ヌ", "nu");
            m.put("ネ", "ne");
            m.put("ノ", "no");
            m.put("ハ", "ha");
            m.put("ヒ", "hi");
            m.put("フ", "fu");
            m.put("ヘ", "he");
            m.put("ホ", "ho");
            m.put("マ", "ma");
            m.put("ミ", "mi");
            m.put("ム", "mu");
            m.put("メ", "me");
            m.put("モ", "mo");
            m.put("ヤ", "ya");
            m.put("ユ", "yu");
            m.put("ヨ", "yo");
            m.put("ラ", "ra");
            m.put("リ", "ri");
            m.put("ル", "ru");
            m.put("レ", "re");
            m.put("ロ", "ro");
            m.put("ワ", "wa");
            m.put("ヲ", "wo");
            m.put("ン", "n");
            m.put("ガ", "ga");
            m.put("ギ", "gi");
            m.put("グ", "gu");
            m.put("ゲ", "ge");
            m.put("ゴ", "go");
            m.put("ザ", "za");
            m.put("ジ", "ji");
            m.put("ズ", "zu");
            m.put("ゼ", "ze");
            m.put("ゾ", "zo");
            m.put("ダ", "da");
            m.put("ヂ", "ji");
            m.put("ヅ", "zu");
            m.put("デ", "de");
            m.put("ド", "do");
            m.put("バ", "ba");
            m.put("ビ", "bi");
            m.put("ブ", "bu");
            m.put("ベ", "be");
            m.put("ボ", "bo");
            m.put("パ", "pa");
            m.put("ピ", "pi");
            m.put("プ", "pu");
            m.put("ペ", "pe");
            m.put("ポ", "po");
            m.put("キャ", "kya");
            m.put("キュ", "kyu");
            m.put("キョ", "kyo");
            m.put("シャ", "sha");
            m.put("シュ", "shu");
            m.put("ショ", "sho");
            m.put("チャ", "cha");
            m.put("チュ", "chu");
            m.put("チョ", "cho");
            m.put("ニャ", "nya");
            m.put("ニュ", "nyu");
            m.put("ニョ", "nyo");
            m.put("ヒャ", "hya");
            m.put("ヒュ", "hyu");
            m.put("ヒョ", "hyo");
            m.put("リャ", "rya");
            m.put("リュ", "ryu");
            m.put("リョ", "ryo");
            m.put("ギャ", "gya");
            m.put("ギュ", "gyu");
            m.put("ギョ", "gyo");
            m.put("ジャ", "ja");
            m.put("ジュ", "ju");
            m.put("ジョ", "jo");
            m.put("ティ", "ti");
            m.put("ディ", "di");
            m.put("ツィ", "tsi");
            m.put("ヂャ", "dya");
            m.put("ヂュ", "dyu");
            m.put("ヂョ", "dyo");
            m.put("ビャ", "bya");
            m.put("ビュ", "byu");
            m.put("ビョ", "byo");
            m.put("ピャ", "pya");
            m.put("ピュ", "pyu");
            m.put("ピョ", "pyo");
            m.put("ー", "-");
        }

        public String convert(String s) {
            StringBuilder t = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                if (i <= s.length() - 2) {
                    if (m.containsKey(s.substring(i, i + 2))) {
                        t.append(m.get(s.substring(i, i + 2)));
                        i++;
                    } else if (m.containsKey(s.substring(i, i + 1))) {
                        t.append(m.get(s.substring(i, i + 1)));
                    } else if (s.charAt(i) == 'ッ') {
                        t.append(m.get(s.substring(i + 1, i + 2)).charAt(0));
                    } else {
                        t.append(s.charAt(i));
                    }
                } else {
                    if (m.containsKey(s.substring(i, i + 1))) {
                        t.append(m.get(s.substring(i, i + 1)));
                    } else {
                        t.append(s.charAt(i));
                    }
                }
            }
            return t.toString();
        }
    }

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

    static class Section {
        Section(String name) {
            this.name = name;
            this.lines = new ArrayList<>();
        }
        ArrayList<String> lines;
        String name;
    }

    static class ASSFile {
        HashMap<String, Section> sections = new LinkedHashMap();
        Tokenizer tokenizer = Tokenizer.builder().build();
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        KanaToRomaji kr = new KanaToRomaji();
        static private String newStyleName = "Default - top";

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
                    if (line.startsWith("[") && line.endsWith("]"))  {
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

        public void updateDialogue() {
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

                String newPhrase = getNewPhrase(matcher.group(4));
                newLines.add(matcher.group(1) + newStyleName + matcher.group(3) + newPhrase);
            }
            eventsSection.lines = newLines;
        }

        private String getNewPhrase(String group) {
            ArrayList<String> tokens = new ArrayList<>();
            ArrayList<String> readingForms = new ArrayList<>();
            for (Token token : tokenizer.tokenize(group)) {
                String surfaceForm = token.getSurfaceForm();
                if (surfaceForm.length() == 1) {
                    if (Character.isWhitespace(surfaceForm.codePointAt(0)) || surfaceForm == "\u3000") {
                        continue;
                    }
                }
                String reading = token.getReading();
                if (reading != null) {
                    reading = kr.convert(reading);
                }
                readingForms.add(reading);
                tokens.add(surfaceForm);
            }
            // Translate all at once in the end.
            tokens.add(group);

            List<Translation> translations = translate.translate(
                    tokens,
                    Translate.TranslateOption.sourceLanguage("ja"),
                    Translate.TranslateOption.targetLanguage("en"));
            int i = 0;
            StringBuilder sb = new StringBuilder(group.length() * 4);
            for (Translation translation : translations) {
                if (tokens.get(i).trim().isEmpty()) {
                    i += 1;
                    continue;
                }
                // Last one, treat differently
                if (i == tokens.size() - 1) {
                    sb.append("\\N");
                    sb.append(translation.getTranslatedText());
                    break;
                }
                sb.append(tokens.get(i));
                if (readingForms.get(i) != null) {
                    sb.append("{\\i1}(");
                    sb.append(readingForms.get(i));
                    sb.append("){\\i0}=");
                }
                sb.append(translation.getTranslatedText());
                sb.append("; ");
                i += 1;
            }
            return sb.toString();
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
