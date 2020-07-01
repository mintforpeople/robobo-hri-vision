package com.mytechia.robobo.framework.hri.vision.laneDetection;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static com.mytechia.robobo.framework.hri.vision.laneDetection.LaneParameters.PREVIOUS_FIT_MARGIN;
import static com.mytechia.robobo.framework.hri.vision.laneDetection.LaneParameters.WINDOW_MARGIN;
import static com.mytechia.robobo.framework.hri.vision.laneDetection.LaneParameters.WINDOW_MIN_PIX;
import static org.opencv.core.CvType.CV_32S;

/**
 * TODO: Add the option to calculate lanes based on previous detections
 * Check naming convention
 *
 */

public class Line {
    //todo: parameterize
    static double ym_per_pix = 30.0 / 720,   // meters per pixel in y dimension
            xm_per_pix = 3.7 / 700;  // meters per pixel in x dimension
    public boolean detected = false;
    public double[] lastFitPixel = null;
    LinkedList<Point> allPoints;
    private double[] lastFitMeter = null;
    private Deque<double[]> recentFitsPixel = new LinkedList<>();
    private Deque<double[]> recentFitsMeter = new LinkedList<>();
    private int bufferLen = 10;

    public Line() {

    }

    public Line(int bufferLen) {
        this.bufferLen = bufferLen;
    }

    /**
     * Get polynomial coefficients for lane-lines detected in an binary image.
     *
     * @param birdeyeBinary: input bird's eye view binary image
     * @param line_lt:        left lane-line previously detected
     * @param line_rt:        left lane-line previously detected
     * @param n_windows:      number of sliding windows used to search for the lines
     * @return updated lane lines
     */
    public static Line[] getFitsBySlidingWindows(Mat birdeyeBinary,
                                                 Line line_lt,
                                                 Line line_rt,
                                                 int n_windows) {


        int height = birdeyeBinary.rows(),
                width = birdeyeBinary.cols();

        // Take a histogram of the bottom half of the image
        Mat halfBirdeyeBinary = birdeyeBinary.rowRange(height / 2, height);
        Mat histogram = new Mat(new Size(width, 1), CV_32S, new Scalar(0));
        for (int j = 0; j < halfBirdeyeBinary.rows(); j++)
            Core.add(histogram, halfBirdeyeBinary.row(j), histogram, new Mat(), CV_32S);
        // This line may be extra, since the max will always be the max, but still, just in case
        histogram.convertTo(histogram, -1, 1.0 / 255);

        // Find the peak of the left and right halves of the histogram
        // These will be the starting point for the left and right lines
        int midpoint = width / 2;
        int leftxBase = (int) Core.minMaxLoc(histogram.colRange(0, midpoint)).maxLoc.x;
        int rightxBase = (int) Core.minMaxLoc(histogram.colRange(midpoint, width)).maxLoc.x + midpoint;

        // Set height of windows
        int window_height = height / n_windows;

        // Identify the x and y positions of all nonzero pixels in the image
        Mat idxp = new Mat(); //MatOfPoint with nonzero pixels
        Core.findNonZero(birdeyeBinary, idxp);

        // If there is no pixel then we have an empty array, else it may crash
        Point[] idx = idxp.dims() > 0 ? new MatOfPoint(idxp).toArray() : new Point[0];

        // Current positions to be updated for each window
        int leftxCurrent = leftxBase;
        int rightxCurrent = rightxBase;

        //todo: paramereter!
        int margin = WINDOW_MARGIN;  // width of the windows +/- margin
        int minpix = WINDOW_MIN_PIX;   // minimum number of pixels found to recenter window

        // Create empty lists to receive left and right lane pixel positions
        LinkedList<Point> rightLanePoints = new LinkedList<>();
        LinkedList<Point> left_lane_points = new LinkedList<>();

        short leftEmptyCounts = 0,
                rightEmptyCounts = 0,
                leftTotalEmptyCounts = 0,
                rightTotalEmptyCounts = 0;
        // Step through the windows one by one
        for (int window = 0; window < (n_windows); ++window) {
            // Identify window boundaries in x and y (and right and left)
            int win_y_low = height - (window + 1) * window_height;
            int win_y_high = height - window * window_height;
            int win_xleft_low = leftxCurrent - margin;
            int win_xleft_high = leftxCurrent + margin;
            int win_xright_low = rightxCurrent - margin;
            int win_xright_high = rightxCurrent + margin;

            // Means of the pixels detected for each line
            long lxmean = 0, rxmean = 0;

            List<Point> grightLanePoints = new LinkedList<>();
            List<Point> gleftLanePoints = new LinkedList<>();

            // Identify the nonzero pixels in x and y within the window
            for (Point p : idx) {

                if (((p.y >= win_y_low) &&
                        (p.y < win_y_high) &&
                        (p.x >= win_xleft_low) &&
                        (p.x < win_xleft_high))) {
                    gleftLanePoints.add(p);
                    lxmean += p.x;
                }
                if ((p.y >= win_y_low) &&
                        (p.y < win_y_high) &&
                        (p.x >= win_xright_low) &&
                        (p.x < win_xright_high)) {
                    grightLanePoints.add(p);
                    rxmean += p.x;
                }
            }
            if (grightLanePoints.size() > 0 && rightEmptyCounts < 2) {

                rightLanePoints.addAll(grightLanePoints);
                if (grightLanePoints.size() > minpix)
                    rightxCurrent = (int) (rxmean / grightLanePoints.size());
                rightEmptyCounts = 0;
            } else {
                rightEmptyCounts += 1;
                rightTotalEmptyCounts += 1;
            }
            if (gleftLanePoints.size() > 0 && leftEmptyCounts < 2) {
                left_lane_points.addAll(gleftLanePoints);

                // If you found > minpix pixels, recenter next window on their mean position
                if (gleftLanePoints.size() > minpix)
                    leftxCurrent = (int) (lxmean / gleftLanePoints.size());
                leftEmptyCounts = 0;
            } else {
                leftEmptyCounts += 1;
                leftTotalEmptyCounts += 1;
            }


        }

//      Extract left and right line pixel positions

        line_lt.allPoints = left_lane_points;
        line_rt.allPoints = rightLanePoints;

        boolean lDetected = true;
        boolean rDetected = true;
        double[] leftFitPixel;
        double[] leftFitMeter;
        double[] rightFitPixel;
        double[] rightFitMeter;
        if (line_lt.allPoints.size() == 0||leftTotalEmptyCounts>=n_windows*.7) {
            leftFitPixel = new double[]{0, 0, 0};
            leftFitMeter = new double[]{0, 0, 0};
            lDetected = false;
        } else {
            leftFitPixel = invertedPolyRegression(line_lt.allPoints.toArray(), 1, 1);
            leftFitMeter = invertedPolyRegression(line_lt.allPoints.toArray(), xm_per_pix, ym_per_pix);
        }

        if (line_rt.allPoints.size() == 0||rightTotalEmptyCounts>=n_windows*.7) {
            rightFitPixel = new double[]{0, 0, 0};
            rightFitMeter = new double[]{0, 0, 0};
            rDetected= false;
        } else {
            rightFitPixel = invertedPolyRegression(line_rt.allPoints.toArray(), 1, 1);
            rightFitMeter = invertedPolyRegression(line_rt.allPoints.toArray(), xm_per_pix, ym_per_pix);
        }
        line_lt.updateLine(leftFitPixel, leftFitMeter, lDetected, false);
        line_rt.updateLine(rightFitPixel, rightFitMeter, rDetected, false);


        return new Line[]{line_lt, line_rt};//, out_img
    }

    /**
     * A function to get the coefficients of a 2nd degree polynomial that fits the observations
     * NOTE: it takes points but flips the x and y
     *
     * @param objects Array of Point with the "observations"
     * @param alfa    Weight on the X axis
     * @param beta    Weight on the Y axis
     * @return Array of double with the 3 coefficients in the order of {a b c} (where ax^2 + bx + c)
     */
    private static double[] invertedPolyRegression(Object[] objects, double alfa, double beta) {
        int n = 2;                       //degree of polynomial to fit the data
        int N = objects.length;                       //no. of data points
        double[] x = new double[N];         //array to store x-axis data points
        double[] y = new double[N];
        for (int i = 0; i < N; i++) {
            Point p = (Point) objects[i];
            x[i] = p.y * beta;
            y[i] = p.x * alfa;
        }
        double X[] = new double[2 * n + 1];
        for (int i = 0; i < 2 * n + 1; i++) {
            X[i] = 0;
            for (int j = 0; j < N; j++)
                X[i] = X[i] + Math.pow(x[j], i);        //consecutive positions of the array will store N,sigma(xi),sigma(xi^2),sigma(xi^3)....sigma(xi^2n)
        }
        double B[][] = new double[n + 1][n + 2], a[] = new double[n + 1];            //B is the Normal matrix(augmented) that will store the equations, 'a' is for value of the final coefficients
        for (int i = 0; i <= n; i++)
            for (int j = 0; j <= n; j++)
                B[i][j] = X[i + j];            //Build the Normal matrix by storing the corresponding coefficients at the right positions except the last column of the matrix
        double Y[] = new double[n + 1];                    //Array to store the values of sigma(yi),sigma(xi*yi),sigma(xi^2*yi)...sigma(xi^n*yi)
        for (int i = 0; i < n + 1; i++) {
            Y[i] = 0;
            for (int j = 0; j < N; j++)
                Y[i] = Y[i] + Math.pow(x[j], i) * y[j];        //consecutive positions will store sigma(yi),sigma(xi*yi),sigma(xi^2*yi)...sigma(xi^n*yi)
        }
        for (int i = 0; i <= n; i++)
            B[i][n + 1] = Y[i];                //load the values of Y as the last column of B(Normal Matrix but augmented)
        n = n + 1;
        for (int i = 0; i < n; i++)                    //From now Gaussian Elimination starts(can be ignored) to solve the set of linear equations (Pivotisation)
            for (int k = i + 1; k < n; k++)
                if (B[i][i] < B[k][i])
                    for (int j = 0; j <= n; j++) {
                        double temp = B[i][j];
                        B[i][j] = B[k][j];
                        B[k][j] = temp;
                    }

        for (int i = 0; i < n - 1; i++)            //loop to perform the gauss elimination
            for (int k = i + 1; k < n; k++) {
                double t = B[k][i] / B[i][i];
                for (int j = 0; j <= n; j++)
                    B[k][j] = B[k][j] - t * B[i][j];    //make the elements below the pivot elements equal to zero or elimnate the variables
            }
        for (int i = n - 1; i >= 0; i--)                //back-substitution
        {                        //x is an array whose values correspond to the values of x,y,z..
            a[i] = B[i][n];                //make the variable to be calculated equal to the rhs of the last equation
            for (int j = 0; j < n; j++)
                if (j != i)            //then subtract all the lhs values except the coefficient of the variable whose value is being calculated
                    a[i] = a[i] - B[i][j] * a[j];
            a[i] = a[i] / B[i][i];            //now finally divide the rhs by the coefficient of the variable to be calculated
        }
        return new double[]{a[2], a[1], a[0]};
    }

    /**
     * Get polynomial coefficients for lane-lines detected in an binary image.
     * This function starts from previously detected lane-lines to speed-up the search of lane-lines in the current frame.
     *
     * @param birdeye_binary: input bird's eye view binary image
     * @param line_lt:        left lane-line previously detected
     * @param line_rt:        left lane-line previously detected
     * @return updated lane lines
     **/
    public static Line[] getFitsByPreviousFits(Mat birdeye_binary,
                                               Line line_lt,
                                               Line line_rt) {


        int height = birdeye_binary.rows(),
                width = birdeye_binary.cols();

        double[] leftFitPixel;
        double[] rightFitPixel;

        leftFitPixel = line_lt.lastFitPixel;
        rightFitPixel = line_rt.lastFitPixel;

        Mat idxp = new Mat();
        Core.findNonZero(birdeye_binary, idxp);

        Point[] nonzero = idxp.dims() > 0 ? new MatOfPoint(idxp).toArray() : new Point[0];

        int margin = PREVIOUS_FIT_MARGIN;

        LinkedList<Point> right_lane_points = new LinkedList<>();
        LinkedList<Point> left_lane_points = new LinkedList<>();
        for (Point p :
                nonzero) {
            double if1 = leftFitPixel[0] * (p.y * p.y) + leftFitPixel[1] * p.y + leftFitPixel[2];
            double if2 = rightFitPixel[0] * (p.y * p.y) + rightFitPixel[1] * p.y + rightFitPixel[2];
            if ((p.x > (if1 - margin)) &&
                    (p.x < (if1 + margin)))
                left_lane_points.add(p);
            if ((p.x > (if2 - margin)) &&
                    (p.x < (if2 + margin)))
                right_lane_points.add(p);
        }
        line_lt.allPoints = left_lane_points;
        line_rt.allPoints = right_lane_points;

        boolean detected = true;
//        double [] leftFitPixel;
//        double [] rightFitPixel;
        double[] left_fit_meter;
        double[] right_fit_meter;
        if (line_lt.allPoints.size() == 0) {
            leftFitPixel = line_lt.lastFitPixel;
            left_fit_meter = line_lt.lastFitMeter;
            detected = false;
        } else {
            leftFitPixel = invertedPolyRegression(line_lt.allPoints.toArray(), 1, 1);
            left_fit_meter = invertedPolyRegression(line_lt.allPoints.toArray(), xm_per_pix, ym_per_pix);
        }

        if (line_rt.allPoints.size() == 0) {
            rightFitPixel = line_rt.lastFitPixel;
            right_fit_meter = line_rt.lastFitMeter;
            detected = false;
        } else {
            rightFitPixel = invertedPolyRegression(line_rt.allPoints.toArray(), 1, 1);
            right_fit_meter = invertedPolyRegression(line_rt.allPoints.toArray(), xm_per_pix, ym_per_pix);
        }
        line_lt.updateLine(leftFitPixel, left_fit_meter, detected, false);
        line_rt.updateLine(rightFitPixel, right_fit_meter, detected, false);


        return new Line[]{line_lt, line_rt};//, img_fit

    }

    public void draw(Mat mask, Scalar color, int line_width, boolean average) {
        double[] coeffs = average ? this.average_fit() : this.lastFitPixel;

        if (coeffs[0] == 0 && coeffs[1] == 0 && coeffs[2] == 0)
            return;

        int h = mask.rows(),
                w = mask.cols(),
                c = mask.channels();

        List<MatOfPoint> list = new ArrayList<MatOfPoint>();
        MatOfPoint mop = new MatOfPoint();
        List<Point> points = new ArrayList<>();

        for (int i = 0; i < h; i++)
            points.add(new Point(coeffs[0] * (i * i) + coeffs[1] * i + coeffs[2],
                    i));
        mop.fromList(points);
        list.add(mop);
        Imgproc.polylines(mask, list, false, color, line_width);
    }

    // clear buffer default is false
    void updateLine(double[] new_fit_pixel,
                    double[] new_fit_meter,
                    boolean detected,
                    boolean clear_buffer) {
        this.detected = detected;
        if (clear_buffer) {
            recentFitsMeter.clear();
            recentFitsPixel.clear();
        }
        if (new_fit_meter == null || new_fit_pixel == null)
            return;
        this.lastFitMeter = new_fit_meter;
        this.lastFitPixel = new_fit_pixel;

        add_to_recent(recentFitsPixel, lastFitPixel);
        add_to_recent(recentFitsMeter, lastFitMeter);
    }

    private void add_to_recent(Deque<double[]> recent, double[] last) {
        if (recent.size() == bufferLen)
            recent.removeFirst();
        recent.add(last);
    }

    public double[] average_fit() {
        double[] res = new double[3];
        for (double[] o :
                recentFitsPixel) {
            res[0] += o[0];
            res[1] += o[1];
            res[2] += o[2];
        }
        res[0] /= recentFitsPixel.size();
        res[1] /= recentFitsPixel.size();
        res[2] /= recentFitsPixel.size();
        return res;
    }

    // radius of curvature of the line (averaged)
    double curvature() {
        int y_eval = 0;
        double[] coeffs = average_fit();
        return (Math.pow(1 + Math.pow(2 * coeffs[0] * y_eval + coeffs[1], 2), 1.5)) / Math.abs(2 * coeffs[0]);
    }

    // radius of curvature of the line (averaged)
    double curvature_meter() {
        int y_eval = 0;
        double[] coeffs = new double[3];
        for (double[] o :
                recentFitsMeter) {
            coeffs[0] += o[0];
            coeffs[1] += o[1];
            coeffs[2] += o[2];
        }
        coeffs[0] /= recentFitsMeter.size();
        coeffs[1] /= recentFitsMeter.size();
        coeffs[2] /= recentFitsMeter.size();
        return (Math.pow(1 + Math.pow(2 * coeffs[0] * y_eval + coeffs[1], 2), 1.5)) / Math.abs(2 * coeffs[0]);
    }

}
