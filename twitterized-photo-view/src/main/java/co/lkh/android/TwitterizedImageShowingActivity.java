
package co.lkh.android;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.SimpleTarget;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.ImageLoader;
import com.github.piasy.biv.view.BigImageView;

import java.io.File;

import co.lkh.android.util.PaletteUtil;

import static android.R.string.no;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class TwitterizedImageShowingActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {
    public final static String ARGS_IMAGE_URL = "ARGS_IMAGE_URL";
    public final static String ARGS_IMAGE_BITMAP = "ARGS_IMAGE_BITMAP";
    public final static String ARGS_TRANSITION_NAME = "ARGS_TRANSITION_NAME";
    public final static String ARGS_TRANSITION_BEFORE_BACKGROUND_COLOR
        = "ARGS_TRANSITION_BEFORE_BACKGROUND_COLOR";
    public final static String ARGS_BOTTOM_MENU_RES_ID = "ARGS_BOTTOM_MENU_RES_ID";
    public final static String ARGS_BOTTOM_REMOTE_VIEWS = "ARGS_BOTTOM_REMOTE_VIEWS";

    public final static String ACTION_NAVIGATION_CLICKED = "ACTION_NAVIGATION_CLICKED";
    public final static String EXTRA_NAVIGATION_ITEM_ID = "EXTRA_NAVIGATION_ITEM_ID";
    public final static String EXTRA_NAVIGATION_ITEM_TITLE = "EXTRA_NAVIGATION_ITEM_TITLE";

    private final static int TRANSITION_DURATION = 200;
    private final static int BACKGROUND_TRANSITION_DURATION = 200;
    private final static int BACKGROUND_COLOR_NAVIGATION_ALPHA = 127;
    private final static int FADE_IN_OUT_DURATION = 300;
    private final static int INVALID_INTEGER = -1;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Nullable
    BigImageView imageView;
    @Nullable
    ViewGroup galleryImageViewLayout;
    @Nullable
    GestureDetectorCompat gestureDetector;
    @Nullable
    Toolbar toolbar;
    @Nullable
    BottomNavigationView bottomNavigationView;

    boolean gallerySystemUiToggle = true;
    int beforeTransitBackgroundColor = Color.WHITE;
    int currentBackgroundColor = Color.BLACK;

    @Nullable
    String transitionName;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force trigger System UI cover on this Activity
        hideSystemUI();
        showSystemUI();

        // Avoid flash while transition from former Activity
        postponeEnterTransition();
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);

        // Using the fade effect for Activity transition
        Fade fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(R.id.showing_image_view, true);
        fade.setDuration(TRANSITION_DURATION);
        applyTransitionToWindow(getWindow(), fade, true, true, true, true, true);

        BigImageViewer.initialize(new CustomImageLoader(this.getApplicationContext()));
        setContentView(R.layout.activity_twitterized_image_showing);

        setupViews();
    }

    private void setupViews() {
        imageView = findViewById(R.id.showing_image_view);
        scheduleStartPostponedTransition(imageView);

        galleryImageViewLayout = findViewById(R.id.showing_image_view_layout);
        bottomNavigationView = findViewById(R.id.navigation_view);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)
                bottomNavigationView.getLayoutParams();
        params.setMargins(0, 0, 0, getNavigationBarHeight());
        bottomNavigationView.setLayoutParams(params);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        final Drawable upArrow = ContextCompat.getDrawable(this,
                android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff
                .Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        params = (ViewGroup.MarginLayoutParams)
                toolbar.getLayoutParams();
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        toolbar.setLayoutParams(params);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            transitionName = bundle.getString(ARGS_TRANSITION_NAME);
            beforeTransitBackgroundColor
                    = bundle.getInt(ARGS_TRANSITION_BEFORE_BACKGROUND_COLOR, Color.WHITE);
            int menuResId = bundle.getInt(ARGS_BOTTOM_MENU_RES_ID, INVALID_INTEGER);
            final RemoteViews remoteViews = bundle.getParcelable(ARGS_BOTTOM_REMOTE_VIEWS);
            final String url = bundle.getString(ARGS_IMAGE_URL);
            final Bitmap bitmap = bundle.getParcelable(ARGS_IMAGE_BITMAP);

            if (menuResId != INVALID_INTEGER) {
                bottomNavigationView.inflateMenu(menuResId);
            } else {
                bottomNavigationView.setEnabled(false);
                bottomNavigationView.setVisibility(View.GONE);

                if (remoteViews != null) {
                    View view = remoteViews.apply(this, galleryImageViewLayout);
                    galleryImageViewLayout.addView(view);
                }
            }

            imageView.setTransitionName(transitionName);
            if (bitmap != null) {
//                imageView.setImageBitmap(bitmap);

                // Push the task to the end of queue that could avoid stuck the main thread
                // at the moment.
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        extractAndApplyColor(bitmap);
                    }
                });
            } else if (url != null) {
                imageView.showImage(Uri.parse(url));
            }
            else {
                throw new IllegalArgumentException("lack needed arguments");
            }
        }

        galleryImageViewLayout.setClickable(true);
        galleryImageViewLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        gestureDetector = new GestureDetectorCompat(this,
            new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (galleryImageViewLayout != null) {
                    if (gallerySystemUiToggle) {
                        hideSystemUI();
                        hideAppNavigationBar();
                    }
                    else {
                        showSystemUI();
                        showAppNavigationBar();
                    }
                    gallerySystemUiToggle = !gallerySystemUiToggle;
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return false;
            }
        });
    }

    private int getNavigationBarHeight() {
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return  resources.getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    private int getStatusBarHeight() {
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return  resources.getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    void extractAndApplyColor(@NonNull final Bitmap bitmap) {
        // Change the background with prominent color from the image
        currentBackgroundColor = extractColor(bitmap);
        applyViewTransitBackgroundColor(galleryImageViewLayout,
                beforeTransitBackgroundColor, currentBackgroundColor);

//        int semiTransparentColor =
//                currentBackgroundColor | (BACKGROUND_COLOR_NAVIGATION_ALPHA & 0xff) << 24;
//        toolbar.setBackgroundColor(semiTransparentColor);
//        bottomNavigationView.setBackgroundColor(semiTransparentColor);
    }

    /**
     * Schedules the shared element transition to be started immediately
     * after the shared element has been measured and laid out within the
     * activity's view hierarchy.
     */
    void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                    ActivityCompat.startPostponedEnterTransition(TwitterizedImageShowingActivity.this);
                    return true;
                }
            });
    }

    void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    Animator.AnimatorListener hideListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            bottomNavigationView.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    Animator.AnimatorListener showListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animator) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animator) {
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    void hideAppNavigationBar() {
        bottomNavigationView.animate().alpha(0f).setDuration(FADE_IN_OUT_DURATION);
        toolbar.animate().alpha(0f).setDuration(FADE_IN_OUT_DURATION).setListener(hideListener);
        bottomNavigationView.setEnabled(false);
        bottomNavigationView.setClickable(false);
        toolbar.setEnabled(false);
    }

    void showAppNavigationBar() {
        bottomNavigationView.animate().alpha(1f).setDuration(FADE_IN_OUT_DURATION);
        toolbar.animate().alpha(1f).setDuration(FADE_IN_OUT_DURATION).setListener(showListener);
        bottomNavigationView.setEnabled(true);
        bottomNavigationView.setClickable(true);
        toolbar.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (galleryImageViewLayout != null) {
            gallerySystemUiToggle = true;
            showSystemUI();
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        }
    }

    static int extractColor(@NonNull final Bitmap bitmap) {
        return PaletteUtil.extractProminentDarkerColor(bitmap);
    }

    static void applyTransitionToWindow(@NonNull final Window window,
                                        @NonNull final Transition transition,
                                        boolean enter, boolean retun,
                                        boolean reenter, boolean exit,
                                        boolean overlap) {
        window.setAllowEnterTransitionOverlap(overlap);
        window.setAllowReturnTransitionOverlap(overlap);
        if (enter) {
            window.setEnterTransition(transition);
        }
        if (reenter) {
            window.setReenterTransition(transition);
        }
        if (exit) {
            window.setExitTransition(transition);
        }
        if (retun) {
            window.setReturnTransition(transition);
        }
    }

    static void applyViewTransitBackgroundColor(@NonNull View view, int beforeColor,
                                                int afterColor) {
        ColorDrawable[] colors = {new ColorDrawable(beforeColor),
            new ColorDrawable(afterColor)};

        // Transit the background from black
        TransitionDrawable trans = new TransitionDrawable(colors);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(trans);
            trans.startTransition(BACKGROUND_TRANSITION_DURATION);
        } else {
            view.setBackgroundColor(afterColor);
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final Intent intent = new Intent(ACTION_NAVIGATION_CLICKED);
        intent.putExtra(EXTRA_NAVIGATION_ITEM_ID, item.getItemId());
        intent.putExtra(EXTRA_NAVIGATION_ITEM_TITLE, item.getTitle());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (galleryImageViewLayout != null) {
                    if (gallerySystemUiToggle) {
                        onBackPressed();
                    } else {
                        showSystemUI();
                        showAppNavigationBar();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class CustomImageLoader implements ImageLoader {
        private final RequestManager requestManager;

        CustomImageLoader(Context context) {
            requestManager = Glide.with(context);
        }

        @Override
        public void loadImage(Uri uri, final Callback callback) {
            requestManager
                    .asFile()
                    .load(uri)
                    .into(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(File resource,
                                                    com.bumptech.glide.request.transition.
                                                            Transition<? super File> transition) {
//                            ImageSource is = ImageSource.uri(Uri.fromFile(resource));

                            callback.onCacheHit(resource);
                            callback.onSuccess(resource);

                            Bitmap bitmap = BitmapFactory.decodeFile(resource.getPath());
                            extractAndApplyColor(bitmap);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            super.onLoadCleared(placeholder);
                            callback.onFinish();
                        }

                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            callback.onStart();
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            callback.onFail(new Exception("onLoadFailed"));
                        }
                    });

        }

        @Override
        public View showThumbnail(BigImageView parent, Uri thumbnail, int scaleType) {
            return null;
        }

        @Override
        public void prefetch(Uri uri) {
            final SimpleTarget<File> target = new SimpleTarget<File>() {
                @Override
                public void onResourceReady(File resource,
                                            com.bumptech.glide.request.transition.
                                                    Transition<? super File> transition) {

                }
            };

//            requestManager.asFile().load(uri).into(target);
        }
    }
}
