# Nimbus Android Sample [![Build](https://github.com/timehop/nimbus-android-sample/actions/workflows/build.yml/badge.svg)](https://github.com/timehop/nimbus-android-sample/actions/workflows/build.yml)

Welcome to Nimbus Sample App - ads by publishers, for publishers.

## Build Setup

### Requirements

- Android 12 SDK (API Level 31)

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
the buildConfigFields in `sammple/build.gradle.kts`

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

## Need help?

You can check out [Nimbus Android Quick Start Guide](https://adsbynimbus-public.s3.amazonaws.com/android/docs/1.11.3/index.html)

## License

Distributed under [GNU GPLv3](https://choosealicense.com/licenses/gpl-3.0/)
