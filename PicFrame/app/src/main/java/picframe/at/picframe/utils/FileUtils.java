/*
    Copyright (C) 2015 Myra Fuchs, Linda Spindler, Clemens Hlawacek, Ebenezer Bonney Ussher

    This file is part of PicFrame.

    PicFrame is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PicFrame is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PicFrame.  If not, see <http://www.gnu.org/licenses/>.package picframe.at.picframe.activities;
*/

package picframe.at.picframe.utils;

import android.content.Context;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import picframe.at.picframe.settings.AppData;

public class FileUtils {
    private static final List<String> ALLOWED_EXTS = Arrays.asList("jpg", "jpeg", "png");

    private static FilenameFilter getAllowedExtsFilter() {
        return (dir, filename) -> {
            File tempfile = new File(dir.getAbsolutePath() + File.separator + filename);
            filename = filename.substring((filename.lastIndexOf(".") + 1));

            if (ALLOWED_EXTS.contains(filename.toLowerCase())) {
                return true;
            }

            return tempfile.isDirectory();
        };
    }

    // returns a List with all files in given directory
    public static List<String> getFileList(Context context, String path) {
        List<String> fileArray = readSdDirectory(path);
        if (fileArray.isEmpty()) return fileArray;
        if (AppData.getRandomize(context)) {
            Collections.shuffle(fileArray);
        } else {
            Collections.sort(fileArray);
        }
        return fileArray;
    }

    private static List<String> readSdDirectory(String path) {
        File folder = new File(path);
        List<String> fileArray = new ArrayList<>();
        File[] files = folder.listFiles(getAllowedExtsFilter());
        if (files == null) {
            return new ArrayList<>();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                fileArray.addAll(readSdDirectory(file.toString()));
            } else {
                fileArray.add(file.getAbsolutePath());
            }
        }
        return fileArray;
    }

}