package br.faustech.gpu;

import br.faustech.bus.Bus;
import br.faustech.memory.MemoryException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

/**
 * This thread processes a video file, converting frames to vertex arrays for rendering.
 */
@Log // Lombok annotation for logging
@RequiredArgsConstructor // Lombok generates a constructor for all final fields
public class VideoFrameToVertexArray extends Thread {

  private final String videoFilePath; // Path to the video file

  private final Java2DFrameConverter converter = new Java2DFrameConverter(); // Converter for frames to images

  private final int width; // Width of the target rendering

  private final int height; // Height of the target rendering

  private final Bus bus; // Bus to write the converted frames

  private final FrameBuffer frameBuffer; // Frame buffer to write the converted frames

  /**
   * Entry point for the thread; begins the video processing.
   */
  @Override
  public void run() {

    this.processVideo();
  }

  /**
   * Processes each frame of the video, converting and writing to the frame buffer.
   */
  private void processVideo() {

    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath)) {
      grabber.start();
      Frame frame;

      while ((frame = grabber.grabImage()) != null) {
        long time = System.currentTimeMillis();

        processFrameAndWriteWithBus(frame);

        time = System.currentTimeMillis() - time;
        long sleepTime = Math.max(0,
            1000 / 60 - time); // Calculate time to delay to maintain frame rate
        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
          log.severe(e.getMessage());
          return;
        }
      }
      grabber.stop();
      this.processVideo(); // Restart video processing to loop continuously
    } catch (Exception e) {
      throw new RuntimeException(String.format("Error processing video: %s", e.getMessage()));
    }
  }

  /**
   * Processes a single frame, resizing and mapping it into the frame buffer using the bus.
   *
   * @param frame The frame to be processed.
   */
  private void processFrameAndWriteWithBus(Frame frame) {

    BufferedImage originalImage = converter.getBufferedImage(frame);
    BufferedImage resizedImage = resizeImage(originalImage, width, height);
    int address = 4100;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        bus.write(address, new int[]{resizedImage.getRGB(x, y)});
        address += 4;
      }
    }
    bus.write(4096, new int[]{0}); // Swap frame buffer
  }

  /**
   * Processes a single frame, resizing and mapping it into the frame buffer.
   *
   * @param frame The frame to be processed.
   * @throws MemoryException If there's an issue writing to the frame buffer.
   */
  private void processFrameAndWriteInBuffer(Frame frame) throws MemoryException {

    BufferedImage originalImage = converter.getBufferedImage(frame);
    BufferedImage resizedImage = resizeImage(originalImage, width, height);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        float normX = (x / (float) width) * 2 - 1;
        float normY = ((height - y) / (float) height) * 2 - 1;

        int color = resizedImage.getRGB(x, y);

        final float[] pixel = new float[]{normX, normY, ((color >> 16) & 0xFF) / 255.0f,  // r
            ((color >> 8) & 0xFF) / 255.0f,                         // g
            (color & 0xFF) / 255.0f,                                // b
            1,                                                      // a
            x / (float) width,                                      // u
            y / (float) height                                      // v
        };

        frameBuffer.writeToBackBufferFromFloats((y * width + x) * 32, pixel);
      }
    }
    frameBuffer.swap();
  }

  /**
   * Resizes a BufferedImage to the specified dimensions.
   *
   * @param originalImage The original BufferedImage.
   * @param targetWidth   The desired width.
   * @param targetHeight  The desired height.
   * @return A new resized BufferedImage.
   */
  private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth,
      int targetHeight) {

    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = resizedImage.createGraphics();
    g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    g2d.dispose();
    return resizedImage;
  }

}
