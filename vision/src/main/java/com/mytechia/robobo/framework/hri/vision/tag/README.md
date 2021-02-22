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

To import the module in your Robobo application you must declare the module in the modules.properties file:

`robobo.module.#MODULE_NUMBER#=com.mytechia.robobo.framework.hri.vision.tag.opencv.OpencvTagModule`

## Acknowledgement

<a href="https://aiplus.udc.es/">
  <img src="https://aiplus.udc.es/wp-content/uploads/2019/12/logo-naranja-100x100.png"
       alt="rosin_logo" height="80"align="left" >
</a>

Developing an artificial intelligence curriculum adapted to european high schools
<br>2019-1-ES01-KA201-065742<br>
More information: <a href="https://aiplus.udc.es/">aiplus.udc.es</a>

<br>

<img src="http://aiplus.udc.es/wp-content/uploads/2021/02/cofinanciadoEN.png"
     alt="eu_flag" height="50" />
 <br>    
This project has been funded with support from the European Commission. This web reflects the views only of the author, and the Commission cannot be held responsible for any use which may be made of the information contained therein.
