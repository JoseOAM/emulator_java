package br.faustech.gpu;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

@Log
@RequiredArgsConstructor
public class VideoFrameToVertexArray extends Thread {

  private final String videoFilePath;

  private final Java2DFrameConverter converter = new Java2DFrameConverter();

  private final int width;

  private final int height;

  private final FrameBuffer frameBuffer;

  private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth,
      int targetHeight) {

    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = resizedImage.createGraphics();
    g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    g2d.dispose();
    return resizedImage;
  }

  @Override
  public void run() {

    this.processVideo();
  }

  private void processVideo() {

    try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFilePath)) {
      grabber.start();
      Frame frame;
      while ((frame = grabber.grabImage()) != null) {
        long time = System.currentTimeMillis();
        float[] vertexData = frameToVertexData(frame);

        // float to byte conversion
        byte[] vertexDataByte = new byte[vertexData.length * 4];
        for (int i = 0; i < vertexData.length; i++) {
          int intBits = Float.floatToIntBits(vertexData[i]);
          vertexDataByte[i * 4] = (byte) (intBits & 0xFF);
          vertexDataByte[i * 4 + 1] = (byte) ((intBits >> 8) & 0xFF);
          vertexDataByte[i * 4 + 2] = (byte) ((intBits >> 16) & 0xFF);
          vertexDataByte[i * 4 + 3] = (byte) ((intBits >> 24) & 0xFF);
        }

        frameBuffer.writeToBackBuffer(0, vertexDataByte);

        time = System.currentTimeMillis() - time;
        long sleepTime = Math.max(0, 1000 / 60 - time);
        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
          log.severe(String.format(e.getMessage()));
          return;
        }
      }
      grabber.stop();
      this.processVideo();
    } catch (Exception e) {
      throw new RuntimeException(String.format("Error processing video: %s", e.getMessage()));
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

}
