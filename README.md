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
  compile 'com.github.lekaha:twitterized-photo-view:0.0.3'
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

Sample
=========
![Sample screenshot1](/screenshots/show.gif)

License
=========

This library is licensed under the terms in the file named "LICENSE" for more detail.

