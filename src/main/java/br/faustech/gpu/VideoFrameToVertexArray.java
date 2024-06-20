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

  private final Java2DFrameConverter converter = new Java2DFrameConverter();

  public volatile int width;

  public volatile int height;

  public VideoFrameToVertexArray(FrameBuffer frameBuffer, String videoFilePath, int width,
      int height) {

    this.frameBuffer = frameBuffer;
    this.videoFilePath = videoFilePath;
    this.width = width;
    this.height = height;
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
        long time = System.currentTimeMillis();
        float[] vertexData = frameToVertexData(frame);
        synchronized (frameBuffer) {
          frameBuffer.writeToBackBufferFromFloats(vertexData);
          frameBuffer.swap();
        }
        time = System.currentTimeMillis() - time;
        long sleepTime = Math.max(0, 1000 / 60 - time);
        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
          LOGGER.severe(String.format("Error sleeping thread: %s", e.getMessage()));
        }
      }
      grabber.stop();
    } catch (Exception e) {
      LOGGER.severe(String.format("Error processing video: %s", e.getMessage()));
    }
  }

  private float[] frameToVertexData(Frame frame) {

    BufferedImage originalImage = converter.getBufferedImage(frame);
    BufferedImage resizedImage = resizeImage(originalImage, width, height);

    // Use the specified width and height for the vertices array
    float[] vertices = new float[width * height * 8];
    int index = 0;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        float normX = (x / (float) width) * 2 - 1;
        float normY = ((height - y) / (float) height) * 2 - 1; // Invert Y coordinate

        int color = resizedImage.getRGB(x, y);
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        // Set texture coordinates for the vertex (u, v)
        float u = x / (float) width;
        float v = y / (float) height;

        vertices[index++] = normX;
        vertices[index++] = normY;
        vertices[index++] = r;
        vertices[index++] = g;
        vertices[index++] = b;
        vertices[index++] = a;
        vertices[index++] = u;
        vertices[index++] = v;
      }
    }
    return vertices;
  }

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
