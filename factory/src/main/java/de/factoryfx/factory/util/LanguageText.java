package de.factoryfx.factory.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageText {
    private HashMap<Locale, String> texts = new HashMap<>();

    public void en(String text) {
        put(Locale.ENGLISH,text);
    }

    public void de(String text) {
        put(Locale.GERMAN,text);
    }

    /** get the text in the locale if possible else rerturn the text in a random(probably ony english) locale or null if no text avaible*/
    public String getPreferred(Locale locale) {
        String text = texts.get(locale);
        if (text==null){
            for (Map.Entry<Locale,String> entry: texts.entrySet()){
                return entry.getValue();
            }
        }
        return text;
    }

    public void put(Locale locale, String text) {
        texts.put(locale, text);
    }
}