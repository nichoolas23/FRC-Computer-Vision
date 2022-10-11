package compvision.CamCalibration;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Calibration {
  final String picFolderPathString = "C:\\Users\\koala\\Downloads\\photonvision-settings (1)\\calibImgs";
  final Path picFolderPath = Paths.get(picFolderPathString);

  static {
    System.load("C:\\Users\\koala\\Downloads\\opencv\\build\\java\\x64\\opencv_java460.dll");
  }


  public void processer() {
    if (!Files.exists(picFolderPath)) {
      System.err.println("The picture folder does not exist!");
      return;
    }
    if (!Files.isDirectory(picFolderPath)) {
      System.err.println("The path is not a folder!");
      return;
    }

    List<Mat> imagePoints = new ArrayList<>();
    final Size patternSize = new Size(6, 9);

    List<Mat> outputFindChessboardCorners = new ArrayList<>();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(picFolderPath)) {
      for (Path path : ds) {
        System.out.println(path.toString());

        final Optional<Mat> outputMat = findChessboardCorners(path.toString(), imagePoints, patternSize);

        if (outputMat.isPresent()) {
          outputFindChessboardCorners.add(outputMat.get());
          System.out.println("successful to find corners.");
        } else {
          System.err.println("unsuccessful to find corners.");
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    List<Mat> objectPoints = getObjectPoints(outputFindChessboardCorners.size(), patternSize);

    final Size imageSize = outputFindChessboardCorners.get(0).size();


    Mat cameraMatrix = new Mat(), distortionCoefficients = new Mat();
    List<Mat> rotationMatrixs = new ArrayList<>(), translationVectors = new ArrayList<>();

    System.out.println("ni: " + objectPoints.get(0).checkVector(3, CvType.CV_32F));
    System.out.println(objectPoints.get(0).dump());
    System.out.println("ni1: " + imagePoints.get(0).checkVector(2, CvType.CV_32F));
    System.out.println(imagePoints.get(0).dump());

    var x = Calib3d.calibrateCamera(objectPoints, imagePoints, imageSize,
        cameraMatrix, distortionCoefficients, rotationMatrixs, translationVectors);


    System.out.println("CameraMatrix: " + cameraMatrix.dump());
    System.out.println("DistortionCoefficients: " + distortionCoefficients.dump());

    Map<String, Mat> exportMats = new HashMap<>();
    exportMats.put("CameraMatrix", cameraMatrix);
    exportMats.put("DistortionCoefficients", distortionCoefficients);
    final Path exportFilePath = Paths.get("C:\\Users\\koala\\Downloads\\CalibrationOutput\\output.xml");
    MatIO.exportMat(exportMats, exportFilePath);


    System.out.println("FINISH!");
  }

  public List<Mat> getObjectPoints(int size, Size patternSize) {
    List<Mat> objectPoints = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      objectPoints.add(getObjectPoint(patternSize));
    }
    return objectPoints;
  }

  public MatOfPoint3f getObjectPoint(Size patternSize) {
    MatOfPoint3f objectPoint = new MatOfPoint3f();

    List<Point3> objectPoint_ = new ArrayList<>();
    // final Size patternSize = new Size(6, 9);
    for (int row = 0; row < patternSize.height; row++) {
      for (int col = 0; col < patternSize.width; col++) {
        objectPoint_.add(getPoint(row, col));
      }
    }

    objectPoint.fromList(objectPoint_);
    return objectPoint;
  }

  public Point3 getPoint(int row, int col) {
    final double REAL_HEIGHT = 20.0, REAL_WIDTH = 20.0;
    return new Point3(col * REAL_WIDTH, row * REAL_HEIGHT, 0.0);
  }

  public Optional<Mat> findChessboardCorners(String picPathString, List<Mat> imagePoints, Size patternSize) {
    Mat inputMat = Imgcodecs.imread(picPathString);
    Mat mat = inputMat.clone();
    // final Size patternSize = new Size(6, 9);
    MatOfPoint2f corners = new MatOfPoint2f();

    Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGR2GRAY);

    final boolean canFindChessboard = Calib3d.findChessboardCorners(inputMat, patternSize, corners);

    if (!canFindChessboard) {
      System.err.println("Cannot find Chessboard Corners.");
      return Optional.empty();
    }

    imagePoints.add(corners);

    Calib3d.drawChessboardCorners(mat, patternSize, corners, true);

    Path picPath = Paths.get(picPathString);
    Path folderPath = Paths.get("C:\\Users\\koala\\Downloads\\CalibrationOutput");
    Path path = Paths.get(folderPath.toString(), picPath.getFileName().toString());

    if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
      try {
        System.out.println("There was no folder, so it is createing a folder. : " + folderPath);
        Files.createDirectory(folderPath);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }

    Imgcodecs.imwrite(path.toString(), mat);

    return Optional.of(inputMat);
  }
}