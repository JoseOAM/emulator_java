package br.faustech.gpu;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoFrameToByteArray extends Thread {

  private static final Logger LOGGER = Logger.getLogger(VideoFrameToByteArray.class.getName());

  private final VideoBuffer videoBuffer;

  private final String videoFilePath;

  public volatile int width;

  public volatile int height;

  public VideoFrameToByteArray(VideoBuffer videoBuffer, String videoFilePath) {

    this.videoBuffer = videoBuffer;
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
        synchronized (this.videoBuffer) {
          videoBuffer.writeToBackBuffer(frameBytes);
        }
      }
      grabber.stop();
    } catch (Exception e) {
      LOGGER.severe(STR."Error processing video file: \{e.getMessage()}");
    }
  }

  private byte[] frameToByteArray(Frame frame) {

    Java2DFrameConverter converter = new Java2DFrameConverter();
    BufferedImage image = converter.getBufferedImage(frame);

    return ImageReader.readImage(image, this.width, this.height);
  }

  public synchronized void setResolution(final int width, final int height) {

    this.width = width;
    this.height = height;
  }

}
