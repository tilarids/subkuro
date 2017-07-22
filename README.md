# subkuro
Learn new languages by watching movies with semi-translated subtitles.


## Subtitle translator

![Imgur](http://i.imgur.com/rPrSuM7.jpg)

* Splits phrases from ASS subtitles into words
* Translates every word separately using Google Translate
* Translates a whole phrase
* Puts everything back into the subtitle file
* Supported languages: should be anything, I've checked that it works for Japanese -> English and English -> Russian.

## Enhanced video player for translated subtitles

![Imgur](http://i.imgur.com/PfUpOPf.jpg)

* Loads the translated subtitles and displays them alongside the video
* It's possible to mark words as learned - those words will not be translated
* Hovering over the words reveal their translation or their original
* JMDict is supported. Hover over the words to see a tooltip with JMDict translation
* Whole phrase translation is hidden by default - you need to hover over the text field to reveal it
* You can select parts of the original and use JMDict or Google Translate to translate it

## Ideas
* Make it possible to mark words as hard
* Make it possible to add exceptions to the way tokenization works
* Make it possible to cache and change word translations
* Maintain a database of such words and use it for the next translation run
