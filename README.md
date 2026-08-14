# Assemble Chinese Java Frontend

**Note:** you must initialize the sqlite database on the first run of this program. Use the `SQLite` menu's option `Initialize with IDS file` option. I got mine [here](https://babelstone.co.uk/CJK/IDS.HTML)

## Motivation
I am mostly self taught for writing Chinese. With English being my first language, I largely adapted the existing conventions I have for writing Chinese rather than learn the official rigid stroke order rules. This gives me a lot of grief with "non google translate" handwriting recognition that insist on official stroke order. Instead of trying to invent "unofficial hand writing recognition", Google Gemini informed me of an "ids" file that has disassemblies of characters to their parts.

## Intended Audience
This tool is not designed for absolute beginners or those with very little character recognition. Attempting to fully assemble a character from its base parts of - / | \ and others will be very painful and might not return any results. See the backend for more details, but each character is not broken down into its atomic parts in the database.

It is intended for people with moderate vocabulary size. This program works best when putting in "more fully assembled" parts vs primitive single strokes. 扮 = (left half of 打) + 分 works much better than 扮 = (left half 打) / \ 刀.

## Overview

This program is designed around traditional Chinese, but will work with both. The front end presents a list of common "parts" found in (traditional) Chinese characters. The parts are currently arranged in 4 rows. The top input box is where you put either the character you want disassembled into parts, or a list of parts you want assembled into possible characters. Press `enter` to search. The results are at the bottom row.

**Other note:** I am not an academic, or have any relation to linguistics. The variable names used all reflect, how someone with only an English background sees things.

## Assembly and Disassembly

The program has 2 modes: assembly and disassembly:
- disassembly: given a character you know, show the parts it is made of according to the db.
- assembly: given a list of parts in any order, show any Chinese character made of those parts.

Disassembly is intended to help when you can recognize parts of an unknown character. You input a character you do know, copy the part you want, then continue assembling.

### Shortcuts
Coming from and English background, the front end accepts the following substitution for parts:
|Shortcut | Actual Part| Example|
|---------|------------|--------|
|x (must be lower case)|㐅|x in the top of 產|
|++ (plus plus like c++)|艹|the top of 草 or the top right of 護|
|B (must be capital)|阝|left half of 陳|
|\| (unix pipe)|丨|(any vertical line)|
|\\ |丶|anything that looks like this|
|^|𠆢|top of 合|
|zigzag|幺|the 2 guys at the top half of 幾|


## UI Inspiratioin
The UI code is largely adapted from the dictionary program. I am not front end by trade, so it was designed with a "works good enough", and not to be beautiful. The Java Swing of the dictionary was entirely self taught for making the dictionary.

Because the IDS file uses custom unicode characters for the various parts, the babel stone fonts are included and automatically loaded by the front end. This avoids a "batteries not included" annoyance. It will work out of the box.

*Note:* this program has also not been tested on Windows. It has also not been tested on Mac OS either, but given the UI code largely comes from the dictionary, Mac OS shouldn't be a problem.

## How to Build
Checkout this repo and the assembly core repo in the same folder. Create `.vscode/settings` in the root folder with this repo and the core and adapt the following

```
{
  "java.project.sourcePaths": [
    "IdsCore/src",
    "JSEFrontEnd/src"
  ],
  "java.format.settings.url": ".vscode/java-formatter.xml",
  "java.project.referencedLibraries": [
    "lib/**/*.jar",
    "sqlite-jdbc-3.53.2.0.jar"
  ]
}
```

Use VSCodium's/VSCode's Java export feature. Make sure to include the fonts, and the sqlite jdbc library.