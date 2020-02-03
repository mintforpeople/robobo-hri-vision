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

robobo.module.#MODULE_NUMBER#=com.mytechia.robobo.framework.hri.vision.objectRecognition.tensorFlow.TensorFlowObjectRecognizerModule

## Acknowledgement
<!-- 
    ROSIN acknowledgement from the ROSIN press kit
    @ https://github.com/rosin-project/press_kit
-->

<a href="http://rosin-project.eu">
  <img src="http://rosin-project.eu/wp-content/uploads/rosin_ack_logo_wide.png" 
       alt="rosin_logo" height="60" >
</a>

Supported by ROSIN - ROS-Industrial Quality-Assured Robot Software Components.  
More information: <a href="http://rosin-project.eu">rosin-project.eu</a>

<img src="http://rosin-project.eu/wp-content/uploads/rosin_eu_flag.jpg" 
     alt="eu_flag" height="45" align="left" >  

This project has received funding from the European Union’s Horizon 2020  
research and innovation programme under grant agreement no. 732287. 
