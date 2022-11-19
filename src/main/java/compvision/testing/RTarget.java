package compvision.testing;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public record RTarget(double targetX, double targetY, Point targetCenter) {
public RTarget{
//  Imgproc.rectangle(new Mat(), Imgproc.boundingRect(contour), new Scalar(255, 0, 0), 1);
//  (targetCenter) -> {
//
//    Imgproc.rectangle(frame, rect, new Scalar(255, 0, 0), 1);
//  target.setTargetCenter(new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0))
//  }

}
}
