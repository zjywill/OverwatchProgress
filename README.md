# OverwatchProgress
Have you ever played Overwatch and amazed by the beautiful loading view,  now you can use in your android application.  

<img width="50%" src="https://raw.githubusercontent.com/zjywill/OverwatchProgress/master/SampleImage/overwatch_new.gif">


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
        compile 'com.github.zjywill:OverwatchProgress:1.0'
}
```

# License
```
Copyright [2017] [Junyi Zhang]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
