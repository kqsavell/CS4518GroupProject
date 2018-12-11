# CS4518 Group Project Design Document
Antony Qin, Kyle Savell, Alex Tian, Joseph Yuen

## Purpose
The purpose of the mobile application is to take notes via typing or image and save it for later use via Drive/email.

## Features
- Note Editor
- Camera picture to text
- Gallery picture to text
- Send Notes to Email/Drive
- Off-Device/On-Device
- Inference Latency

### Note Editor
Provides the user with a notes title line and body section to write/edit text.

For the title input text view, we implemented `textPersonName` inputType. For the body section input text view, we implemented `textMultiLine` inputType. `textMultiLine` allows the for the user to type multiple lines and scroll through their text after exiting the keyboard.

### Camera image to text
Retrieves text from a camera photo taken within the application.

We implemented `fromCameraListener` and an intent to open the camera application and capture a photo.

### Gallery image to text
Retrieves text from a user-selected gallery image.

We implemented `fromPhotosListener` and an intent to open the image gallery application and select an image.

### Send Notes to Email/Drive
Allows the user to send their notes to the user’s Google Drive as a Google Doc and send thier notes as an email.

We implemented `emailListener` and an `ACTION_SEND` intent that first accesses the device’s stored Google accounts to get Gmail and Drive info, and then prompts the user to create an email to the account or create a Google Doc. Either will have the title and body text.

### Off-Device/On-Device Inference
Switches the inference computation to on-device or off-device.

Implemented `getOnDeviceTextRecognizer()` instance for on-device processing and `getCloudTextRecognizer()` instance for off-device processing.

### Inference Latency
Displays how long the inference takes to retrieve an image’s text.

Measured the start time using `onPreExecute()` and the end using `onPostExecute()` in the AsyncTask. Then, we calculated the latency time and updated the textView designated as `mTextView_latency`.

## Inference Model
###  FirebaseVision TextRecognizer

FirebaseVision is an entry class for Firebase machine learning vision services. To implement the inference model, we use `getOnDeviceTextRecognizer()` for on-device processing and `.getCloudTextRecognizer()` for off-device processing. Both processing types are ran in the background through AsyncTask with their respective instances of the model. Once an inference is complete, the body section of a note is updated with text.
