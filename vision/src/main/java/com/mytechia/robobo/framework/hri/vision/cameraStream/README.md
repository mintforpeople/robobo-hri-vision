# Camera streaming module

The Robobo camera streaming module uses the OpenCV camera to get images from the camera, encode them in the JPEG format and send them via a Websocket.

By default the WebSocket uses the port 3434, which can be changed in the Server class.
You can change the quality of the image by adding properties to the encoding method, for example to set the quality to 30 (very low) you should add as follows:

`MatOfInt props = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 30);
 Imgcodecs.imencode(".jpg", mat, bytemat, props);`
 
where mat is the Mat object that the OpenCV camera module sends and bytemat its the buffer for the encoding

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