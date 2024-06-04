package br.faustech.gpu;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoFrameToByteArray implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(VideoFrameToByteArray.class.getName());

  private final VideoMemory videoMemory;

  private final String videoFilePath;

  public VideoFrameToByteArray(VideoMemory videoMemory, String videoFilePath) {

    this.videoMemory = videoMemory;
    this.videoFilePath = videoFilePath;
  }

  @Override
  public void run() {

    processVideo();
  }

  private void processVideo() {

    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath)) {
      grabber.start();

      Frame frame;
      while ((frame = grabber.grabImage()) != null) {
        byte[] frameBytes = frameToByteArray(frame);
        synchronized (this.videoMemory) {
          videoMemory.writeToBackBuffer(frameBytes);
        }
      }
      grabber.stop();
    } catch (Exception e) {
      LOGGER.severe("Error processing video file: " + e.getMessage());
    }
  }

  private static byte[] frameToByteArray(Frame frame) {

    Java2DFrameConverter converter = new Java2DFrameConverter();
    BufferedImage image = converter.getBufferedImage(frame);

    return ImageReader.readImage(image, 800, 600);
  }

}
