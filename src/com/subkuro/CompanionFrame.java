package com.subkuro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by tilarids on 1/17/17.
 */
public class CompanionFrame extends JFrame {
    private final SubtitlesDatabase database;
    private int time = -1;
    private final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();

    public CompanionFrame(SubtitlesDatabase database) {
        this.database = database;

        initUI();
    }

    private void initUI() {
        setTitle("Companion");
        setSize(300, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }


    public void updateUI(int time, PhraseTranslator.Phrases phrase) {
        if (this.time == time) {
            return;
        }
        this.time = time;
        JPanel pane = (JPanel) getContentPane();
        pane.removeAll();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        for (int i = 0; i < phrase.foreignTokens.size(); ++i) {
            pane.add(new TokensPanel(this.database,
                                     phrase.foreignTokens.get(i),
                                     phrase.readingFormTokens.get(i),
                                     phrase.translatedTokens.get(i)));
        }

        JButton fullTranslateButton = new JButton();

        fullTranslateButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                fullTranslateButton.setText(phrase.translation);
            }
        });

        Box  b = Box.createHorizontalBox();
        b.add( Box.createHorizontalGlue() );
        b.add( fullTranslateButton );
        pane.add(b);

        pack();

        positionWindow();
    }

    private class TokensPanel extends JPanel {
        private final String readingFormPart;
        private final String translatedPart;
        private final String foreignPart;
        private final SubtitlesDatabase database;

        public TokensPanel(SubtitlesDatabase database, String foreignPart, String readingFormPart, String translatedPart) {
            this.database = database;
            this.foreignPart = foreignPart;
            this.readingFormPart = readingFormPart;
            this.translatedPart = translatedPart;
            JTextField readingFormPartComponent = new JTextField(20);
            JTextField translatedPartComponent = new JTextField(20);

            readingFormPartComponent.setText(this.readingFormPart);
            boolean shouldSkip = this.database.shouldSkip(this.foreignPart);
            if (shouldSkip) {
                translatedPartComponent.setText(this.foreignPart);
            } else {
                translatedPartComponent.setText(this.translatedPart);
            }

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            JButton revealButton = new JButton("reveal");
            revealButton.addActionListener((ActionEvent event) -> {
                translatedPartComponent.setText(translatedPart);
            });

            JButton storeButton = new JButton("store");
            storeButton.addActionListener((ActionEvent event) -> {
                try {
                    database.storeSkipWord(foreignPart);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            this.add(readingFormPartComponent);
            this.add(storeButton);
            this.add(revealButton);
            this.add(translatedPartComponent);
        }
    }

    public void positionWindow() {
        int x = (int) (dimension.getWidth() - this.getWidth());
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
    }
}
