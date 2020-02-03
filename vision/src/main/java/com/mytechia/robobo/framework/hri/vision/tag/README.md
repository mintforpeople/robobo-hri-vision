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
