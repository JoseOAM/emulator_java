package br.faustech.gpu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoFrameToVertexArray extends Thread {

  private static final Logger LOGGER = Logger.getLogger(VideoFrameToVertexArray.class.getName());

  private final FrameBuffer frameBuffer;

  private final String videoFilePath;

  public final int width;

  public final int height;

  private final Java2DFrameConverter converter;

  public VideoFrameToVertexArray(FrameBuffer frameBuffer, String videoFilePath, int width,
      int height) {

    this.frameBuffer = frameBuffer;
    this.videoFilePath = videoFilePath;
    this.width = width;
    this.height = height;
    this.converter = new Java2DFrameConverter();
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
        long frameStartTime = System.currentTimeMillis();
        float[] vertexData = frameToVertexData(frame);
        synchronized (this.frameBuffer) {
          frameBuffer.writeToBackBuffer(vertexData);
          frameBuffer.swap();
        }

        // Calculate the time to sleep to maintain approximately 60 FPS
        long frameProcessTime = System.currentTimeMillis() - frameStartTime;
        long sleepTime = Math.max(16 - frameProcessTime, 0); // 16 ms for 60 FPS
        Thread.sleep(sleepTime);
      }
      grabber.stop();
    } catch (Exception e) {
      LOGGER.severe("Error processing video file: " + e.getMessage());
    }
  }

  private float[] frameToVertexData(Frame frame) {

    BufferedImage originalImage = this.converter.getBufferedImage(frame);
    BufferedImage resizedImage = resizeImage(originalImage, this.width, this.height);

    // Use the specified width and height for the vertices array
    float[] vertices = new float[width * height * 6];
    int index = 0;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        float normX = (x / (float) width) * 2 - 1;
        float normY = ((height - y) / (float) height) * 2 - 1; // Inverte a coordenada Y

        int color = resizedImage.getRGB(x, y);
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        vertices[index++] = normX;
        vertices[index++] = normY;
        vertices[index++] = r;
        vertices[index++] = g;
        vertices[index++] = b;
        vertices[index++] = a;
      }
    }
    return vertices;
  }

  private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth,
      int targetHeight) {

    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = resizedImage.createGraphics();
    g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    g2d.dispose();
    return resizedImage;
  }

}
