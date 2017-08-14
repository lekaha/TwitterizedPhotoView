TwitterizedPhotoView
=========

[![Download](https://api.bintray.com/packages/lekaha/MavenRepo/TwitterizedPhotoView/images/download.svg)](https://bintray.com/lekaha/MavenRepo/TwitterizedPhotoView/_latestVersion)

A view that shows the image with transition animation and the background color is extracting 
from the image. The transition animation is moving and scaling the image from origin position to
  the center of screen, and scale the image to fit the width of screen. When showing the image the
  background color of the screen will be determine by extracting the image's prominent dark color.
  
Download
=========
Use Gradle:
```
dependencies {
  compile 'com.github.lekaha:twitterized-photo-view:0.0.5'
}
```
    
Usage
=========

- Passing the showing image's URL:

```java
String transitionName = ViewCompat.getTransitionName(imageView);
Intent intent = new Intent(context, TwitterizedImageShowingActivity.class);
intent.putExtra(TwitterizedImageShowingActivity.ARGS_IMAGE_URL, DUMMY_IMAGE_URL);
intent.putExtra(TwitterizedImageShowingActivity.ARGS_TRANSITION_NAME,
        transitionName);

ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
        MainActivity.this, imageView, transitionName);

ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
```

- Passing the showing image's bitmap:

```java
String transitionName = ViewCompat.getTransitionName(imageView);
Intent intent = new Intent(context, TwitterizedImageShowingActivity.class);
intent.putExtra(TwitterizedImageShowingActivity.ARGS_IMAGE_BITMAP, bitmap);
intent.putExtra(TwitterizedImageShowingActivity.ARGS_TRANSITION_NAME,
        transitionName);

ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
        MainActivity.this, imageView, transitionName);

ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());

```

### More
  
- Attach the navigation by given the Menu resource ID.
```java
intent.putExtra(TwitterizedImageShowingActivity.ARGS_BOTTOM_MENU_RES_ID,
                        R.menu.navigation);
```

Notice
=========
This library is using Google Android Support Library and the version is `26.0.0`. 
Please check it out your build environment that should be compatible with this version.
You may need to add Google's maven repository in your `build.gradle`
And, using [PhotoView](https://github.com/chrisbanes/PhotoView/) at the moment it is needed 
to add one more maven repository,  

```
allprojects {
    repositories {
        ...
        maven { url "https://maven.google.com" }
        maven { url "https://jitpack.io" }
    }
}
```
For more detail please refer to [Support Library Setup](https://developer.android.com/topic/libraries/support-library/setup.html)

Sample
=========
![Sample screenshot1](/screenshots/show.gif)

License
=========

This library is licensed under the terms in the file named "LICENSE" for more detail.

