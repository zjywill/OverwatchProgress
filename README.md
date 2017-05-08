# OverwatchSpinner
<img width="50%" src="https://raw.githubusercontent.com/zjywill/OverwatchSpinner/master/SampleImage/overwatch_new.gif">

# Example
```
<com.comix.overwatch.HiveProgressView
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="192dp"
    android:layout_height="192dp"
    android:layout_gravity="center"
    app:hive_animDuration="5000" //animation duration
    app:hive_color="@color/colorAccent" //color of hexagon
    app:hive_maxAlpha="255" //hexagon animation max alpha
    />
```

# Installation
```
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
```
dependencies {
        compile 'com.github.zjywill:OverwatchSpinner:1.0'
}
```
