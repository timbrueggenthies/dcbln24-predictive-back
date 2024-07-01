# Predictive Back Gesture Samples

This is a demo application to display various types of experiments with the [Predictive Back Gesture](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture) on Android. It acts as a gallery app that display some images from the assets.

## Motivation
This demo application was developed for a [Talk](https://berlin.droidcon.com/speaker/tim-bruggenthies/) at the 2024 Droidcon Berlin about Predictive Back Gestures on Android.

## Local Setup

You can checkout the repository and build and run the application yourself. However, to get the complete functionality, you need to make some changes.

### Google Maps

The app uses Google Maps to display location information about an image. To enable this functionality, you need to provide an API Key for the Google Maps API. You can either set your Key directly in the manifest entry, or via the `local.properties` file using this statement
```
MAPS_API_KEY = <your-key>
```