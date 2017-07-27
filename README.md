[![Release](https://jitpack.io/v/badoualy/kanji-strokeview.svg)](https://jitpack.io/#badoualy/kanji-strokeview)

# <img src="https://github.com/badoualy/kanji-strokeview/blob/master/ART/web_hi_res_512.png" width="32"> Kanji StrokeView
<img src="https://github.com/badoualy/kanji-strokeview/blob/master/ART/preview.gif" width="300">

> **Warning**: This library was developed to use with KanjiVG files, if you want to use it with another source, you'll need to update the code and change the input rect size to match the size of your input files (or the view will not be able to scale properly).

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
compile 'com.github.badoualy:kanji-strokeview:1.0.0'
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

Then in your code, you can use the following methods:
- `setPathData` to set the view from another KanjiStrokeView
- `loadPathData` to load a list of path data strings like:
```kotlin
listOf("M34.25,16.25c1,1,1.48,2.38,1.5,4c0.38,33.62,2.38,59.38-11,73.25",
       "M36.25,19c4.12-0.62,31.49-4.78,33.25-5c4-0.5,5.5,1.12,5.5,4.75c0,2.76-0.5,49.25-0.5,69.5c0,13-6.25,4-8.75,1.75",
       "M37.25,38c10.25-1.5,27.25-3.75,36.25-4.5",
       "M37,58.25c8.75-1.12,27-3.5,36.25-4")
```
- `loadSvg` to load an SVG input (string/inputstream/file) (it'll be slower than the above method since it need to parse the entire svg file)

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
