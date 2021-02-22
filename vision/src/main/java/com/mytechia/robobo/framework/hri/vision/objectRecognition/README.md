# Object recognition module

The Robobo object recognition module uses a Tensorflow Lite model to detect and recognize multiple objects in real time in the images captured by the camera.

The module provides the following data about the detected objects:

- label: Label of the object
- posx/posY: Coordinates of the center of the object
- hidth/height: Width and Height of the bounding box
- confidence: Level of confidence of the detection

By default, the object recognition module uses a basic tflite model that detects 100 types of assorted objects.
You can add your custom models by dropping your detect.tflite and labelmap.txt files in a folder located in "/properties" in the phone internal memory.
The model must be trained for a 300x300 input image.

To import the module in your Robobo application you must declare the module in the modules.properties file:

`robobo.module.#MODULE_NUMBER#=com.mytechia.robobo.framework.hri.vision.objectRecognition.tensorFlow.TensorFlowObjectRecognizerModule`

## Acknowledgement

<a href="https://aiplus.udc.es/">
  <img src="https://aiplus.udc.es/wp-content/uploads/2019/12/logo-naranja-100x100.png"
       alt="rosin_logo" height="60" align="left" >
</a>

Developing an artificial intelligence curriculum adapted to european high schools
2019-1-ES01-KA201-065742.<br>
More information: <a href="https://aiplus.udc.es/">aiplus.udc.es</a>

<br>

<img src="http://aiplus.udc.es/wp-content/uploads/2021/02/cofinanciadoEN.png"
     alt="eu_flag" height="65" align="left"> This project has been funded with support from the European Commission. This web reflects the views only of the author, and the Commission cannot be held responsible for any use which may be made of the information contained therein.
