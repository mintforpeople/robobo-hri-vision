# Charuco Calibration App
The *Charuco Calibration App* can help you to get a better experience with the OpenCV camera (used in different modules*) by eliminating some distortions that could be introduced by the lens of the camera.
This document will serve as a guide to calibrate the camera correctly.
> *: Right now the only module that takes advantage of this is the Tag module.
## Charuco Board
The most important part of this process it's to obtain a Charuco board. You can make one yourself (ie, using [Calib.io](https://calib.io/pages/camera-calibration-pattern-generator)), ordering one online or getting one on a local camera store.
In any case, its fundamental that the board its **totally flat**.
## App Use
To use the App and the Caruco board, you should:

 - Place the Charuco board the desired surface (**always flat**)
 - Set the settings corresponding with your charuco board on the settings view (Menu -> Settings) and click "*Done*". Remember that the sizes should be on mm.
 - Choose the camera to be calibrated (by using the "*Change camera*" button).
 - Capture pictures of the Charuco board (by using the "*Capture*" button).
 - Calibrate the camera using the photos taken (by using the "*Calibrate*" button).
## Important Notes
It's important to remember that:
 - The boards needs to be flat.
 - Check the illumination.
 - The pictures should be of the Charuco board.
 - The calibration process should be done for each camera.

## For Devs
The calibration its done by using a Camera Matrix and a Distortion Coefficients Vector, each calibrations saves the obtained value for that camera in the *camera.properties* file. There would be only one matrix and vector for each camera with the names using the following format:

> distCoeffs# ...
> cameraMatrix# ...
>
Where *#* it's the camera identifier.

