# Charuco Calibration App
The *Charuco Calibration App* (<a href="https://github.com/mintforpeople/robobo-programming/wiki/calibration-app.zip">download the app</a>) can help you to get a better experience with the OpenCV camera (used in different modules*) by eliminating some distortions that could be introduced by the lens of the camera.
This document will serve as a guide to calibrate the camera correctly.
> *: Right now the only module that takes advantage of this is the Tag module.
## Charuco Board
The most important part of this process is to obtain a Charuco board. You can make one yourself (ie, using [Calib.io](https://calib.io/pages/camera-calibration-pattern-generator)), ordering one online or getting one on a local camera store.
In any case, its fundamental that the board its **totally flat**. Be careful when you print it to verify that the selected dimensions are not modified by the printer. We recommend to measure the size of the printed aruco with a ruler or similar.
## App Use
To use the App and the Charuco board, you should:

 - Place the Charuco board in a flat surface (**for instance, in a wall**)
 - Modify the settings corresponding with your charuco board on the settings view (Menu -> Settings) and click "*Done*". Remember that the sizes should be on mm.
 - Choose the camera to be calibrated, front or back (by using the "*Change camera*" button).
 - Capture pictures of the Charuco board (by using the "*Capture*" button). We recommend to take between 5 and 10 images with different angles.
 - Calibrate the camera using the photos taken (by using the "*Calibrate*" button).
## Important Notes
It's important to remember that:
 - The boards needs to be flat.
 - The illumination should be clear.
 - The pictures should contain the Charuco board.
 - The calibration process should be done for each camera.

## For Devs
The calibration is carried out by using a Camera Matrix and a Distortion Coefficients Vector. Each calibration saves the obtained value for that camera in the *camera.properties* file of the Android Sytem. There would be only one matrix and vector for each camera with the names using the following format:

> distCoeffs# ...
> cameraMatrix# ...
>
Where *#* it's the camera identifier.

