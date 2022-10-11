package compvision.testing;

import static compvision.testing.Main.Mat2BufferedImage;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Mat;

public class VideoFeedControl {

  public static JLabel CreateVideo(String title, Mat videoIn, JLabel vidpanel) {
    var windows = JFrame.getWindows();
    List<String> windowNames = new ArrayList<>();
    for (var window :
        windows) {
      windowNames.add(window.getName());

    }
    if (!windowNames.contains(title)) {
      JFrame jframe = new JFrame(title);
      jframe.setName(title);
      jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      vidpanel = new JLabel();
      vidpanel.setSize(videoIn.width(), videoIn.height());
      jframe.setContentPane(vidpanel);
      jframe.setSize(videoIn.width(), videoIn.height());
      jframe.setVisible(true);

    }

    var buffimage = Mat2BufferedImage(videoIn);

    ImageIcon image = new ImageIcon(buffimage);

    vidpanel.setSize(videoIn.width(), videoIn.height());
    vidpanel.setIcon(image);
    vidpanel.repaint(); //updates frame with new video frame

    return vidpanel;
  }
}
