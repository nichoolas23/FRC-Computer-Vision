package compvision.testing;

import static compvision.testing.Main.solve.start;
import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.imgproc.Imgproc.contourArea;

import compvision.CamCalibration.Calibration;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Main extends TargetData {


  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_GREEN = "\u001B[32m"; //color text quick why not

  public static void main(String[] args) {

    System.load(System.getenv("opencvPath")); // "C:\Users\Username\Downloads\opencv\build\java\x64\opencv_java460.dll"

    //var standard = new Mat();
    var undist = new Mat();
    //JFrame jframe = new JFrame("Video");
    var camera = new VideoCapture(0);

    camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 1280);
    camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 720);
    camera.read(undist);
    start();
    //var main = new Main();
    new Calibration().processer();
    var frame = new Mat();
    var vidpanel = new JLabel();
    var vidpanelNorm = new JLabel();

    while (true) {
      if (camera.read(undist)) {
        Mat maxTrix = new Mat(3, 3, CV_32FC1);
        GripPipeline detectpubs = new GripPipeline(); // distortion coefficients
        maxTrix.put(0, 0, 1134.224478355993);
        maxTrix.put(0, 1, 0);
        maxTrix.put(0, 2, 657.0822079238818);
        maxTrix.put(1, 0, 0);
        maxTrix.put(1, 1, 1134.534512866808);
        maxTrix.put(1, 2, 351.0793471736803);
        maxTrix.put(2, 0, 0);
        maxTrix.put(2, 1, 0);
        maxTrix.put(2, 2, 1);
        Mat distco = new Mat(1, 5, CV_32FC1);
        distco.put(0, 0, 0.1297334884159764);
        distco.put(0, 1, -0.9730374784207438);
        distco.put(0, 2, -0.002135406911210138);
        distco.put(0, 3, -0.003738121446973457);
        distco.put(0, 4, 1.852758390710876);

        var newCameraMatrix = Calib3d.getOptimalNewCameraMatrix(maxTrix, distco,
            new Size(1780, 720), 1);
        Calib3d.undistort(undist, frame, newCameraMatrix, distco);
        //Imgproc.GaussianBlur(frame, frame, new Size(41, 41), 2, 2);
        detectpubs.process(frame);
        //Imgproc.adaptiveThreshold(frame,frame,20,ADAPTIVE_THRESH_GAUSSIAN_C,THRESH_BINARY,21,0);
       /* Imgproc.erode(frame, frame, new Mat(), new Point(), 2, BORDER_CONSTANT);
        Imgproc.dilate(frame, frame, new Mat(), new Point(), 3, BORDER_CONSTANT);*/

        var contours = detectpubs.findContoursOutput();
        Imgproc.drawContours(frame, contours, 0, new Scalar(255, 255, 0), 2, 3);
        ContourDisplay(contours, frame);
        vidpanelNorm = VideoFeedControl.CreateVideo(""
            + "normal", undist, vidpanelNorm);
        vidpanel = VideoFeedControl.CreateVideo("map1", frame, vidpanel);
      }
    }
  }

  public static void ContourDisplay(ArrayList<MatOfPoint> contours, Mat frame) {
    //double alist = 0;
    // int k = 0;
    // List<Double> averageRect = new ArrayList<>();
    // ^^use for getting ratio data

    int i = 0;
    List<TargetData> targetList = new ArrayList<>();
    for (MatOfPoint contour : contours) {
      var rect = Imgproc.boundingRect(contour);
      double contArea = 0;
      double targetFullnessRatio = 0.0;

      Imgproc.drawContours(frame, GripPipeline.convexHullsOutput, -1, new Scalar(0, 0, 255));
      for (var convexHulls : GripPipeline.convexHullsOutput) {

        if (i < GripPipeline.origContour.size()) {
          contArea = contourArea(GripPipeline.origContour.get(i));
        } // needed to make sure there is no indexoutofbounds error because opencv for java never got around to really fixing convex hull ops :))))))))
        i++;
        targetFullnessRatio = getTargetFullnessRatio(contArea, convexHulls);
        checkAspectRatio(frame, targetList, rect, targetFullnessRatio);
      }
    }
    for (var target : targetList) { // goes through target list adding target data to screen
      DecimalFormat decimalFormat = new DecimalFormat("0.0000");
      var targetCoordsX = (decimalFormat.format(target.getTargetX()) + "X");
      var targetCoordsY = (decimalFormat.format(target.getTargetY()) + "Y");

      // Displays target x & y position relative to center of camera.

      Imgproc.putText(frame, targetCoordsX,
          new Point(target.getTargetCenter().x + 40, target.getTargetCenter().y - 10),
          4, .4, new Scalar(255, 0, 0), 1);
      Imgproc.putText(frame, targetCoordsY,
          new Point(target.getTargetCenter().x + 40, target.getTargetCenter().y + 10),
          4, .4, new Scalar(255, 0, 0), 1);
      Imgproc.arrowedLine(frame, new Point(frame.width() / 2.0, frame.height() / 2.0),
          target.getTargetCenter(), new Scalar(255, 0, 0)); // Draws line to center of target
    }
  }

  /**
   *
   * @param frame main video frame
   * @param targetList list of TargetData class, contains x,y, and center position
   * @param rect A rectangle around target contour
   * @param targetFullnessRatio guess and check
   */
  private static void checkAspectRatio(Mat frame, List<TargetData> targetList, Rect rect,
      double targetFullnessRatio) {
    if (targetFullnessRatio > .1 && targetFullnessRatio < 0.28) { //Aspect ratio checking
      TargetData target = new TargetData();
      Imgproc.rectangle(frame, rect, new Scalar(255, 0, 0), 1);
      target.setTargetCenter(new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0));
      Imgproc.circle(frame, target.getTargetCenter(), 1, new Scalar(0, 0, 255), 1);

      target.setTargetX(
          (target.getTargetCenter().x - (frame.width() / 2.0)) / (frame.width() / 2.0));
      target.setTargetY(
          (target.getTargetCenter().y - (frame.height() / 2.0)) / (frame.height() / 2.0));
      targetList.add(target);
    }
  }


  private static double getTargetFullnessRatio(double contArea, MatOfPoint convexHulls) {
    double targetFullnessRatio;
    var convexArea = contourArea(convexHulls);
    targetFullnessRatio = contArea / convexArea;
    if (!Double.isNaN(targetFullnessRatio) && targetFullnessRatio != 0.0) {
      System.out.println(targetFullnessRatio);

    }
    return targetFullnessRatio;
  }


  public static BufferedImage Mat2BufferedImage(Mat m) {
    int bufferSize = m.channels() * m.cols() * m.rows();
    byte[] b = new byte[bufferSize];
    m.get(0, 0, b); // get all the pixels
    BufferedImage image = new BufferedImage(m.cols(), m.rows(), BufferedImage.TYPE_3BYTE_BGR);
    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    System.arraycopy(b, 0, targetPixels, 0, b.length);
    return image;
  }

  static class solve extends JFrame implements ChangeListener {

    // frame
    static JFrame f;

    // slider
    static JSlider B;
    static JSlider G;
    static JSlider R;

    // label

    static JLabel l;

    // main class
    public static void start() { // for fine-tuning ratios or BGR values or whatever ig

      f = new JFrame("Edit ratio");

      solve s = new solve();
      l = new JLabel();
      JPanel panel = new JPanel();
      var d = new JSlider[]{
          B = new JSlider(0, 100, 0),
          G = new JSlider(0, 100, 0),
          R = new JSlider(0, 255, 0),
      };

      for (var sliders : d) {
        sliders.setPaintTrack(true);
        sliders.setPaintTicks(true);
        sliders.setPaintLabels(true);

        sliders.setMajorTickSpacing(10);
        sliders.setMinorTickSpacing(1);

        sliders.addChangeListener(s);

        sliders.setOrientation(SwingConstants.VERTICAL);

        sliders.setFont(new Font("Serif", Font.ITALIC, 20));

        // add slider to panel
        panel.add(sliders);
        panel.add(l);

        f.add(panel);

        // set the text of label
        l.setText("value of Slider is =" + sliders.getValue());

        // set the size of frame
        f.setSize(300, 300);

        f.show();
      }
    }

    // if JSlider value is changed
    public void stateChanged(ChangeEvent e) {
      l.setText(
          "value of Slider is =" + B.getValue() / 10.0 + "," + G.getValue() / 10.0);
    }
  }
}