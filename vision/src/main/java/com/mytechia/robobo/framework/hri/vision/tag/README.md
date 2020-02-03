# Robobo Tag detection module

The Robobo tag recognition uses the OpenCV_contrib Aruco module to detect Aruco or AprilTag tags and estimate its pose.

The module provides the following data about the detected objects:

- id: ID of the tag
- cor1x: First corner x coordinate
- cor1y: First corner y coordinate
- cor2x: Second corner x coordinate
- cor2y: Second corner y coordinate
- cor3x: Third corner x coordinate
- cor3y: Third corner y coordinate
- cor4x: Fourth corner x coordinate
- cor4y: Fourth corner y coordinate
- rvec_0: First component of the rotation vector
- rvec_1: Second component of the rotation vector
- rvec_2: Third component of the rotation vector
- tvec_0: First component of the translation vector
- tvec_1: Second component of the translation vector
- tvec_2: Third component of the translation vector

The default detected markers are Aruco tags from the 4x4_1000 dictionary, with 100mm sides
