# Robobo lane detection module

The Robobo lane detection uses OpenCV to detect lanes:
The standard module uses the lineDetector module and returns the two straight lines (see Format #1).
The advanced module uses only the OpenCV camera to find two lines by fitting second degree polynomials **in a birdview from the original image**.

## Format #1:
A Mat containing the two lines with the x,y coordinates for the starting and ending point of the line
- cor1x: First corner x coordinate
- cor1y: First corner y coordinate
- cor2x: Second corner x coordinate
- cor2y: Second corner y coordinate

## Format #2:
Two Line objects (each containing the coefficients for the second-degree polynomial found) and a transformation matrix (3x3 matrix) used to transform a plane/image to the original perspective.

This could be used to draw into a mask and then transformed into the original perspective using the `minv` matrix.


The recommended way to draw a line its using its draw() function (see more at the Javadoc), or by using the `last_fit_pixel` member variable (a double array with each coefficient).

The transformation that the module uses to get a birdview can be modified, to achieve this the next 9 lines should be on camera.properties:

    lt_tl_x=0.25
    lt_tl_y=0.25
    lt_tr_x=0.8
    lt_tr_y=0.2
    lt_bl_x=0
    lt_bl_y=1
    lt_br_x=1
    lt_br_y=1
    lt_avg_results=false

All the keys should be present, but the values could be changed as needed.

The first 8 values should be a float, and represent a percentage of the total size (height or width)

The last line its a flag use an average the coefficients of the lines instead of searching for the lines on each iterations, this could help on really slow devices.

Another thing to note is that the key names for the percentages follow the next pattern:

`lt(keyword)_corner(tr=top right for example)_coordinate(x or y)`


## Notes
The advanced module is recommended if possible.
The advanced module detect white and yellow lines.
There's a call that can be made to toggle a color inversion (INVERT-COLORS-LANE), this is useful when working on a white background and black lines (inverse from real life), in this case the yellow lines are not "inverted", this means that the module will search for yellow and black lines.

The standard module detects any line.

To import the module in your Robobo application you must declare the module in the modules.properties file:

`robobo.module.#MODULE_NUMBER#=com.mytechia.robobo.framework.hri.vision.laneDetection.opencv.OpencvAdvancedLaneDetectionModule`

or

`robobo.module.#MODULE_NUMBER#=com.mytechia.robobo.framework.hri.vision.lineDetection.opencv.OpencvLineDetectionModule`
`robobo.module.#MODULE_NUMBER#=com.mytechia.robobo.framework.hri.vision.laneDetection.opencv.OpencvLaneDetectionModule`
