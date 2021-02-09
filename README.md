InstaSaver
======

InstaSaver is a simple and powerful kotlin library for saving of Instagram Posts and Reels. InstaSaver will allow you to save videos, Images, IGTV and Reels with single click.

# Gradle
```groovy
dependencies {
    implementation 'com.github.dayakar:InstantSave:1.0.0'
}
```
### Let's Download the Posts!
#### Download videos and posts
```kotlin
 val post=InstaSaver.getInstaPost(url)
 val downloadUrls=post.downloadLinks
```
#### I want to download captions
```kotlin
val post=InstaSaver.getInstaPost(url)
val caption=post.caption

```
### InstaSaver now is using Kotlin coroutines!
#### Calling InstaSaver should be done from coroutines scope
```kotlin
// e.g calling from activity lifecycle scope
lifecycleScope.launch {
   val post=InstaSaver.getInstaPost(url)
}

// calling from global scope
GlobalScope.launch {
   val post=InstaSaver.getInstaPost(url)
}
```
`

License
-------
   Copyright (c) 2021 Dayakar.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
