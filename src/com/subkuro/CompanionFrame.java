package com.subkuro;

import com.sun.jna.Memory;
import javafx.application.Platform;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import org.jdom2.JDOMException;
import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by tilarids on 1/17/17.
 */
public class CompanionFrame extends JFrame implements KeyListener {
    private final SubtitlesDatabase database;
    private final String mediaFile;
    private final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    private JLabel romanization;
    private MainPanel mainPanel;
    private JTextField fullTranslate;
    private TranslateHoverListener translateHoverListener;
    private JLabel timeLabel;
    static Font defaultFont = new Font("Menlo", Font.PLAIN, 16);

    private JMDict dict = null;
    private JTextField fullForeign;
    private JLabel selectedTranslate;

    public CompanionFrame(SubtitlesDatabase database, String mediaFile) throws IOException, JDOMException {
        this.database = database;
        this.mediaFile = mediaFile;
        initUI();


    }

    Set<String> getGlosses(String lookup) {
        if (this.dict == null) {
            return new HashSet<>();
        } else {
            return this.dict.getGlosses(lookup);
        }
    }

    void loadDict() throws IOException, JDOMException {
        System.out.println("Loading the dictionary!");
        JMDict newDict = new JMDict("JMdict.xml.gz");
        System.out.println("Dictionary loaded!");
        this.dict = newDict;
    }

    private void initUI() {
        setTitle("Companion");
        setSize(300, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.romanization = new JLabel();
        this.romanization.setFont(defaultFont);

        this.mainPanel = new MainPanel(this.database, mediaFile);
        this.mainPanel.playerPanel.addKeyListener(this);
        JPanel pane = (JPanel) getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(this.romanization);
        pane.add(this.mainPanel);
        this.fullTranslate = new JTextField(20);
        this.fullTranslate.setFont(defaultFont);

        this.timeLabel = new JLabel();

        JButton dictLoader = new JButton("load dict");
        dictLoader.addActionListener((ActionEvent event) -> {
            dictLoader.setText("loading...");
            dictLoader.setEnabled(false);
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        loadDict();
                        dictLoader.setText("loaded");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JDOMException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        this.fullForeign = new JTextField();
        this.selectedTranslate = new JLabel();
        JButton dictTanslate = new JButton("dict translate");
        dictTanslate.addActionListener((ActionEvent event) -> {
            Set<String> glosses = getGlosses(fullForeign.getSelectedText());
            selectedTranslate.setText(String.join("; ", glosses));
        });
        JButton googleTranslate = new JButton("gtranslate");
        googleTranslate.addActionListener((ActionEvent event) -> {
            try {
                selectedTranslate.setText(new GTranslate().translate(fullForeign.getSelectedText()).translation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Box  foreignBox = Box.createHorizontalBox();
        foreignBox.add(Box.createHorizontalGlue() );
        foreignBox.add(this.fullForeign);
        foreignBox.add(selectedTranslate);
        foreignBox.add(dictTanslate);
        foreignBox.add(googleTranslate);
        pane.add(foreignBox);

        Box  translateBox = Box.createHorizontalBox();
        translateBox.add( Box.createHorizontalGlue() );
        translateBox.add(this.timeLabel);
        translateBox.add(this.fullTranslate);
        translateBox.add(dictLoader);
        pane.add(translateBox);

        this.translateHoverListener = new TranslateHoverListener(fullTranslate);
        fullTranslate.addMouseListener(this.translateHoverListener);

        positionWindow();
    }

    void startPlaying() {
        this.mainPanel.playerPanel.playIfNecessary(this.mediaFile);
    }

    public void updateUI(ASSFile subtitleFile) {
        long time = this.mainPanel.playerPanel.getTime();
        if (this.mainPanel.playerPanel.getTime() <= 0) {
            return;
        }
        long phraseTime = this.mainPanel.playerPanel.getTime() + this.mainPanel.playerPanel.getSubtitleDelay();
        PhraseTranslator.Phrases phrase = subtitleFile.getPhraseAtTime(phraseTime);
        if (phrase == null) {
            return;
        }
        this.timeLabel.setText(subtitleFile.getStringTime(time));

        if (this.romanization.getText().compareTo(phrase.romanization) == 0) {
            return;
        }
        this.mainPanel.updateUI(phrase);
        this.romanization.setText(phrase.romanization);
        this.translateHoverListener.translation = phrase.translation;
        this.fullTranslate.setText("");
        this.fullForeign.setText(phrase.original);
        this.selectedTranslate.setText("");
//        pack();

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SPACE) {
            this.mainPanel.playerPanel.togglePause();
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            this.mainPanel.playerPanel.seek(e);
        } else if (key == KeyEvent.VK_V) {
            this.translateHoverListener.hover();
        } else if (key == KeyEvent.VK_G) {
            this.mainPanel.playerPanel.increaseSubtitleDelay(-100);
        } else if (key == KeyEvent.VK_H) {
            this.mainPanel.playerPanel.increaseSubtitleDelay(100);
        }
    }

    class TranslateHoverListener extends MouseAdapter {
        private final JTextField fullTranslate;
        public String translation;

        public TranslateHoverListener(JTextField fullTranslate) {
            this.fullTranslate = fullTranslate;
        }

        public void mouseEntered(java.awt.event.MouseEvent evt) {
            hover();
        }

        private void hover() {
            fullTranslate.setText(this.translation);
        }
    }

    public void positionWindow() {
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(0, y);
        this.setSize((int)dimension.getWidth(), (int) dimension.getHeight() / 2);
    }

    private class MainPanel extends JPanel {
        private final SubtitlesDatabase database;
        public final PlayerPanel playerPanel;
        private final ReadingPanel readingPanel;
        private final TranslationPanel translationPanel;

        public MainPanel(SubtitlesDatabase database, String mediaFile) {
            this.database = database;
            this.playerPanel = new PlayerPanel();
            this.readingPanel = new ReadingPanel();
            this.translationPanel = new TranslationPanel(this.database);

            this.setLayout(new BorderLayout());
            this.readingPanel.setPreferredSize(new Dimension(200, 800));
            this.translationPanel.setPreferredSize(new Dimension(250, 800));
            this.playerPanel.setPreferredSize(new Dimension(800, 800));
            this.add(this.readingPanel, BorderLayout.WEST);
            this.add(this.playerPanel, BorderLayout.CENTER);
            this.add(this.translationPanel, BorderLayout.EAST);
        }

        public void updateUI(PhraseTranslator.Phrases phrase) {
            this.readingPanel.updateUI(phrase);
            this.translationPanel.updateUI(phrase);

        }
    }

    private class ReadingPanel extends JPanel {
        public ReadingPanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        public void updateUI(PhraseTranslator.Phrases phrase) {
            this.removeAll();
            for (int i = 0; i < phrase.readingFormTokens.size(); ++i) {
                String readingForm = phrase.readingFormTokens.get(i);
                String foreignForm = phrase.foreignTokens.get(i);

                JTextField field = new JTextField(20);
                field.setText(readingForm);
                field.setFont(defaultFont);
                field.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        super.mouseEntered(e);
                        field.setText(foreignForm);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        super.mouseExited(e);
                        field.setText(readingForm);
                    }
                });
                this.add(field);
            }
        }
    }

    private class TranslationPanel extends JPanel {
        private final SubtitlesDatabase database;

        public TranslationPanel(SubtitlesDatabase database) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.database = database;
        }

        public void updateUI(PhraseTranslator.Phrases phrase) {
            this.removeAll();
            for (int i = 0; i < phrase.translatedTokens.size(); ++i) {
                JTextField translatedText = new JTextField(20);
                translatedText.setFont(defaultFont);

                String translated = phrase.translatedTokens.get(i);
                String foreign = phrase.foreignTokens.get(i);
                boolean shouldSkip = database.shouldSkip(foreign);

                if (shouldSkip) {
                    translatedText.setText(foreign);
                } else {
                    translatedText.setText(translated);
                }
                translatedText.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        translatedText.setText(translated);
                    }
                });

                Set<String> glosses = getGlosses(foreign);
                if (!glosses.isEmpty()) {
                    translatedText.setToolTipText("<html>" + String.join("<br>", glosses) + "</html>");
                }

                JButton storeButton = new JButton("store");
                storeButton.addActionListener((ActionEvent event) -> {
                    try {
                        database.storeSkipWord(foreign);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                Box  b = Box.createHorizontalBox();
                b.add(translatedText);
                b.add(storeButton);
                this.add(b);
            }
        }
    }



    private class PlayerPanel extends JFXPanel {
        private static final String NAME_VIDEO = "video";
//        private final CardLayout cardLayout;
        private String mediaFile;


        private ImageView imageView;
        private DirectMediaPlayerComponent mediaPlayerComponent;
        private WritableImage writableImage;
        private Pane playerHolder;
        private WritablePixelFormat<ByteBuffer> pixelFormat;
        private FloatProperty videoSourceRatioProperty;
        private long subtitleDelay = 0;
        public void togglePause() {
            this.mediaPlayerComponent.getMediaPlayer().setPause(this.mediaPlayerComponent.getMediaPlayer().isPlaying());
        }

        public long getTime() {
            return this.mediaPlayerComponent.getMediaPlayer().getTime();
        }

        public long getSubtitleDelay() {
            return this.subtitleDelay;
        }


        public void seek(KeyEvent e) {
            long seek = 500;
            if (e.getKeyCode()==KeyEvent.VK_LEFT) {
                seek = -500;
            }
            if (e.isShiftDown()) {
                seek *= 5;
            }
            if (e.isControlDown()) {
                seek *= 20;
            }
            if (e.isAltDown()) {
                seek *= 100;
            }
            this.mediaPlayerComponent.getMediaPlayer().skip(seek);
        }

        public void increaseSubtitleDelay(int delay) {
            this.subtitleDelay += delay;
            System.out.println("Subtitle delay: " + this.subtitleDelay);
        }


        private class CanvasBufferFormatCallback implements BufferFormatCallback {
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
                Platform.runLater(() -> videoSourceRatioProperty.set((float) sourceHeight / (float) sourceWidth));
                return new RV32BufferFormat((int) visualBounds.getWidth(), (int) visualBounds.getHeight());
            }
        }

        private class CanvasPlayerComponent extends DirectMediaPlayerComponent {

            public CanvasPlayerComponent() {
                super(new CanvasBufferFormatCallback());
            }

            PixelWriter pixelWriter = null;

            private PixelWriter getPW() {
                if (pixelWriter == null) {
                    pixelWriter = writableImage.getPixelWriter();
                }
                return pixelWriter;
            }

            @Override
            public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
                if (writableImage == null) {
                    return;
                }
                Platform.runLater(() -> {
                    Memory nativeBuffer = mediaPlayer.lock()[0];
                    try {
                        ByteBuffer byteBuffer = nativeBuffer.getByteBuffer(0, nativeBuffer.size());
                        getPW().setPixels(0, 0, bufferFormat.getWidth(), bufferFormat.getHeight(), pixelFormat, byteBuffer, bufferFormat.getPitches()[0]);
                    }
                    finally {
                        mediaPlayer.unlock();
                    }
                });
            }

            @Override
            protected String[] onGetMediaPlayerFactoryExtraArgs() {
                return new String[]{"--file-caching=10000"};
            }
        }

        PlayerPanel() {

//            cardLayout = new CardLayout();
//            setLayout(cardLayout);


            mediaPlayerComponent = new CanvasPlayerComponent();
            playerHolder = new Pane();
            videoSourceRatioProperty = new SimpleFloatProperty(0.4f);
            pixelFormat = PixelFormat.getByteBgraPreInstance();
            initializeImageView();
            Scene scene = new Scene(new BorderPane(playerHolder));
            this.setScene(scene);
            this.setBackground(Color.black);

            this.playerHolder.setOnMousePressed(event -> togglePause());
        }

        private void initializeImageView() {
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            writableImage = new WritableImage((int) visualBounds.getWidth(), (int) visualBounds.getHeight());

            imageView = new ImageView(writableImage);
            playerHolder.getChildren().add(imageView);

            playerHolder.widthProperty().addListener((observable, oldValue, newValue) -> {
                fitImageViewSize(newValue.floatValue(), (float) playerHolder.getHeight());
            });

            playerHolder.heightProperty().addListener((observable, oldValue, newValue) -> {
                fitImageViewSize((float) playerHolder.getWidth(), newValue.floatValue());
            });

            videoSourceRatioProperty.addListener((observable, oldValue, newValue) -> {
                fitImageViewSize((float) playerHolder.getWidth(), (float) playerHolder.getHeight());
            });

        }

        private void fitImageViewSize(float width, float height) {
            Platform.runLater(() -> {
                float fitHeight = videoSourceRatioProperty.get() * width;
                if (fitHeight > height) {
                    imageView.setFitHeight(height);
                    double fitWidth = height / videoSourceRatioProperty.get();
                    imageView.setFitWidth(fitWidth);
                    imageView.setX((width - fitWidth) / 2);
                    imageView.setY(0);
                }
                else {
                    imageView.setFitWidth(width);
                    imageView.setFitHeight(fitHeight);
                    imageView.setY((height - fitHeight) / 2);
                    imageView.setX(0);
                }
            });
        }

        public void play(String mediaFile) {
            mediaPlayerComponent.getMediaPlayer().prepareMedia(mediaFile);
            mediaPlayerComponent.getMediaPlayer().start();
        }

        public void playIfNecessary(String mediaFile) {
            if (this.mediaFile == null) {
                this.mediaFile = mediaFile;
                play(mediaFile);
            }
        }

    }
}
