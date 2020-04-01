# Robobo line detection module

The Robobo line detection uses OpenCV to detect lines on the camera.


The module provides the following data about the detected lines:

- cor1x: First corner x coordinate
- cor1y: First corner y coordinate
- cor2x: Second corner x coordinate
- cor2y: Second corner y coordinate

To import the module in your Robobo application you must declare the module in the modules.properties file:

`robobo.module.#MODULE_NUMBER#=com.mytechia.robobo.framework.hri.vision.lineDetection.opencv.OpencvLineDetectionModule`
