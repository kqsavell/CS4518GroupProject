author: Kyle Savell, Antony Qin, Alex Tian, Joseph Yuen
summary: CS4518 Group Project Part 3 Tutorial
id: Deep Learning Optimization
categories: Deep Learning Optimization
environment: java
status: final

# CS4518 Group Project Part 3 Tutorial
## Overview of Tutorial
This tutorial will show you how to optimize our deep learning application.

In this tutorial you will do the following:

*  Load a model in a background thread

Prerequisites:

* Android Studio Installed
* AVD installed with API 27 or later
* Part 1 Source Code downloaded

## Using a background thread
**Duration: 10 minutes**

* Create an AsyncTask that loads our on-device model in the onCreate() method

### Create an AsyncTask that loads our on-device model in the onCreate() method

Before optimizing our application, it took around one second to fully load. Part of the reason why this is the case is because the inference models used in the application are loaded directly in the onCreate() function of the launch activity. This means that the models need to be loaded before the UI is displayed. To enable the UI to be displayed faster, we used another AsyncTask to load the models in background threads.

Since we used Firebase for our inference models, the application uses two model instances - one for the on-device inference, and one for the cloud inference. In other inference methods the cloud-based model would not be referenced directly in the application, but the Firebase API has a cloud-based inference object to abstract all of the networking.

Due to having two models that need to be loaded, we will be passing in a boolean to the AsyncTask to tell it whether or not the model being loaded will be the on-device or off-device model. The return value will be an instance of `FirebaseVisionTextRecognizer` which is the superclass of the model.

``` java
private class loadModels extends AsyncTask<Boolean, Float, FirebaseVisionTextRecognizer>
```

For the background task the on-device or off-device model is loaded and returned.

``` java
@Override
        protected FirebaseVisionTextRecognizer doInBackground(Boolean... params) {
            onDevice = params[0];
            if (onDevice) {
                return FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            }
            else {
                return FirebaseVision.getInstance().getCloudTextRecognizer();
            }
        }
```

After the background task is executed, we stored the model in a global variable to be used when inference was needed.

``` java
@Override
        protected void onPostExecute(FirebaseVisionTextRecognizer model) {
            if (onDevice) {
                onDeviceModel = model;
                Log.d("MODELS", "On-Device model loaded.");
            }
            else {
                offDeviceModel = model;
                Log.d("MODELS", "Off-Device model loaded.");
            }
        }
```

Once the AsyncTask was set up we just replace the code we used to load the models in onCreate() with two background tasks, one to load each model.

``` java
AsyncTask loadOnDevice = new loadModels().execute(true); // Load on-device model
AsyncTask loadOffDevice = new loadModels().execute(false); // Load off-device model

```

## Startup Time Comparison

Once the AsyncTask was ready, we compared the start up time to the times we recorded in the Part 2 performance report.

|Start up Time Before Optimization (ms)|Start up Time After Optimization (ms)|
|:---:|:---:|
|1263|754|
|1081|797|
|873|548|
|1250|620|
|976|579|
|982|574|
|978|678|
|701|646|
|956|1083|
|715|702|
|680|556|
|913|720|
|717|641|
|768|572|
|1123|535|
|731|656|
|717|555|
|688|594|
|819|734|
|733|684|

|Start up Time Before Optimization Avg. (ms)| Start up Time After Optimization Avg. (ms)|
|:---:|:---:|
|883|661|

* Summary: These measurements were taken from a physical Samsung Galaxy S5. Our app started up, on average, in 883ms before optimization. After optimizing the app by moving the loading of the models to a background thread our app started up, on average, in 661ms. That shaved off a significant 222ms from the app startup.

## Conclusion
Based on start up times recorded after implementing model loading in the background, it appears that this is an effective way of reducing the start up time for our application.
