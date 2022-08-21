# Nimbus Android Sample [![Build](https://github.com/timehop/nimbus-android-sample/actions/workflows/build.yml/badge.svg)](https://github.com/timehop/nimbus-android-sample/actions/workflows/build.yml)

Welcome to Nimbus Sample App - ads by publishers, for publishers.

## Build Setup

### Requirements

- Android 13 SDK (API Level 33)

### Installation

After cloning this repo, open it in Android Studio wait for the Gradle sync to finish

### Required Keys

The sample application is bundled with keys pointing to a development environment defined in the 
root project gradle.properties file. If you would like to use your production keys, replace the 
`sample_publisher_key` and `sample_api_key` in that file or locate the `PUBLISHER_KEY` and 
`API_KEY` buildConfigFields in `sample/build.gradle.kts`

### Optional IDs

In order to see APS/FAN/GAM/Unity examples you must also supply it's IDs. You can do so by
adding the IDs in the predefined fields in the root project `gradle.properties` file or by replacing
the buildConfigFields in `sample/build.gradle.kts`

## How to run

After installing all dependencies and setting up the required keys you're good to go

- Make sure to have an emulator already set up or a physical device connected to Android Studio
- Run the app by clicking the play button Android Studio

### What you'll see

You will be able to see several examples categorized by specific sections, such as:

- Examples of different types of Ads
- Ads by mediation platforms (Google)
- Ads with MOAT viewability integration
- Ad Markup Renderer

## Testing Ad Markup

The `Test Render` section of the sample app provides basic functionality for rendering ad markup using the latest 
version of the Nimbus SDK. The tool provides a text input field that can receive any well formed HTML or VAST markup
and will render the ad as a full screen ad. 

1. Copy the contents of the `markup` field, without the any leading or trailing quotes, from a Nimbus
   response and paste it into the input field.
2. Click the `Test` button. The markup will be rendered into a full screen container.

### Verifying Static Ads

If the container shows a blank white screen (a potential bad ad) or further verification of functionality is required:

1. Ensure the Sample app is running using an Emulator or a physical device with USB Debugging enabled.
2. Open Chrome and navigate to `chrome://inspect`. A list of active WebViews in com.adsbynimbus.android.sample will be  
displayed.
3. Click the `inspect` button on the WebView named `about:blank about:blank` that matches the size of the screen.
4. In the DevTools window, select the Console tab and inspect the output for any errors. 

##### Any errors that appear in the DevTools console can be ignored if the ad renders properly and there are minimal reporting discrepancies between Nimbus and the network serving the creative.
    
If the ad markup does not render using the test tool, first ensure that the markup pasted into the input field is valid. 
For example, if the markup was obtained from a server log it may contain additional formatting characters that must be 
removed or properly escaped prior to pasting it into the tool.

### Verifying Video Ads

Errors rendering a video ad can be identified by a completely black screen with the the close button appearing at the 
top left of the ad container. Detailed information about the error can be retrieved using `logcat`.

#### Companion Ads

If the VAST creative contains a companion ad that does not render, check the size of the Companion Ad in the markup.
The `Test Render` tool is setup with a 320 by 480 end card Companion Ad by default; if another size Companion Ad is 
defined in the VAST it will not render without rebuilding the Sample app with an additional Companion Ad definition
that matches the size defined in the VAST markup.

## Need help?

You can check out [Nimbus Android Quick Start Guide](https://adsbynimbus-public.s3.amazonaws.com/android/docs/1.12.1/index.html)

## License

Distributed under [GNU GPLv3](https://choosealicense.com/licenses/gpl-3.0/)
