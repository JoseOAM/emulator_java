package br.faustech.gpu;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageReader {

  public static byte[] readImage(String filePath, int width, int height) {

    try {
      BufferedImage bufferedImage = ImageIO.read(new File(filePath));
      return getImageBytes(bufferedImage, width, height);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static byte[] getImageBytes(final BufferedImage image, final int width,
      final int height) {

    BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    resizedImage.getGraphics().drawImage(image, 0, 0, width, height, null);

    byte[] pixelData = new byte[width * height * 4];
    int[] pixels = new int[width * height];
    resizedImage.getRGB(0, 0, width, height, pixels, 0, width);

    for (int i = 0; i < pixels.length; i++) {
      int argb = pixels[i];
      pixelData[i * 4] = (byte) ((argb >> 24) & 0xFF); // Alpha
      pixelData[i * 4 + 1] = (byte) ((argb >> 16) & 0xFF); // Red
      pixelData[i * 4 + 2] = (byte) ((argb >> 8) & 0xFF); // Green
      pixelData[i * 4 + 3] = (byte) (argb & 0xFF); // Blue
    }

    return pixelData;
  }

  public static byte[] readImage(BufferedImage image, int width, int height) {

    return getImageBytes(image, width, height);
  }

}
