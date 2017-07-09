[![Release](https://jitpack.io/v/badoualy/kanji-strokeview.svg)](https://jitpack.io/#badoualy/kanji-strokeview)

# Kanji StrokeView

Setup
----------------

First, add jitpack in your build.gradle at the end of repositories:
 ```gradle
repositories {
    // ...
    maven { url "https://jitpack.io" }
}
```

Then, add the library dependency:
```gradle
compile 'com.github.badoualy:kanji-strokeview:<version>'
```


Now go do some awesome stuff!

Usage
----------------
Just add the following code to your layout:
```xml
<com.github.badoualy.kanjistroke.KanjiStrokeView
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_gravity="center"/>
```

You can change the size to whatever you want, it should scale smoothly.

You can customize the view with the following attributes:

| name                   | default | description |
|------------------------|---------|-------------|
| svAutoRun              | false   | if true, the animation will start automatically when the view is first drawn. You can also set a delay via the `autoRunDelay` field. (No attribute yet) |
| svAnimate              | true    | if false, the kanji will not animate and always be drawn fully
| svStrokeColor          | black   | stroke color of the kanji (still and animated)
| svFingerColor          | primary color | color of the circle to display at current finger position when animating stroke
| svStrokeLightColor     | black at 50 opacity | color of the background complete shape ("preview")
| svStrokeHighlightColor | accent color | color of an highlighted stroke. You can highlight a stroke via the code by its index
| svStrokeWidth          | 4dp | stroke of the paint used to draw the strokes
| svFingerRadius         | 8dp | radius of the circle at current finger position when animating