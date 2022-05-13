// 13.05.2022 - Reviewed. All Done.
package com.mainapp.instagramclone.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class FileSearch {
    public static ArrayList<String> getDirectoryPaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for (int i = 0; i < Objects.requireNonNull(listFiles).length; i++) {
            if (listFiles[i].isDirectory())
                pathArray.add(listFiles[i].getAbsolutePath());
        }
        return pathArray;
    }

    public static ArrayList<String> getFilePaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();
        for (int i = 0; i < Objects.requireNonNull(listFiles).length; i++) {
            if (listFiles[i].isFile())
                pathArray.add(listFiles[i].getAbsolutePath());
        }
        return pathArray;
    }
}
