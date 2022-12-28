# GTIntercherAndroid

[![Github Action](https://img.shields.io/github/actions/workflow/status/ZTzTopia/GTIntercherAndroid/android_ci.yml?branch=dev&logo=github&logoColor=white)](https://github.com/ZTzTopia/SAAndroidJava/actions?query=workflow%3ABuild)
[![GitHub Release](https://img.shields.io/github/v/release/ZTzTopia/GTIntercherAndroid.svg?color=orange&logo=docusign&logoColor=orange)](https://github.com/ZTzTopia/SAAndroidJava/releases/latest)

**GTIntercherAndroid** is a combination of two sources that i created, [GTInternalAndroid](https://github.com/ZTzTopia/GTInternalAndroid) and [GTLauncherAndroid](https://github.com/ZTzTopia/GTLauncherAndroid).

> **GTInternalAndroid** is a [Growtopia](https://www.growtopiagame.com/) mod menu with [ImGui](https://github.com/ocornut/imgui).

> **GTLauncherAndroid** is a [Growtopia](https://www.growtopiagame.com/) launcher **LiKE nO oThER**. Without having to update the launcher if the game is updated.

## Supported Version
Android 5.0 Lollipop (SDK 21) ~ 13 Tiramisu (SDK 33)

## Features
- Free and open source.
- Launch [Growtopia](https://www.growtopiagame.com/) without having to update the launcher if the game is updated.
- Floating Windows.
- Mod Menu
  - [x] Cheat
  - [ ] Multi Bot
  - [ ] Lua Executor

## Screenshot
|                   Mod Menu                   |
|:--------------------------------------------:|
| ![Mod Menu](https://i.imgur.com/fAFAFvU.png) |

|                    Floating Windows                    |
|:------------------------------------------------------:|
|  ![Floating Windows](https://i.imgur.com/WOT5aEf.png)  |

## Download
The latest application package kit can be found on the [Releases page](https://github.com/ZTzTopia/GTIntercherAndroid/releases).

## Documentation
Documentation can be found on the [Wiki](https://github.com/ZTzTopia/GTIntercherAndroid/wiki).

## Build
The following dependencies are required to build from source.
- [Android Studio](https://developer.android.com/studio).

The following steps are for building from source.
1. First you need to clone the source code of this project. `git clone --recurse-submodules https://github.com/ZTzTopia/GLauncherAndroid.git`
2. Then open it via [Android Studio](https://developer.android.com/studio).
3. When you click the `▶ Run button`, it will be built and run automatically.
> **Note**: Building this source from android will most likely work but I haven't tried it so feel free to experiment yourself :)
> 
> But you can try the tutorial I gave last year to build GTIntercherAndroid: [Build for Android](https://github.com/ZTzTopia/GTIntercherAndroid/wiki#build-for-android).

## Credits
- [Dobby](https://github.com/jmpews/Dobby): A lightweight, multi-platform, multi-architecture hook framework
- [frida-gum](https://github.com/frida/frida-gum): Retrieve some memory code
- [ImGui](https://github.com/ocornut/imgui): Bloat-free Graphical User interface
- [llvm-project](https://github.com/llvm/llvm-project): Retrieve clear cache code
- [KittyMemory](https://github.com/MJx0/KittyMemory): Runtime code patching

## License
This project is licensed under the MIT License. See the [LICENSE](https://github.com/ZTzTopia/GTLauncherAndroid/blob/main/LICENSE) file for details.
