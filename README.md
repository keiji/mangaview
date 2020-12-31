MangaView
========

An ImageView for reading manga(comics) for Android.

You can download the sample app via [deploygate](https://dply.me/1dakl4).

Features
========

![scale_and_swipe](https://github.com/keiji/mangaview/blob/gallery/scale_and_swipe.gif)
![double_spread_page](https://github.com/keiji/mangaview/blob/gallery/double_spread_page.gif)

 * Scrolling, with smooth scrolling fling.
 * Page zooming, using multi-touch and double-tap(optional).
 * Page transition, with swipe.
 * View spread pages by the display orientation.
 * Works fine when used in a scrolling parent (such as RecyclerView or ViewPager2).

You'll look all features [here](https://github.com/keiji/mangaview/blob/gallery/README.md).

Installation
========
To add MangaView to your project, include the following in your app module build.gradle file:

```
dependencies {
    ...

    implementation 'dev.keiji.mangaview:mangaview:1.1.1'
}
```

Usage
=======
There is a [sample](https://github.com/keiji/mangaview/tree/master/sample) provided how to use the MangaView.

 * Full features sample: [MainActivity](https://github.com/keiji/mangaview/blob/master/sample/src/main/java/jp/co/c_lis/mangaview/android/MainActivity.kt)
 * With ViewPager2 sample: [WithViewPager2Activity](https://github.com/keiji/mangaview/blob/master/sample/src/main/java/jp/co/c_lis/mangaview/android/WithViewPager2Activity.kt)

License
=======

    Copyright 2020-2021 Keiji ARIYAMA (C-LIS CO., LTD.)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

