package com.subkuro;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Character.isWhitespace;

/**
 * Created by tilarids on 1/17/17.
 */
public class PhraseTranslator {
    //Translate translate = TranslateOptions.getDefaultInstance().getService();
    GTranslate translate = new GTranslate();
    KanaToRomaji kr = new KanaToRomaji();
    Tokenizer tokenizer = new Tokenizer();

    public Phrases translatePhrase(String group) throws IOException {
        ArrayList<String> tokens = new ArrayList<>();
        ArrayList<String> readingForms = new ArrayList<>();

        for (Token token : tokenizer.tokenize(group)) {
            String surfaceForm = token.getSurface();
            if (surfaceForm.length() == 1) {
                if (isWhitespace(surfaceForm.codePointAt(0)) ||
                        surfaceForm.compareTo("\u3000") == 0 ||
                        surfaceForm.compareTo("…") == 0) {
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

        GTranslate.TranslationResult full = translate.translate(group);

        Phrases phrases = new Phrases(group, full.romanization, full.translation);
        for (int i = 0; i < translations.size(); ++i) {
            if (tokens.get(i).trim().isEmpty()) {
                continue;
            }
            String foreignToken = tokens.get(i);
            if (readingForms.get(i) != null) {
                foreignToken += "{\\i1}(" + readingForms.get(i) + "){\\i0}";
            }
            phrases.foreignTokens.add(foreignToken);
            phrases.translatedTokens.add(translations.get(i).translation);
        }
        return phrases;
    }

    public static class Phrases {
        public final ArrayList<String> foreignTokens = new ArrayList<>();
        public final ArrayList<String> translatedTokens = new ArrayList<>();
        public String romanization;
        public String translation;
        public String original;
        public final ArrayList<String> readingFormTokens = new ArrayList<>();

        public Phrases(String original, String romanization, String translation) {
            this.original = original;
            this.romanization = romanization;
            this.translation = translation;
        }
    }
}
