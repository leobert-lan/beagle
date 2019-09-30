# Beagle (Android library)
*A smart and reliable side drawer for debugging your Android apps.*

<img src="screenshots/general01.png" width="30%" /> <img src="screenshots/general02.png" width="30%" />

### Usage
Add the following to your top level Gradle file:

```groovy
allprojects {
    repositories {
        …
        maven { url "https://jitpack.io" }
    }
}
```

...and this to the module-level build script (check the widget below the code snippet for the latest version):

```groovy
dependencies {
    …
    def beagleVersion = "0.2.1"
    debugImplementation "com.github.pandulapeter.beagle:beagle:$beagleVersion"
    releaseImplementation "com.github.pandulapeter.beagle:beagle-noop:$beagleVersion"
}
```

The latest version is:


[![](https://jitpack.io/v/pandulapeter/beagle.svg)](https://jitpack.io/#pandulapeter/beagle)

The library has to be initialized with an Application instance (preferably in the Application's onCreate() method) by calling:

```kotlin
Beagle.imprint(this)
```

After this a list of modules needs to be provided, but this can be changed at any time and the UI will be automatically updated. See [this implementation](https://github.com/pandulapeter/beagle/blob/master/example/src/main/java/com/pandulapeter/beagleExample/BeagleExampleApplication.kt) for a detailed example.

### Modules
Any number of generic modules can be added in any order as long as they have a unique ID:
* [Text](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/TextTrick.kt) [[Screenshot]](/screenshots/moduleText.png) - Displays simple text content.
* [LongText](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/LongTextTrick.kt) [[Screenshot]](/screenshots/moduleLongText.png) - Displays a longer piece of text that can be collapsed into a title.
* TODO: Image - Displays a drawable.
* TODO: TextInput - Allows the user to enter free text.
* TODO: Slider - Allows the user to adjust a numeric value.
* TODO: ColorPicker - Allows the user to pick a color.
* [Toggle](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/ToggleTrick.kt) [[Screenshot]](/screenshots/moduleToggle.png) - Displays a switch with configurable title and behavior - ideal for feature toggles.
* [Button](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/ButtonTrick.kt) [[Screenshot]](/screenshots/moduleButton.png) - Displays a button with configurable text and action.
* TODO: KeyValue - Displays a list of key-value pairs that can be collapsed into a title.
* [List](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/ListTrick.kt) [[Screenshot]](/screenshots/moduleList.png) - Displays an expandable list of custom items and exposes a callback when the user makes a selection. A possible use case could be providing a list of test accounts to make the authentication flow faster.
* [SingleSelectionList](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/SingleSelectionListTrick.kt) [[Screenshot]](/screenshots/moduleSingleSelectionList.png) - Displays a list of radio buttons. A possible use case could be changing the base URL of the application to simplify testing on different backend environments.
* TODO: MultipleSelectionList - Displays a lst of checkboxes.

Unique modules can only be added once as they are specific to a single use case:
* [Header](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/HeaderTrick.kt) [[Screenshot]](/screenshots/moduleHeader.png) - Displays a header on top of the drawer with general information about the app / build.
* [KeylineOverlayToggle](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/KeylineOverlayToggleTrick.kt) [[Screenshot]](/screenshots/moduleKeylineOverlayToggle.png) - Displays a switch that, when enabled, draws a grid over your app with configurable dimensions that you can use to check the alignments of your Views.
* TODO: ForceRtToggle - Forces RTL layout orientation (on/off).
* [AppInfoButton](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/AppInfoButtonTrick.kt) [[Screenshot]](/screenshots/moduleAppInfoButton.png) - Displays a button that links to the Android App Info page for your app.
* [ScreenshotButton](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/ScreenshotButtonTrick.kt) [[Screenshot]](/screenshots/moduleScreenshotButton.png) - Displays a button that takes a screenshot of the current layout and allows the user to share it.
* TODO: StringGeneratorButton - Generates a strings and copies it to the clipboard based on the user's preferences.
* TODO: StressReliefButton - Displays fireworks.
* [NetworkLogList](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/NetworkLogListTrick.kt) [[Screenshot]](/screenshots/moduleNetworkLogList.png) - Displays an expandable list of historical network activity. Each item can be tapped for more information. To use this functionality, the custom beagleNetworkInterceptor needs to be added to the OkHTTP Client's builder, as implemented [here](https://github.com/pandulapeter/beagle/blob/master/example/src/main/java/com/pandulapeter/beagleExample/networking/NetworkingManager.kt).
* [LogList](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/modules/LogListTrick.kt) [[Screenshot]](/screenshots/moduleLogList.png) - Displays an expandable list of your custom logs. An example use case could be logging analytics events. Each item can be tapped for more information if you specified a payload. To log an event, simply call beagle.log().
* TODO: DeviceConfigurationKeyValue - Displays information about the current device and the OS.

The module list can be changed at any time (from any thread) using the following functions:

```kotlin
Beagle.learn(modules)
Beagle.learn(module, positioning)
Beagle.forget(id)
```

### Customization
* The UI of the drawer can be personalized by specifying an [Appearance](https://github.com/pandulapeter/beagle/blob/master/beagle-core/src/main/java/com/pandulapeter/beagleCore/configuration/Appearance.kt) instance when initializing the library.
* To properly support back navigation, all activities must check if the drawer consumes the event. This is implemented [here](https://github.com/pandulapeter/beagle/blob/master/example/src/main/java/com/pandulapeter/beagleExample/screens/MainActivity.kt).
* The drawers can be disabled / enabled at runtime by modifying the value of Beagle.isEnabled. This could be useful if you want to restrict access to the debug drawer features based on user type.

### To do
* Create a base class for dialogs with proper 2D scrolling
* Improve the example app
* Add support for dividers
* Expose the BeagleDrawer view

### License
```
Copyright 2019 Pandula Péter

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