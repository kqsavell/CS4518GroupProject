# Performance Measurements
### Inference Time
* Inference time was calculated by using SystemClock.uptimeMillis() in onPreExecute() and onPostExecute() of the AsyncTask for both off device inference and on device inference. The latency was displayed in a TextView in the app, and then manually recorded for 20 tests on the same image on a physical Samsung Galaxy S5.

|Off Device Inference (ms)| On Device Inference (ms)|
|:-----------------------:|:-----------------------:|
|           579           |           498           |
|           747           |           578           |
|           562           |           684           |
|           724           |           661           |
|           855           |           458           |
|           641           |           467           |
|           621           |           475           |
|           630           |           473           |
|           632           |           485           |
|           664           |           583           |
|           534           |           501           |
|           541           |           498           |
|           642           |           471           |
|           703           |           506           |
|           687           |           433           |
|           467           |           737           |
|           430           |           602           |
|           481           |           438           |
|           502           |           473           |
|           440           |           495           |

|Off Device Inference Average (ms)| On Device Inference Average (ms)|
|:-------------------------------:|:-------------------------------:|
|               604               |               526               |

* Although there is not a huge difference, off device inference took on average, 78 ms longer than on device inference.

![offDeviceLatency](images/offDeviceLatency.png) 
![onDeviceLatency](images/onDeviceLatency.png) 

### Memory & CPU Usage
* Memory and CPU Usage was determined by viewing the Android Profiler as the app was used in different stages on a physical Samsung Galaxy S5.


![offDeviceMemory](images/offDeviceMemory.png) 
![offDeviceCPU](images/offDeviceCPU.png) 
![onDeviceMemory](images/onDeviceMemory.png) 
![onDeviceMemory](images/onDeviceCPU.png) 

* As you can see from the pictures, off device and on device inference used about the same amount of memory and CPU resources.
* During off device inference, the app used about ~15mb worth of memory for running Native code while during on device inference, the app used about 13mb worth of memory for running Native code. These values were determined by subtracting the peak Native memory usage underneath the "Inference" timezone displayed in the images from the plateau of Native memory usage underneath the "App displaying inference text of image" timezone.

### Battery Usage
* something good
