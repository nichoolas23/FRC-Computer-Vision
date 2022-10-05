package compvision.testing;

import org.opencv.core.Point;

public class TargetData {

  private double targetX;
  private double targetY;
  private Point targetCenter;

  public TargetData() {
  }

  public Point getTargetCenter() {
    return targetCenter;
  }

  public void setTargetCenter(Point targetCenter) {
    this.targetCenter = targetCenter;
  }

  public double getTargetY() {
    return targetY;
  }

  public void setTargetY(double targetY) {
    this.targetY = targetY;
  }

  public double getTargetX() {
    return targetX;
  }

  public void setTargetX(double targetX) {
    this.targetX = targetX;
  }
}



