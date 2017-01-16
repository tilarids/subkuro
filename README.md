# subkuro
Learn new languages by watching movies with semi-translated subtitles.

![Imgur](http://i.imgur.com/rPrSuM7.jpg)

## What it does now
* Splits phrases from ASS subtitles into words
* Translates every word separately
* Translates a whole phrase
* Puts everything back into the subtitle file
* Supported languages: Japanese -> English (unless I added some command line params)

## Ideas
* Write a simple Lua extension for VLC to publish current playing time
* Use this time to show current set of words with translations
* Make it possible to mark words as learned
* Make it possible to mark words as hard
* Make it possible to add exceptions to the way tokenization works
* Make it possible to cache and change word translations
* Maintain a database of such words and use it for the next translation run
