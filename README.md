# Android Jacoco Instrumentation Guide (v202503)
## 1 Introduction
Instrumenting Android apps has always been a big headache:

+ The developing environment of Android apps is very complex with challenges from Android, Java, Gradle, AGP, etc., interwoven with each other.
+ Android apps are becoming more and more complex, relying on hundreds of subprojects, plugins, libraries, etc.
+ Existing tutorials are often outdated or simplistic, thus lack realistic guidance in practice.



This repository provides a guide to Android test coverage instrumentation (by Jacoco) based on extensive existing data and my own years of practice.



<br/>



## 2 Practical Limitations
Since the components associated with Android apps are so numerous and change so quickly, practice limitations should be declared before we start.

#### Gradle
+ Gradle 8: Tested Recently (2025.03)
+ Gradle 7 and 7-: The main idea is the same, but some changes are required. For example, you may need to include Jacoco explicitly.

#### Android
+ Tested on Android 9、13
+ As the Android version grows, there are more and more limitations to App Permissions

#### App
+ Apps from [AndroTest24](https://github.com/Yuanhong-Lan/AndroTest24) (including apps like Firefox, Signal, WordPress, and so on) have all been instrumented based on the way described here.
+ However, other apps or new versions of these apps may require more attempts.



<br/>



## 3 The Overall Process
1. **Clone the source code of the target app**
2. **Importing the app into Android Studio  ->  Fix app code sync issues**
3. **Build the raw APK of the app  ->  Fix the basic build issues**
4. **Install and run the raw app on an Android device  ->  Ensure normal operation**
5. **Do instrumentation changes**
6. **Build the instrumented APK of the app**
7. **Install and run the instrumented app on Android device**
8. **Get ec files while running the app**
9. **Generate Jacoco test report**



<br/>



## 4 Instrumentation Detail
#### Step-1 Add Permissions
It is recommended that EC files be managed at external storage.

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" tools:ignore="ScopedStorage" />
```

<br/>

#### Step-2 Add Broadcast Code
The Broadcast to trigger coverage dump of Jacoco.

Refer to [CoverageBroadcast.java](./java_class/CoverageBroadcast.java)

<br/>

#### Step-3 Add Broadcast Receiver
Add a receiver for manual coverage dump.

```xml
<receiver android:name=".CoverageBroadcast" android:exported="true">
    <intent-filter>
        <action android:name="com.example.pkg.Coverage_Jacoco" />
    </intent-filter>
</receiver>
```

<br/>

#### Step-4 Add Auto Dump
Let the app dump coverage automatically to reduce coverage data loss.

Find the application class and add:

```java
# Java Version
@Override
public void onTrimMemory(int level) {
    Log.d("CoverageJacoco", "Application onTrimMemory!");
    CoverageBroadcast.dumpCoverageData(this, "trim", false);
    super.onTrimMemory(level);
}
```

```kotlin
# Kotlin Version
override fun onTrimMemory(level: Int) {
    Log.d("CoverageJacoco", "Application onTrimMemory!")
    CoverageBroadcast.dumpCoverageData(this, "trim", false)
    super.onTrimMemory(level)
}
```

<br/>

#### Step-5 Add Root Gradle Script
Add a Gradle script to the project root to find all Android subprojects and config Jacoco automatically.

Refer to [root_jacoco.gradle](./gradle_scripts/root_jacoco.gradle)

<br/>

#### Step-6 Include Root Gradle Script
Include the Root Gradle Script at the project-level gradle.

```groovy
plugins {
    ...
}
# It is recommended to put it just after the plugins.
apply from: 'root_jacoco.gradle'
```

<br/>

#### Step-7 Add App Gradle Script
Add a Gradle script to the app module for Jacoco report generation.

Refer to [app_jacoco.gradle](./gradle_scripts/app_jacoco.gradle)

<br/>

#### Step-8 Include App Gradle Script
Include the App Gradle Script at the app-level gradle.

```groovy
plugins {
    ...
}
# It is recommended to put it just after the plugins.
apply from: 'app_jacoco.gradle'
```

<br/>

#### Step-9 Build APK
Sync the Gradle project, and then build the instrumented APK.

<br/>

#### Step-10 Generate Coverage Dirs
Run the `Task saveCoverageDirs` under `root_jacoco.gradle`, you will get a JSON file recording the important Dirs for the later Jacoco coverage report generation.

<br/>

#### Step-11 Get EC Files
There are two ways you can get the coverage EC files with our instrumentation:

1. Auto: The app will dump coverage every time OnTrimMemory
2. Manual: Use the Broadcast to get coverage whenever you want

<br/>

#### Step-12 Generate Jacoco Report
Put the EC files under `build/outputs/code-coverage` of the app module, then run the `Task jacocoTestReport` under `app_jacoco.gradle`, and you will get the Jacoco report under `build/reports`



