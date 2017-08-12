package co.lkh.android.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import co.lkh.android.TwitterizedImageShowingActivity;

public class MainActivity extends AppCompatActivity {
    private final String DUMMY_IMAGE_URL = "http://lorempixel.com/400/200/sports/1/";
    private final String DUMMY_IMAGE_URL_2 = "http://lorempixel.com/400/200/sports/5/";

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (TwitterizedImageShowingActivity
                        .ACTION_NAVIGATION_CLICKED.equals(intent.getAction())) {
                    int id = intent.getIntExtra(
                            TwitterizedImageShowingActivity.EXTRA_NAVIGATION_ITEM_ID, -1);
                    String title = intent.getStringExtra(
                            TwitterizedImageShowingActivity.EXTRA_NAVIGATION_ITEM_TITLE);
                    Toast.makeText(context, "Clicked item: ID=" + id + " TITLE=" + title,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView imageView = findViewById(R.id.imageView);
        final ImageView imageView2 = findViewById(R.id.imageView2);

        Glide.with(this).load(DUMMY_IMAGE_URL).into(imageView);
        Glide.with(this).load(DUMMY_IMAGE_URL_2).into(imageView2);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String transitionName = ViewCompat.getTransitionName(imageView);
                Intent intent = new Intent(MainActivity.this, TwitterizedImageShowingActivity.class);
                intent.putExtra(TwitterizedImageShowingActivity.ARGS_IMAGE_URL, DUMMY_IMAGE_URL);
                intent.putExtra(TwitterizedImageShowingActivity.ARGS_TRANSITION_NAME,
                        transitionName);

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this, imageView, transitionName);

                ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String transitionName = ViewCompat.getTransitionName(imageView2);
                Intent intent = new Intent(MainActivity.this, TwitterizedImageShowingActivity.class);
                intent.putExtra(TwitterizedImageShowingActivity.ARGS_BOTTOM_MENU_RES_ID,
                        R.menu.navigation);
                intent.putExtra(TwitterizedImageShowingActivity.ARGS_IMAGE_URL, DUMMY_IMAGE_URL_2);
                intent.putExtra(TwitterizedImageShowingActivity.ARGS_TRANSITION_NAME,
                        transitionName);

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this, imageView2, transitionName);

                ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
            }
        });

        /*
         Since the navigation item clicked event sent through {@link LocalBroadcastManager}
          */
        IntentFilter intentFilter = new IntentFilter(
                TwitterizedImageShowingActivity.ACTION_NAVIGATION_CLICKED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                intentFilter);
    }
}
