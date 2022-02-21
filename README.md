# GTIntercherAndroid
GTIntercherAndroid is GTInternalAndroid + GTLauncherAndroid. (After GTIntercherAndroid update i will update GTInternalAndroid and GTLauncherAndroid, if i'm not lazy :D.)

![](https://cdn.discordapp.com/attachments/488978346072604682/934413462870839296/unknown.png)
![](https://cdn.discordapp.com/attachments/488978346072604682/934413382025625640/unknown.png)
![](https://cdn.discordapp.com/attachments/796637528328503317/934495591667478588/unknown.png)

## 📜 Features
- [x] Launch growtopia without having to update the launcher if the game is updated.
- [x] Floating Windows.
- [x] Lua executor. [Lua Api Documentations](https://github.com/ZTzTopia/GTIntercherAndroid/wiki)
- [ ] Multibot.

## 💻 Requirements
The following dependencies are required to build the library from source.
- Windows
  - [Android Studio](https://developer.android.com/studio).

- Android
  - [Termux](https://github.com/termux/termux-app/releases)
  - [Android SDK](https://github.com/Lzhiyong/termux-ndk/releases/tag/android-sdk)
  - [Android NDK](https://github.com/Lzhiyong/termux-ndk/releases/tag/android-ndk)

## 🔨 Building
- Android

  Download the [Android SDK](https://github.com/Lzhiyong/termux-ndk/releases/tag/android-sdk) and [Android NDK](https://github.com/Lzhiyong/termux-ndk/releases/tag/android-ndk).

  ```bash
  # install openjdk-17
  pkg install openjdk-17

  # install gradle
  pkg install gradle
  ```

  Add a `local.properties` file to the root of the project as below
  ```local.properties
  # modify the local.properties file
  # although ndk.dir has been deprecated, but it still works
  sdk.dir=/path/to/android-sdk
  ndk.dir=/path/to/android-ndk
  # for example:
  sdk.dir=/data/data/com.termux/files/home/android-sdk
  ndk.dir=/data/data/com.termux/files/home/android-ndk-r23b
  ```

  Execute the `gradle build` command to start building the android app, when building for the first time, the below error will occur.
  this is because the gradle plugin will download a corresponding version of `aapt2-7.0.3-7396180-linux.jar`, we need to replace it.

  ![](https://github.com/Lzhiyong/termux-ndk/blob/master/build-app/screenshot/build_aapt2_error1.jpg)

  Replace the aapt2 in `aapt2-7.0.3-7396180-linux.jar` with [aapt2-7.0.3-7396180-linux.jar](https://www.mediafire.com/file/7lclq4xaij3jiwg/aapt2-7.0.3-7396180-linux.jar/file) or [Making AGP jar file](https://github.com/Lzhiyong/termux-ndk/tree/master/build-app#making-agp-jar-file) you need [sdk-tools/build-tools/aapt2](https://github.com/Lzhiyong/sdk-tools/releases)

  ```bash
  # aapt2 is inside the jar file(aapt2-7.0.3-7396180-linux.jar)
  # because the aapt2 is x86_64 architecture not aarch64, so we need to replace it
  # execute the find command to search aapt2-xxx-linux.jar, then replace it
  cd ~/.gradle
  find . -type f -name aapt2-*-linux.jar
  cp /path/to/aapt2-7.0.3-7396180-linux.jar /the/output/from/find/command
  ```

  ![](https://github.com/Lzhiyong/termux-ndk/blob/master/build-app/screenshot/build_aapt2_error2.jpg)

  If an error occurs during the build app, this may be a network problem, please execute the `gradle build` again or execute the `gradle build --info` for more information.

  **Tutorial by: https://github.com/Lzhiyong/termux-ndk/tree/master/build-app**


**If you can't make it, you can download the finished application on the release page.**
