# Building

## Requirements

| Tool | Version |
|---|---|
| JDK | 17 (21 also works) |
| Android SDK | platform 34, build-tools 34.0.0 |
| Gradle | 8.9 (wrapper included) |
| Kotlin / AGP | 1.9.0 / 8.7.3 (pinned in `gradle/libs.versions.toml`) |

## Desktop (Windows / Linux / macOS)

```bash
git clone https://github.com/Plusorange1/ZuiTweak.git
cd ZuiTweak
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

Android Studio works as well — open the project, let it sync, then *Build → Build APK*.

### Release signing

`app/build.gradle.kts` expects `app/release-keystore.jks` and three environment
variables:

```bash
export storePassword=...
export keyAlias=...
export keyPassword=...
./gradlew assembleRelease
```

Without them, use the debug build.

## CI

`.github/workflows/build.yml` builds a debug APK on every push to `main` (and uploads it
as a workflow artifact). `.github/workflows/release.yml` creates a release with the APK
when a tag is pushed.

## Termux (Android, arm64)

The SDK ships x86_64 binaries, so `aapt2` and `zipalign` must be swapped for Termux's
arm64 builds. This is how the current fork is built on-device.

```bash
pkg install -y openjdk-17 aapt2 zipalign unzip curl

export ANDROID_HOME=$HOME/android-sdk
export JAVA_HOME=$PREFIX/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

mkdir -p "$ANDROID_HOME/cmdline-tools"
cd "$ANDROID_HOME"
curl -LO https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q commandlinetools-linux-11076708_latest.zip -d cmdline-tools
mv cmdline-tools/cmdline-tools cmdline-tools/latest

SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
yes | "$SDKMANAGER" --sdk_root="$ANDROID_HOME" --licenses
yes | "$SDKMANAGER" --sdk_root="$ANDROID_HOME" "platforms;android-34" "build-tools;34.0.0"

# use the arm64 binaries
BT="$ANDROID_HOME/build-tools/34.0.0"
ln -sf "$PREFIX/bin/aapt2"    "$BT/aapt2"
ln -sf "$PREFIX/bin/zipalign" "$BT/zipalign"

cd /path/to/ZuiTweak
echo "sdk.dir=$ANDROID_HOME" > local.properties
echo "android.aapt2FromMavenOverride=$PREFIX/bin/aapt2" >> gradle.properties

./gradlew --no-daemon assembleDebug
```

`local.properties` and the `aapt2FromMavenOverride` line are machine-specific — do not
commit them (`local.properties` is already in `.gitignore`).

### Termux troubleshooting

| Error | Cause / fix |
|---|---|
| `Cannot parse project property android.nonTransitiveRClass='trueandroid.aapt2…'` | `gradle.properties` had no trailing newline when the line was appended — add a newline |
| `aapt2 … Exec format error` | the x86_64 build-tools binary was used; the symlink above was skipped |
| `SDK location not found` | `local.properties` missing or pointing at the wrong directory |
| Gradle runs out of memory | `org.gradle.jvmargs=-Xmx2g` in `gradle.properties` |

## Project layout

```
app/                 application module (Compose UI + hooks)
hidden-api/          compile-only stubs for hidden Android APIs
gradle/              version catalog + wrapper
docs/                documentation
```

The module compiles against `de.robv.android.xposed:api:82` (legacy Xposed API) and
`com.github.topjohnwu.libsu:core` (root shell for the *Reload* action).
