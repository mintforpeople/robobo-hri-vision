package com.mytechia.robobo.framework.hri.vision.laneDetection.opencv;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.hri.vision.basicCamera.Frame;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraListener;
import com.mytechia.robobo.framework.hri.vision.basicCamera.ICameraModule;
import com.mytechia.robobo.framework.hri.vision.laneDetection.ALaneDetectionModule;
import com.mytechia.robobo.framework.hri.vision.laneDetection.ILaneDetectionListener;
import com.mytechia.robobo.framework.hri.vision.laneDetection.Line;
import com.mytechia.robobo.framework.remote_control.remotemodule.Command;
import com.mytechia.robobo.framework.remote_control.remotemodule.ICommandExecutor;
import com.mytechia.robobo.framework.remote_control.remotemodule.IRemoteControlModule;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.mytechia.robobo.framework.hri.vision.laneDetection.LaneParameters.EQUALIZED_THRESHOLD;
import static com.mytechia.robobo.framework.hri.vision.laneDetection.LaneParameters.SOBEL_KERNEL_SIZE;
import static com.mytechia.robobo.framework.hri.vision.laneDetection.LaneParameters.SOBEL_THRESHOLD;
import static com.mytechia.robobo.framework.hri.vision.laneDetection.LaneParameters.YELLOW_HSV_TH_MAX;
import static com.mytechia.robobo.framework.hri.vision.laneDetection.LaneParameters.YELLOW_HSV_TH_MIN;
import static com.mytechia.robobo.framework.hri.vision.laneDetection.Line.get_fits_by_previous_fits;
import static com.mytechia.robobo.framework.hri.vision.laneDetection.Line.get_fits_by_sliding_windows;

public class OpencvAdvanceLaneDetection extends ALaneDetectionModule implements ICameraListener {

    private ICameraModule cameraModule;
    private Executor executor;


    private Line line_lt = new Line(), line_rt = new Line();
    private boolean processed_frames;
    private boolean processing = false;

    @Override
    public void pauseDetection() {
        cameraModule.unsuscribe(this);
    }

    @Override
    public void resumeDetection() {
        cameraModule.suscribe(this);
    }

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        m = manager;
        // Load camera and remote control modules
        try {
            rcmodule = m.getModuleInstance(IRemoteControlModule.class);
            cameraModule = m.getModuleInstance(ICameraModule.class);

        } catch (ModuleNotFoundException e) {
            e.printStackTrace();
        }

        cameraModule.suscribe(this);

        executor = Executors.newFixedThreadPool(1);

        rcmodule.registerCommand("START-LANE", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                resumeDetection();
            }
        });

        rcmodule.registerCommand("STOP-LANE", new ICommandExecutor() {
            @Override
            public void executeCommand(Command c, IRemoteControlModule rcmodule) {
                pauseDetection();

            }
        });

        resumeDetection();
    }

    @Override
    public void shutdown() throws InternalErrorException {
        pauseDetection();
    }

    @Override
    public String getModuleInfo() {
        return null;
    }

    @Override
    public String getModuleVersion() {
        return null;
    }

    @Override
    public void onNewFrame(Frame frame) {

    }

    @Override
    public void onNewMat(final Mat mat) {
        if (!processing) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    processing = true;
                    // todo: undistort

//        Core.bitwise_not(mat,mat);

                    // Binarize the image getting the white and yellow parts
                    Mat binary = binarize(mat);

                    // Transform the image to get a bird view
                    Mat[] birdResults = birdeye(binary);
                    Mat img_birdeye = birdResults[0];
                    Mat minv = birdResults[1];

                    // Fit polynomial function
                    if (processed_frames && line_lt.detected && line_rt.detected) {
                        Line[] lines = get_fits_by_previous_fits(img_birdeye, line_lt, line_rt);
                        line_lt = lines[0];
                        line_rt = lines[1];
                    } else {
                        Line[] lines = get_fits_by_sliding_windows(img_birdeye, line_lt, line_rt, 9); //todo: parameterize
                        line_lt = lines[0];
                        line_rt = lines[1];
                        processed_frames=true;
                    }

                    notifyLinesDetected(line_lt, line_rt, minv);

                    processing = false;
                }
            });

        }

    }


    /**
     * Transforms the image to get a bird view of the street
     * @param mat original image
     * @return a Mat array with two elements: the first its the image warped, and the second its the transformation matrix to inverse the warped image
     */
    private Mat[] birdeye(Mat mat) {
        int h = mat.rows(),
                w = mat.cols();

        Mat src = new Mat(4, 2, CvType.CV_32F),
                dst = new Mat(4, 2, CvType.CV_32F),
                m, minv, warped = new Mat();

        // todo: parameterize
        src.put(0, 0, new float[]{w, h});
        src.put(1, 0, new float[]{0, h});
        src.put(2, 0, new float[]{(int) (w * .2), (int) (h * .2)});
        src.put(3, 0, new float[]{(int) (w * .8), (int) (h * .2)});

        dst.put(0, 0, new float[]{w, h});
        dst.put(1, 0, new float[]{0, h});
        dst.put(2, 0, new float[]{0, 0});
        dst.put(3, 0, new float[]{w, 0});

        m = Imgproc.getPerspectiveTransform(src, dst);
        minv = Imgproc.getPerspectiveTransform(dst, src);

        Imgproc.warpPerspective(mat, warped, m, new Size(w, h), Imgproc.INTER_LINEAR);
        return new Mat[]{warped, minv};
    }

    /**
     * Binarize an image extracting edges (using Sobel), yellow and white areas.
     *
     * @param mat Original image that will be binarized
     * @return The Mat object containing the binarized object leaving only the white and yellow parts as 255 values; the rest will be 0
     */
    private Mat binarize(Mat mat) {

        int height = mat.rows(),
                width = mat.cols();

        Mat binary,
                hsv_yellow_mask,
                eqWhiteMask,
                sobel,
                closing = new Mat(),
                ones = Mat.ones(5, 5, CvType.CV_8U),
                grey = new Mat();

        Imgproc.cvtColor(mat, grey, Imgproc.COLOR_BGR2GRAY);

        binary = Mat.zeros(height, width, CvType.CV_8U);

        // Extract the yellow parts
        hsv_yellow_mask = thresh_frame_in_hsv(mat);

        // Extract white parts
        eqWhiteMask = getBinaryFromEqualizedGray(grey);

        // Extract the edges
        sobel = thresh_frame_sobel(grey);

        // Merge all the binary images
        Core.bitwise_or(binary, hsv_yellow_mask, binary);
        Core.bitwise_or(binary, eqWhiteMask, binary);
        Core.bitwise_or(binary, sobel, binary);

        // Close = erode(dilate(img))
        // Used to fill the gaps on the binary image
        Imgproc.morphologyEx(binary, closing, Imgproc.MORPH_CLOSE, ones);


        return closing;
    }

    /**
     * This functions gets an image, then applies an extended Sobel operation on both axis
     * Then thresholds the image to create a binary result.
     *
     * @param mat a Mat object containing a black and white image
     * @return a Mat object containing the resulting binary image
     */
    private Mat thresh_frame_sobel(Mat mat) {
        Mat sobelX = new Mat(),
                sobelY = new Mat(),
                sobelMag = new Mat();

        // Apply the Sobel operation on X and Y axis
        Imgproc.Sobel(mat, sobelX, CvType.CV_64F, 1, 0, SOBEL_KERNEL_SIZE);
        Imgproc.Sobel(mat, sobelY, CvType.CV_64F, 0, 1, SOBEL_KERNEL_SIZE);

        // val=sqrt(x^2 + y^2)
        Core.pow(sobelX, 2, sobelX);
        Core.pow(sobelX, 2, sobelY);
        Core.add(sobelX, sobelY, sobelMag);
        Core.sqrt(sobelMag, sobelMag);

        // Sets every value on a scale of 0-255
        sobelMag.convertTo(sobelMag, CvType.CV_8U, 255.0 / Core.minMaxLoc(sobelMag).maxVal);

        //Thresholds the image; val>SOBEL_THRESHOLD = 255, else 0
        Imgproc.threshold(sobelMag, sobelMag, SOBEL_THRESHOLD, 255, Imgproc.THRESH_BINARY);

        return sobelMag;
    }

    /**
     * This function gets a gray image, equalizes it and then applies a threshold to get the white
     * parts
     *
     * @param grey the gray image to be processed
     * @return a Mat object containing the resulting binary image
     */
    private Mat getBinaryFromEqualizedGray(Mat grey) {
        Mat eq_global = new Mat(),
                th = new Mat();
        // Equalize the image
        // This consists of improving the contrast of the image
        Imgproc.equalizeHist(grey, eq_global);

        //Then threshold the most white pixels
        Imgproc.threshold(eq_global, th, EQUALIZED_THRESHOLD, 255, Imgproc.THRESH_BINARY);

        return th;
    }

    /**
     * This function obtains only the yellow parts of an image, this is designed to extract the yellow lines of a road
     *
     * @param mat Image where the yellow will be "extracted"
     * @return Mat containing the resulting binary image
     */
    private Mat thresh_frame_in_hsv(Mat mat) {
        Mat thresh = new Mat();
        Mat hsv = new Mat();

        Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_RGB2HSV);

        //Get only colors in threshold
        Core.inRange(hsv,
                new Scalar(YELLOW_HSV_TH_MIN[0],
                        YELLOW_HSV_TH_MIN[1],
                        YELLOW_HSV_TH_MIN[2]),
                new Scalar(YELLOW_HSV_TH_MAX[0],
                        YELLOW_HSV_TH_MAX[1],
                        YELLOW_HSV_TH_MAX[2]), thresh);

        //Convert every value > 0 to 255
        Imgproc.threshold(thresh, thresh, 0, 255, Imgproc.THRESH_BINARY);

        return thresh;
    }

    @Override
    public void onDebugFrame(Frame frame, String frameId) {

    }

    @Override
    public void onOpenCVStartup() {

    }
}
