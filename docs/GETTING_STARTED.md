# Getting Started

In order to build the project, make sure that you first clone all of the submodules:

```bash
$ git submodules update --init --recursive
```

## Dependencies

You will need to install **JDK 19** (to run Gradle) and **Node.js** (to compile the web extension). There are a few different ways to install these:

### Nix

If you have [Nix](https://nixos.org/download.html) and [direnv](https://github.com/nix-community/nix-direnv) installed, these dependencies will be installed for you automatically. When your shell is inside of the repository, it will populate `JAVA_HOME` for you and `node` and `npm` will be part of your `PATH`.

Xcode will find the dependencies automatically, but Android Studio will have to be configured manually. You will need to find the JDK path:

```bash
$ echo $JAVA_HOME
```

and then set this path in Android Studio in `Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK`.

### Homebrew

If you are developing on macOS, you can use Homebrew. You will need to install the following dependencies:

```bash
$ brew install node
$ brew install --cask temurin
```

Xcode will automatically find these dependencies, but you will need to configure Android Studio manually. You will need to select `temurin-19` in Android Studio in `Preferences -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK`.

## Building

### macOS and iOS

In order to build for Apple platforms, you will need to be developing on a Mac and you will need to download [Xcode](https://developer.apple.com/xcode/).

Open the Xcode project (`Apple/Wallet.xcodeproj`) and build and run the app using the `App (iOS)` or `App (macOS)` schemes.

### Android

In order to build for Android, you will need to download [Android Studio](https://developer.android.com/studio).

Open the repository in Android Studio and build and run the app using the `Android` configuration.

If you are building from the command line (for example to run `./gradlew test` or `./gradlew :Android:assembleRelease`), install
the Android command-line tools and point the build at them via `ANDROID_SDK_ROOT`:

```bash
mkdir -p "$HOME/android-sdk"
cd "$HOME/android-sdk"
curl -L -o commandlinetools-linux.zip \
  https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux.zip -d cmdline-tools-temp
mkdir -p cmdline-tools/latest
mv cmdline-tools-temp/cmdline-tools/* cmdline-tools/latest/
rm -rf cmdline-tools-temp commandlinetools-linux.zip

export ANDROID_SDK_ROOT="$HOME/android-sdk"
yes | "$ANDROID_SDK_ROOT"/cmdline-tools/latest/bin/sdkmanager --licenses
"$ANDROID_SDK_ROOT"/cmdline-tools/latest/bin/sdkmanager \
  "platform-tools" "platforms;android-36" "build-tools;35.0.0"
```

With the SDK installed, set `ANDROID_SDK_ROOT` in your shell before invoking Gradle so that the Android plugins can locate the
platform and build tools.
