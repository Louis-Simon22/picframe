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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.PageTransformer;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import picframe.at.picframe.R;
import picframe.at.picframe.utils.FileUtils;
import picframe.at.picframe.utils.transformers.AccordionTransformer;
import picframe.at.picframe.utils.transformers.BackgroundToForegroundTransformer;
import picframe.at.picframe.utils.transformers.CubeOutTransformer;
import picframe.at.picframe.utils.transformers.CustomViewPager;
import picframe.at.picframe.utils.transformers.DrawFromBackTransformer;
import picframe.at.picframe.utils.EXIFUtils;
import picframe.at.picframe.utils.transformers.FadeInFadeOutTransformer;
import picframe.at.picframe.utils.transformers.FlipVerticalTransformer;
import picframe.at.picframe.utils.transformers.ForegroundToBackgroundTransformer;
import picframe.at.picframe.utils.Gestures;
import picframe.at.picframe.utils.transformers.RotateDownTransformer;
import picframe.at.picframe.utils.transformers.StackTransformer;
import picframe.at.picframe.utils.transformers.ZoomInTransformer;
import picframe.at.picframe.utils.transformers.ZoomOutPageTransformer;
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

    private class ImagePagerAdapter extends PagerAdapter {
        private final LayoutInflater inflater;
        private int localPage;

        public ImagePagerAdapter(Activity activity) {
            this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            updateSettings();
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            View viewLayout = inflater.inflate(R.layout.fullscreen_layout, container, false);

            ImageView imgDisplay = viewLayout.findViewById(R.id.photocontainer);

            if (AppData.getScaling(getApplicationContext())) {
                imgDisplay.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imgDisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            this.localPage = position;
            if (!showExamplePictures) {
                imgDisplay.setImageBitmap(EXIFUtils.decodeFile(mFilePaths.get(this.localPage), getApplicationContext()));
            } else {
                String currentImage = "ex" + this.localPage;
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
                    hideActionBar();
                }

                @Override
                public void onTap() {
                    showActionBar();
                }
            });
            container.addView(viewLayout);
            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
            container.removeView((RelativeLayout) object);
        }

        private void updateSettings() {
            mFilePaths = FileUtils.getFileList(getApplicationContext(), AppData.getImagePath(getApplicationContext()));
            setSize();
        }

        public int getPage() {
            return this.localPage;
        }
    }

    public final static int APP_STORAGE_ACCESS_REQUEST_CODE = 501;
    private final static boolean DEBUG = true;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int nbOfExamplePictures = 6;
    private static final int ACTION_BAR_SHOW_DURATION = 5000;
    private static boolean showExamplePictures = false;

    private ImagePagerAdapter imagePagerAdapter;
    private CustomViewPager pager;
    private Timer slideshowTimer;

    private String mOldPath;
    private boolean paused;

    private final PageTransformer[] TRANSFORMERS = new PageTransformer[]{new ZoomOutPageTransformer(), new AccordionTransformer(), new BackgroundToForegroundTransformer(), new CubeOutTransformer(), new DrawFromBackTransformer(), new FadeInFadeOutTransformer(), new FlipVerticalTransformer(), new ForegroundToBackgroundTransformer(), new RotateDownTransformer(), new StackTransformer(), new ZoomInTransformer(), new ZoomOutPageTransformer(),};
    private List<String> mFilePaths;
    private int size;
    private int currentPage;
    private Handler actionbarHideHandler;
    public boolean mDoubleBackToExitPressedOnce;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paused = false;
        hideActionBar();
        pager = findViewById(R.id.pager);
        loadAdapter();
        setUpSlideShow();

        mOldPath = AppData.getImagePath(getApplicationContext());

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
        if (Objects.requireNonNull(pager.getAdapter()).getCount() < currentPage) {
            currentPage = 1;
        }
        AppData.getSharedPreferences(this).registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (key != null) {
                if (key.equals(getString(R.string.sett_key_transition))
                        || key.equals(getString(R.string.sett_key_randomize))
                        || key.equals(getString(R.string.sett_key_srcpath_sd))
                        || key.equals(getString(R.string.sett_key_scaling))
                ) {
                    startSlideshow();
                }
            }
        });
    }

    private void setupTimer() {
        if (slideshowTimer == null) {
            slideshowTimer = new Timer();
        }
        SlideShowTimerTask slideShowTimerTask = new SlideShowTimerTask(new WeakReference<>(this));
        int displayTimeInMillis = AppData.getDisplayTime(this) * 1000;
        debug("Display time: " + displayTimeInMillis);
        slideshowTimer.schedule(slideShowTimerTask, displayTimeInMillis, displayTimeInMillis);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        startActivity(new Intent(this, SettingsActivity.class));
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupTimer();

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
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
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

        slideshowTimer.cancel();
        slideshowTimer = null;

        mOldPath = AppData.getImagePath(getApplicationContext());

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        currentPage = pager.getCurrentItem();
    }

    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
        } else {
            this.mDoubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.main_toast_exitmsg, Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> mDoubleBackToExitPressedOnce = false, 2000);
        }
    }

    public void showActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            actionBar.show();
        }
        if (actionbarHideHandler != null) {
            actionbarHideHandler.removeCallbacksAndMessages(null);
            actionbarHideHandler = null;
        }
        actionbarHideHandler = new Handler(Looper.getMainLooper());
        actionbarHideHandler.postDelayed(this::hideActionBar, ACTION_BAR_SHOW_DURATION);
    }

    private void hideActionBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void nextSlideshowPage() {
        if (imagePagerAdapter.getCount() > 0 && !paused) {
            int localpage = pager.getCurrentItem();
            localpage++;
            // We loop when reaching the end
            if (localpage == imagePagerAdapter.getCount()) {
                localpage = 0;
            }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_STORAGE_ACCESS_REQUEST_CODE && Environment.isExternalStorageManager()) {
            startSlideshow();
        }
    }

    private void setUpSlideShow() {
        pager.setScrollDurationFactor(8);
    }

    private void startSlideshow() {
        if (!FileUtils.getFileList(getApplicationContext(), AppData.getImagePath(getApplicationContext())).isEmpty()) {
            if (!AppData.getImagePath(getApplicationContext()).equals(mOldPath)) {
                loadAdapter();
            }
        }

        updateFileList();

        // start on the page we left in onPause, unless it was the first or last picture (as this freezes the slideshow)
        if (currentPage < Objects.requireNonNull(pager.getAdapter()).getCount() - 1 && currentPage > 0) {
            pager.setCurrentItem(currentPage);
        }

        setUpSlideShow();
    }

    public void updateFileList() {
        if (AppData.getImagePath(getApplicationContext()).isEmpty()) {
            showExamplePictures = true;
            Toast.makeText(getApplicationContext(), R.string.main_toast_noFolderPathSet, Toast.LENGTH_SHORT).show();
        } else {
            mFilePaths = FileUtils.getFileList(getApplicationContext(), AppData.getImagePath(getApplicationContext()));
            showExamplePictures = mFilePaths.isEmpty();
            if (showExamplePictures) {
                Toast.makeText(getApplicationContext(), R.string.main_toast_noFileFound, Toast.LENGTH_SHORT).show();
            }
        }
        setSize(); // size is count of images in folder, or constant if example pictures are used
        imagePagerAdapter.notifyDataSetChanged();
    }

    private void loadAdapter() {
        imagePagerAdapter = new ImagePagerAdapter(this);
        pager.setAdapter(imagePagerAdapter);
        currentPage = imagePagerAdapter.getPage();
    }

    public void selectTransformer() {
        int[] transitionTypeValues = getResources().getIntArray(R.array.transitionTypeValues);
        int transitionStyleIndex = AppData.getTransitionStyle(getApplicationContext());
        // If the style is the random style, we cycle randomly select another style
        if (transitionStyleIndex == transitionTypeValues[transitionTypeValues.length - 1]) {
            transitionStyleIndex = transitionTypeValues[(int) (Math.random() * (transitionTypeValues.length - 1))];
        }
        pager.setPageTransformer(true, TRANSFORMERS[transitionStyleIndex]);
    }

    private void setSize() {
        if (!showExamplePictures) {
            size = mFilePaths.size();
        } else {
            size = nbOfExamplePictures;
        }
    }


    private void debug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}

