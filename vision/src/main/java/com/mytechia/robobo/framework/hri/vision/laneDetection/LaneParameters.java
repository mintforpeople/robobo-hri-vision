package com.mytechia.robobo.framework.hri.vision.laneDetection;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.hri.vision.util.AuxPropertyWriter;

public class LaneParameters {
    private static LaneParameters instance;

    // From a orange-ish to a yellow-green-ish
    // Avoiding very washed color of that spectrum
    public final static int[] YELLOW_HSV_TH_MIN = {0, 70, 70};
    public final static int[] YELLOW_HSV_TH_MAX = {50, 255, 255};
    public final static int EQUALIZED_THRESHOLD = 250;
    public final static int SOBEL_KERNEL_SIZE = 9;
    public final static int SOBEL_THRESHOLD = 50;


    public final static int WINDOW_MARGIN = 100 / 3; // Distance from the center of the window to each side
    public final static int WINDOW_MIN_PIX = 50 / 3; // Minimum number of pixels to recenter the window


    // Todo: move this to a file?
    private LaneParameters(){
//    private LaneParameters(RoboboManager manager){
//        AuxPropertyWriter propertyWriter = new AuxPropertyWriter("")
    }

    public static LaneParameters getInstance() {
        if (instance==null)
            instance = new LaneParameters();
        return instance;
    }
}
