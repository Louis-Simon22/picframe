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

package picframe.at.picframe.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import picframe.at.picframe.settings.AppData;

public class GlobalPhoneFuncs {
    private static List<String> allowedExts = Arrays.asList("jpg", "jpeg", "png");

    private static FilenameFilter getAllowedExtsFilter(Context context) {
        return (dir, filename) -> {
            File tempfile = new File(dir.getAbsolutePath() + File.separator + filename);
            filename = filename.substring((filename.lastIndexOf(".") + 1));

            if (allowedExts.contains(filename.toLowerCase())) {
                return true;
            }
            boolean greg = AppData.getRecursiveSearch(context) && tempfile.isDirectory();
            return AppData.getRecursiveSearch(context) && tempfile.isDirectory();
        };
    }

    // returns a List with all files in given directory
    public static List<String> getFileList(Context context, String path) {
        List<String> fileArray = readSdDirectory(context, path);
        if (fileArray.isEmpty()) return fileArray;
        if (AppData.getRandomize(context)) {
            Collections.shuffle(fileArray);
        } else {
            Collections.sort(fileArray);
        }
        return fileArray;
    }

    private static List<String> readSdDirectory(Context context, String path) {
        File folder = new File(path);
        List<String> fileArray = new ArrayList<>();
        File[] files = folder.listFiles(getAllowedExtsFilter(context));
        if (files == null) {
            return new ArrayList<>();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                fileArray.addAll(readSdDirectory(context, file.toString()));
            } else {
                fileArray.add(file.getAbsolutePath());
            }
        }
        return fileArray;
    }

    // Checks whether the given directory has allowed files
    public static boolean hasAllowedFiles(Context context) {
        List<String> files = readSdDirectory(context, AppData.getImagePath(context));
        return !(files.isEmpty());
    }

    // Checks if external storage is available for read and write
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Checks if external storage is available to at least read
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static boolean wifiConnected(Context context) {
        NetworkInfo wifi = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected();
    }

    public static void recursiveDeletionInBackgroundThread(final File directory, final boolean deleteRoot) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                recursiveDelete(directory, deleteRoot);
            }
        });
    }

    private static boolean recursiveDelete(File dir, boolean delRoot) {       // for directories
        if (dir.exists()) {
            File[] list = dir.listFiles();
            for (File file : list) {
                if (file.isDirectory()) {
                    recursiveDelete(new File(file.getAbsolutePath()), true);
                } else {
                    if (!file.delete()) {
                        System.err.println("RecursiveDelete | Couldn't delete >" + file.getName() + "<");
                    }
                }
            }
            if (delRoot) {
                return dir.delete();
            }
        }
        // Comment to remove warning
        return true;
    }
}