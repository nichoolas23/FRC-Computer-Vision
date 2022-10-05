package compvision.testing;

import static compvision.testing.Main.solve.start;
import static org.opencv.imgproc.Imgproc.contourArea;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.event.*;
import java.awt.*;
import javax.swing.*;
import org.opencv.calib3d.StereoBM;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.*;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Main extends TargetData {

  public static void main(String[] args) {

    System.load("C:\\Users\\koala\\Downloads\\opencv\\build\\java\\x64\\opencv_java460.dll");
    Mat frame = new Mat();
    VideoCapture camera = new VideoCapture(0);
    camera.read(frame);

    start();

    JFrame jframe = new JFrame("Video");
    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JLabel vidpanel = new JLabel();
    vidpanel.setSize(frame.width(), frame.height());
    jframe.setContentPane(vidpanel);
    jframe.setSize(frame.width(), frame.height());

    jframe.setVisible(true);
    while (true) {
      if (camera.read(frame)) {
        GripPipeline detectpubs = new GripPipeline();
        detectpubs.process(frame);
        var contours = detectpubs.findContoursOutput();
        frame = ContourDisplay(contours, frame);
        ImageIcon image = new ImageIcon(Mat2BufferedImage(frame));
        vidpanel.setSize(frame.width(), frame.height());
        vidpanel.setIcon(image);
        vidpanel.repaint(); //updates frame with new video frame

        StereoBM.create(16);
      }
    }
  }

  public static final String ANSI_RESET = "\u001B[0m";

  public static final String ANSI_GREEN = "\u001B[32m"; //color text quick why not

  public static Mat ContourDisplay(ArrayList<MatOfPoint> contours, Mat frame) {
    //double alist = 0;
   // int k = 0;
   // List<Double> averageRect = new ArrayList<>();
    // ^^use for getting ratio data
    int i = 0;
    List<TargetData> targetList = new ArrayList<>();
    for (MatOfPoint contour : contours) {

      var rect = Imgproc.boundingRect(contour);
      var rectArea = rect.area();
      //(contArea / rectArea > .007 && contArea / rectArea < .02)
      //(contArea / rectArea > B.getValue()/1000.0 && contArea / rectArea < G.getValue()/1000.0) {
      //^^old method of filtering might use later, works well filtering straight not, not good with angles
      double contArea = 0;
      double targetFullnessRatio = 0.0;

      Imgproc.drawContours(frame, GripPipeline.convexHullsOutput, -1, new Scalar(0, 0, 255));
      for (var convexHulls : GripPipeline.convexHullsOutput) {

        if (i < GripPipeline.origContour.size()) {
          contArea = contourArea(GripPipeline.origContour.get(i));
        } // needed to make sure there is no indexoutofbounds error because opencv for java never got around to really fixing convex hull ops :))))))))
        i++;

        var convexArea = contourArea(convexHulls);
        targetFullnessRatio = contArea / convexArea;
        if(!Double.isNaN(targetFullnessRatio) && targetFullnessRatio != 0.0){
          System.out.println(targetFullnessRatio);

        }

        //contour.depth();
        //Point2D.distance(contour.)
        if (targetFullnessRatio > .1 && targetFullnessRatio < 0.25) { //Aspect ratio checking
          TargetData target = new TargetData();

          Imgproc.rectangle(frame, rect, new Scalar(255, 0, 0), 1);
          target.setTargetCenter(new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0));
          Imgproc.circle(frame, target.getTargetCenter(), 1, new Scalar(0, 0, 255), 1);

          target.setTargetX((target.getTargetCenter().x - (frame.width() / 2.0)) / (frame.width() / 2.0));
          target.setTargetY((target.getTargetCenter().y - (frame.height() / 2.0)) / (frame.height() / 2.0));
          targetList.add(target);
        }
      }
    }
    for (var target : targetList){
      DecimalFormat decimalFormat = new DecimalFormat("0.0000");
      var targetCoordsX = (decimalFormat.format(target.getTargetX()) + "X");
      var targetCoordsY = (decimalFormat.format(target.getTargetY()) + "Y");

      // Displays target x & y position relative to center of camera.

      Imgproc.putText(frame, targetCoordsX, new Point(target.getTargetCenter().x + 40, target.getTargetCenter().y - 10),
          4, .4, new Scalar(255, 0, 0), 1);
      Imgproc.putText(frame, targetCoordsY, new Point(target.getTargetCenter().x + 40, target.getTargetCenter().y + 10),
          4, .4, new Scalar(255, 0, 0), 1);
      Imgproc.arrowedLine(frame, new Point(frame.width() / 2.0, frame.height() / 2.0),
          target.getTargetCenter(), new Scalar(255, 0, 0)); // Draws line to center of target
    }
    return frame;
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

  public static void displayImage(Image img2) {
    ImageIcon icon = new ImageIcon(img2);
    JFrame frame = new JFrame();
    frame.setLayout(new FlowLayout());
    frame.setSize(img2.getWidth(null) + 50, img2.getHeight(null) + 50);
    JLabel lbl = new JLabel();
    lbl.setIcon(icon);
    frame.add(lbl);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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