package com.louissimonmcnicoll.simpleframe.utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by linda on 31.12.2015.
 */
public class SDCardHelper {
    File mnt;
    File roots[];

    public SDCardHelper() {
        mnt = new File("/storage");
        if (!mnt.exists()) {
            mnt = new File("/mnt");
        }
    }

    public String getExteralStoragePath() {
        String path;
        // recompute path each time since sd-card could have been mounted/unmounted
        // while app was running

        // If there's one internal and one or several external SD-cards,
        // return the path the an external sd-card (we assume this is the *last* sd-card).

        // If there's only one internal SD-card, return path to it.

        // If there are no sd-cards, return path to external_storage
        if (hasSDCard()) { // hasSDCard also fill in the values of root if needed
            // Compute full list for debugging purposes only
            ArrayList<String> list = new ArrayList<>();
            for (File root : roots) {
                list.add(root.getAbsolutePath());
            }
            path = list.get(list.size() - 1);
        } else {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return path;
    }

    private boolean hasSDCard() {
        if (!mnt.exists())
            return false;
        roots = mnt.listFiles(pathname -> pathname.isDirectory() && pathname.exists()
                && pathname.canWrite() && !pathname.isHidden());
        if (roots == null || roots.length == 0) {
            System.out.println("no roots!");
            return false;
        }
        return true;
    }
}
