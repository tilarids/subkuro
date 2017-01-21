package com.subkuro;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Created by tilarids on 1/21/17.
 */
public class JMDict {
    private Document document = null;
    private HashMap<String, Element> kanjiMap = new HashMap<>();
    private HashMap<String, Element> readingMap = new HashMap<>();

    JMDict(String fileName) throws IOException, JDOMException {
        InputStream stream = new GZIPInputStream(new FileInputStream(fileName));
        SAXBuilder builder = new SAXBuilder();

        this.document = (Document) builder.build(stream);
        for (Element entry : this.document.getRootElement().getChildren("entry")) {
            for (Element k_ele : entry.getChildren("k_ele")) {
                kanjiMap.put(k_ele.getChildText("keb"), entry);
            }
            for (Element r_ele : entry.getChildren("r_ele")) {
                readingMap.put(r_ele.getChildText("reb"), entry);
            }
        }
    }

    Set<String> getGlosses(String lookup) {
        Set<String> ret = new HashSet<>();
        Element kanjiEntry = kanjiMap.get(lookup);
        if (kanjiEntry != null) {
            for (Element gloss : kanjiEntry.getChild("sense").getChildren("gloss")) {
                ret.add(gloss.getText());
            }
        }
        Element readingEntry = readingMap.get(lookup);
        if (readingEntry != null) {
            for (Element gloss : readingEntry.getChild("sense").getChildren("gloss")) {
                ret.add(gloss.getText());
            }
        }
        return ret;
    }
}
