MangaView
========

An ImageView for reading manga(comics) for Android.

![mangaview1](https://user-images.githubusercontent.com/932136/91121347-8b21f300-e6d2-11ea-895c-a74f6095ebfc.gif)
![mangaview2](https://user-images.githubusercontent.com/932136/91121378-9b39d280-e6d2-11ea-9914-c390ae3000ba.gif)

### Page Layout Orientation
MangaView supports various page layout orientation.

 * Horizontal(RTL: Right to Left, LTR: Left to Right)

![mangaview4](https://user-images.githubusercontent.com/932136/91523749-8199c480-e938-11ea-9d2f-b2a4f7268e4a.gif)
![mangaview5](https://user-images.githubusercontent.com/932136/91523849-bad23480-e938-11ea-89fb-66e24bb8d65f.gif)


 * Vertical
 
![mangaview3](https://user-images.githubusercontent.com/932136/91523666-4bf4db80-e938-11ea-9b71-d46ec06f5ab7.gif)

Installation
========
To add MangaView to your project, include the following in your app module build.gradle file:

```
repositories {
    maven {
        url "https://c-lis.bintray.com/maven"
    }
}

dependencies {
    ...

    implementation 'dev.keiji.mangaview:mangaview:0.8.1-alpha4'
}
```


Sample App
========
You can download the sample app via [deploygate](https://dply.me/1dakl4).


License
=======

    Copyright 2020 Keiji ARIYAMA (C-LIS CO., LTD.)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

