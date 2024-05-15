/*
   Copyright (C) 2015 Myra Fuchs, Linda Spindler, Clemens Hlawacek, Ebenezer Bonney Ussher,
   Martin Bayerl, Christoph Krasa

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
   along with PicFrame.  If not, see <http://www.gnu.org/licenses/>.
*/

package picframe.at.picframe.activities;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.PageTransformer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import picframe.at.picframe.R;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.viewpager.AccordionTransformer;
import picframe.at.picframe.helper.viewpager.BackgroundToForegroundTransformer;
import picframe.at.picframe.helper.viewpager.CubeOutTransformer;
import picframe.at.picframe.helper.viewpager.CustomViewPager;
import picframe.at.picframe.helper.viewpager.DrawFromBackTransformer;
import picframe.at.picframe.helper.viewpager.EXIF_helper;
import picframe.at.picframe.helper.viewpager.FadeInFadeOutTransformer;
import picframe.at.picframe.helper.viewpager.FlipVerticalTransformer;
import picframe.at.picframe.helper.viewpager.ForegroundToBackgroundTransformer;
import picframe.at.picframe.helper.viewpager.Gestures;
import picframe.at.picframe.helper.viewpager.RotateDownTransformer;
import picframe.at.picframe.helper.viewpager.StackTransformer;
import picframe.at.picframe.helper.viewpager.ZoomInTransformer;
import picframe.at.picframe.helper.viewpager.ZoomOutPageTransformer;
import picframe.at.picframe.settings.AppData;

public class MainActivity extends AppCompatActivity {

    private static class SlideShowTimerTask extends TimerTask {
        private final WeakReference<MainActivity> mainActivityWeakReference;
        private final Handler mainThreadHandler;

        private SlideShowTimerTask(WeakReference<MainActivity> mainActivityWeakReference) {
            this.mainActivityWeakReference = mainActivityWeakReference;
            this.mainThreadHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void run() {
            MainActivity mainActivity = mainActivityWeakReference.get();
            if (mainActivity == null) {
                cancel();
            } else {
                mainThreadHandler.post(mainActivity::nextSlideshowPage);
            }
        }
    }

    private final static boolean DEBUG = true;
    final static int APP_STORAGE_ACCESS_REQUEST_CODE = 501;
    private static final String TAG = MainActivity.class.getSimpleName();

    private ImagePagerAdapter imagePagerAdapter;
    private CustomViewPager pager;
    private static final long slideshowIntervalInMilliseconds = 5 * 1000;
    private Timer slideshowTimer;

    private String mOldPath;
    private boolean mOldRecursive;
    private RelativeLayout mainLayout;
    private boolean paused;

    private static final int nbOfExamplePictures = 6;
    private static boolean showExamplePictures = false;

    private ArrayList<PageTransformer> transformers;
    private List<String> mFilePaths;
    private int size;
    private int currentPage;
    private Handler actionbarHideHandler;

    public boolean mDoubleBackToExitPressedOnce;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slideshowTimer = new Timer();
        SlideShowTimerTask slideShowTimerTask = new SlideShowTimerTask(new WeakReference<>(this));
        slideshowTimer.schedule(slideShowTimerTask, slideshowIntervalInMilliseconds, slideshowIntervalInMilliseconds);

        mainLayout = findViewById(R.id.mainLayout);
        paused = false;
        enableGestures();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initializeTransitions();
        pager = findViewById(R.id.pager);
        loadAdapter();
        setUpSlideShow();

        mOldPath = AppData.getImagePath(getApplicationContext());
        mOldRecursive = AppData.getRecursiveSearch(getApplicationContext());

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectTransformer();
            }
        });

        currentPage = AppData.getCurrentPage(getApplicationContext());
        if (pager.getAdapter().getCount() < currentPage) {
            currentPage = 1;
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        startActivity(new Intent(this, SettingsActivity.class));
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // refresh toolbar options (hide/show downloadNow)
        supportInvalidateOptionsMenu();
        if (AppData.getFirstAppStart(getApplicationContext())) {
            AppData.setFirstAppStart(getApplicationContext(), false);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent;
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            myIntent = new Intent(this, SettingsActivity.class);
        } else {
            return super.onOptionsItemSelected(item);
        }
        startActivity(myIntent);
        return true;
    }

    protected void onPause() {
        super.onPause();
        mOldPath = AppData.getImagePath(getApplicationContext());
        mOldRecursive = AppData.getRecursiveSearch(getApplicationContext());

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        currentPage = pager.getCurrentItem();
    }

    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
        } else {
            this.mDoubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.main_toast_exitmsg, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDoubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    public void showActionBar() {
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().show();
        }
        if (actionbarHideHandler != null) {
            actionbarHideHandler.removeCallbacksAndMessages(null);
            actionbarHideHandler = null;
        }
        actionbarHideHandler = new Handler();
        actionbarHideHandler.postDelayed(() -> getSupportActionBar().hide(), 2500);
    }

    private void nextSlideshowPage() {
        if (imagePagerAdapter.getCount() > 0 && !paused) {
            int localpage = pager.getCurrentItem();
            localpage++;
            pager.setCurrentItem(localpage, true);
            debug("localpage " + localpage);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Save the current Page to resume after next start
        // maybe not right here will test
        AppData.setCurrentPage(getApplicationContext(), currentPage);
        debug("SAVING PAGE  " + currentPage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        slideshowTimer.purge();
    }

    private class ImagePagerAdapter extends PagerAdapter {
        //        private List<String> mFilePaths;
        private Activity activity;
        private LayoutInflater inflater;
        private ImageView imgDisplay;
        private int localpage;
//        private int size;

        public ImagePagerAdapter(Activity activity) {
            this.activity = activity;
            updateSettings();
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.fullscreen_layout, container, false);

            imgDisplay = (ImageView) viewLayout.findViewById(R.id.photocontainer);

            if (AppData.getScaling(getApplicationContext())) {
                imgDisplay.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imgDisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            this.localpage = position;
            if (!showExamplePictures) {
                imgDisplay.setImageBitmap(EXIF_helper.decodeFile(mFilePaths.get(this.localpage), getApplicationContext()));
            } else {
                String currentImage = "ex" + this.localpage;
                int currentImageID = getApplicationContext().getResources().getIdentifier(currentImage, "drawable", getApplicationContext().getPackageName());
                imgDisplay.setImageResource(currentImageID);
            }
            imgDisplay.setOnTouchListener(new Gestures(getApplicationContext()) {
                @Override
                public void onSwipeBottom() {
                    showActionBar();
                }

                @Override
                public void onSwipeTop() {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().hide();
                    }
                }

                @Override
                public void onTap() {
                    if (AppData.getSlideshow(getApplicationContext())) {
                        showActionBar();
                    }
                }
            });
            container.addView(viewLayout);
            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }

        private void updateSettings() {
            mFilePaths = GlobalPhoneFuncs.getFileList(getApplicationContext(), AppData.getImagePath(getApplicationContext()));
            //setUp.notifyDataSetChanged();
            setSize();
        }

        public int getPage() {
            return this.localpage;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_STORAGE_ACCESS_REQUEST_CODE && Environment.isExternalStorageManager()) {
            startSlideshow();
        } else {
            // TODO show toast saying we can't do anything
        }
    }

    private void setUpSlideShow() {
        if (AppData.getSlideshow(getApplicationContext())) {
            pager.setScrollDurationFactor(8);
//            pager.setPagingEnabled(false);
        } else {
            pager.setScrollDurationFactor(3);
//            pager.setPagingEnabled(true);
        }
    }

    private void startSlideshow() {
        if (!GlobalPhoneFuncs.getFileList(getApplicationContext(), AppData.getImagePath(getApplicationContext())).isEmpty()) {
            if (!AppData.getImagePath(getApplicationContext()).equals(mOldPath) || mOldRecursive != AppData.getRecursiveSearch(getApplicationContext())) {
                loadAdapter();
            }
        }

        updateFileList();

        // start on the page we left in onPause, unless it was the first or last picture (as this freezes the slideshow)
        if (currentPage < pager.getAdapter().getCount() - 1 && currentPage > 0) {
            pager.setCurrentItem(currentPage);
        }

        setUpSlideShow();
    }

    public void updateFileList() {
        if (AppData.getImagePath(getApplicationContext()).equals("")) {
            showExamplePictures = true;
            Toast.makeText(getApplicationContext(), R.string.main_toast_noFolderPathSet, Toast.LENGTH_SHORT).show();
        } else {
            mFilePaths = GlobalPhoneFuncs.getFileList(getApplicationContext(), AppData.getImagePath(getApplicationContext()));
            showExamplePictures = mFilePaths.isEmpty() || mFilePaths.size() <= 0;
            if (showExamplePictures) {
                Toast.makeText(getApplicationContext(), R.string.main_toast_noFileFound, Toast.LENGTH_SHORT).show();
            }
        }
        setSize(); // size is count of images in folder, or constant if example pictures are used
        imagePagerAdapter.notifyDataSetChanged();
    }

    private void loadAdapter() {
        imagePagerAdapter = new ImagePagerAdapter(MainActivity.this);
        try {
            pager.setAdapter(imagePagerAdapter);
            currentPage = imagePagerAdapter.getPage();
        } catch (Exception e) {
            Log.e("Image adapter error", Log.getStackTraceString(e));
        }
    }

    private void enableGestures() {
        mainLayout.setOnTouchListener(new Gestures(getApplicationContext()) {
            @Override
            public void onSwipeBottom() {
                showActionBar();
            }

            @Override
            public void onSwipeTop() {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            }
        });
    }

    public void selectTransformer() {
        if (AppData.getSlideshow(getApplicationContext()) && AppData.getTransitionStyle(getApplicationContext()) == 11) {
            pager.setPageTransformer(true, transformers.get(random()));
        } else if (AppData.getSlideshow(getApplicationContext())) {
            pager.setPageTransformer(true, transformers.get(AppData.getTransitionStyle(getApplicationContext())));
        } else {
            pager.setPageTransformer(true, new ZoomOutPageTransformer());
        }
    }

    private int random() {
        //Random from 0 to 13
        return (int) (Math.random() * 11);
    }

    private void setSize() {
        if (!showExamplePictures) size = mFilePaths.size();
        else size = nbOfExamplePictures;
    }

    private void initializeTransitions() {
        transformers = new ArrayList<>();
        this.transformers.add(new AccordionTransformer());
        this.transformers.add(new BackgroundToForegroundTransformer());
        this.transformers.add(new CubeOutTransformer());
        this.transformers.add(new DrawFromBackTransformer());
        this.transformers.add(new FadeInFadeOutTransformer());
        this.transformers.add(new FlipVerticalTransformer());
        this.transformers.add(new ForegroundToBackgroundTransformer());
        this.transformers.add(new RotateDownTransformer());
        this.transformers.add(new StackTransformer());
        this.transformers.add(new ZoomInTransformer());
        this.transformers.add(new ZoomOutPageTransformer());
    }


    private void debug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}

