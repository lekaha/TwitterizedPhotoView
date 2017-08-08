
package co.lkh.android.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.Transition;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;

import co.lkh.android.view.util.PaletteUtil;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class TwitterizedImageShowingActivity extends AppCompatActivity {
    public final static String ARGS_IMAGE_URL = "ARGS_IMAGE_URL";
    public final static String ARGS_IMAGE_BITMAP = "ARGS_IMAGE_BITMAP";
    public final static String ARGS_TRANSITION_NAME = "ARGS_TRANSITION_NAME";
    public final static String ARGS_TRANSITION_BEFORE_BACKGROUND_COLOR
        = "ARGS_TRANSITION_BEFORE_BACKGROUND_COLOR";
    public final static int TRANSITION_DURATION = 200;
    public final static int BACKGROUND_TRANSITION_DURATION = 200;

    @Nullable
    ImageView imageView;
    @Nullable
    ViewGroup galleryImageViewLayout;
    @Nullable
    GestureDetectorCompat gestureDetector;

    boolean gallerySystemUiToggle = false;
    int beforeTransitBackgroundColor = Color.WHITE;
    int currentBackgroundColor = Color.BLACK;

    @Nullable
    String transitionName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        Fade fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(R.id.showing_image_view, true);
        fade.setDuration(TRANSITION_DURATION);

        applyTransitionToWindow(getWindow(), fade, true, true, true, true, true);

        setContentView(R.layout.activity_twitterized_image_showing);
        setupViews();
    }

    private void setupViews() {
        imageView = findViewById(R.id.showing_image_view);
        galleryImageViewLayout = findViewById(R.id.showing_image_view_layout);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            transitionName = bundle.getString(ARGS_TRANSITION_NAME);
            beforeTransitBackgroundColor
                    = bundle.getInt(ARGS_TRANSITION_BEFORE_BACKGROUND_COLOR, Color.WHITE);
            imageView.setTransitionName(transitionName);

            final String url = bundle.getString(ARGS_IMAGE_URL);
            final Bitmap bitmap = bundle.getParcelable(ARGS_IMAGE_BITMAP);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);

                // Push the task to the end of queue that could avoid stuck the main thread
                // at the moment.
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        extractAndApplyColor(bitmap);
                    }
                });
            } else if (url != null) {
                final SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource,
                                                com.bumptech.glide.request.transition.
                                                        Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(resource);
                        extractAndApplyColor(resource);
                    }
                };
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(url)
                        .into(target);
            }
            else {
                throw new IllegalArgumentException("lack needed arguments");
            }
        }

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        galleryImageViewLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        gestureDetector = new GestureDetectorCompat(this,
            new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (galleryImageViewLayout != null) {
                    if (gallerySystemUiToggle) {
                        hideSystemUI();
                    }
                    else {
                        showSystemUI();
                    }
                    gallerySystemUiToggle = !gallerySystemUiToggle;
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (galleryImageViewLayout != null) {
                    hideSystemUI();
                }
                return true;
            }
        });

        hideSystemUI();
    }

    void extractAndApplyColor(@NonNull final Bitmap bitmap) {
        // Change the background with prominent color from the image
        currentBackgroundColor = extractColor(bitmap);
        applyViewTransitBackgroundColor(galleryImageViewLayout,
                beforeTransitBackgroundColor, currentBackgroundColor);
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
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }

    void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (galleryImageViewLayout != null) {
            gallerySystemUiToggle = false;
            hideSystemUI();
        }
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
}
