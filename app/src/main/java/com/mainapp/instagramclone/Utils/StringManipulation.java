package com.mainapp.instagramclone.Utils;

public class StringManipulation {
    public static String expandUsername(String username) {
        return username.replace(".", " ");
    }

    public static String condenseUsername(String username) {
        return username.replace(" ", ".");
    }

    public static String getTags(String string) {
        if (string.indexOf("#") > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] chars = string.toCharArray();
            boolean foundWord = false;
            for (char c: chars) {
                if (c == '#') {
                    foundWord = true;
                    stringBuilder.append(c);
                } else if (foundWord) {
                    stringBuilder.append(c);
                }

                if (c == ' ') {
                    foundWord = false;
                }
            }
            String s = stringBuilder.toString().replace(" ", "").replace("#", ",#");
            return s.substring(1);
        }
        return string;
    }
}
