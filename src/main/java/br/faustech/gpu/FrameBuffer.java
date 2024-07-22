package br.faustech.gpu;

import br.faustech.comum.Component;
import br.faustech.memory.MemoryException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 * A class representing a framebuffer that manages two buffers for double buffering.
 */
@Log
public class FrameBuffer extends Component {

  @Getter private static int bufferSize; // Size of each buffer

  private final byte[] pixelBuffer; // Buffer to store pixel data

  private byte[] frontBuffer; // Buffer currently displayed

  private byte[] backBuffer; // Buffer to write new data to

  /**
   * Constructs a FrameBuffer with specified memory addresses and buffer size.
   *
   * @param addresses  The memory addresses.
   * @param bufferSize The size of each buffer.
   */
  public FrameBuffer(final int[] addresses, int bufferSize) {

    super(addresses);
    this.pixelBuffer = new byte[bufferSize];  // Initialize pixel buffer
    bufferSize *= 8;
    this.frontBuffer = new byte[bufferSize];  // Initialize front buffer
    this.backBuffer = new byte[bufferSize];   // Initialize back buffer
    FrameBuffer.bufferSize = bufferSize;
  }

  /**
   * Writes data to the back buffer starting from a specified position.
   *
   * @param beginAddress The starting position in the back buffer.
   * @param data         The byte data to be written.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writeToBackBufferFromBytes(final int beginAddress, final byte[] data)
      throws MemoryException {

    int endAddress = beginAddress + data.length;
    if (beginAddress < 0 || endAddress > backBuffer.length) {
      throw new MemoryException(
          "Invalid data positions or data length. (beginAddress: " + beginAddress + ", endAddress: "
              + endAddress + ")");
    }
    System.arraycopy(data, 0, backBuffer, beginAddress, data.length);
  }

  /**
   * Writes pixel data to the back buffer starting from a specified position.
   *
   * @param beginAddress The starting position in the back buffer.
   * @param data         The pixel data as an array of integers.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writePixel(int beginAddress, final int[] data) throws MemoryException {

    try {
      this.writeToPixelBufferFromInts(beginAddress, data);

      int width = GPU.getWidth();
      int height = GPU.getHeight();

      for (int i = 0; i < data.length; i++) {
        int color = data[i];

        // Calculate normalized coordinates for texture mapping
        int x = (beginAddress + i) % width;
        int y = (beginAddress + i) / width;

        float normX = (x / (float) width) * 2 - 1;
        float normY = ((height - y) / (float) height) * 2 - 1;

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = 1;
        float u = x / (float) width;
        float v = y / (float) height;

        final float[] pixel = new float[]{normX, normY, r, g, b, a, u, v};

        int address = 8 * (y * width + x);

        this.writeToBackBufferFromFloats(address, pixel);
      }
    } catch (Exception e) {
      log.severe(e.getMessage());
    }
  }

  /**
   * Writes integer data to the back buffer, converting them to bytes before storing.
   *
   * @param beginAddress The starting index where data is to be written.
   * @param data         The integer data to be converted and written.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writeToPixelBufferFromInts(final int beginAddress, final int[] data)
      throws MemoryException {

    int endAddress = beginAddress + data.length;
    if (beginAddress < 0 || endAddress > pixelBuffer.length / 4) {
      throw new MemoryException(
          "Invalid data positions or data length. (beginAddress: " + beginAddress + ", endAddress: "
              + endAddress + ")");
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.nativeOrder());
    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    intBuffer.put(data);

    byteBuffer.rewind();
    byteBuffer.get(pixelBuffer, beginAddress * 4, byteBuffer.remaining());
  }

  /**
   * Writes float data to the back buffer, converting them to bytes before storing.
   *
   * @param beginAddress The starting index where data is to be written.
   * @param data         The float data to be converted and written.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writeToBackBufferFromFloats(final int beginAddress, final float[] data)
      throws MemoryException {

    int endAddress = beginAddress + data.length;
    if (beginAddress < 0 || endAddress > backBuffer.length / 4) {
      throw new MemoryException(
          "Invalid data positions or data length. (beginAddress: " + beginAddress + ", endAddress: "
              + endAddress + ")");
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.nativeOrder());
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    floatBuffer.put(data);

    byteBuffer.rewind();
    byteBuffer.get(backBuffer, beginAddress * 4, byteBuffer.remaining());
  }

  /**
   * Swaps the front and back buffers, promoting the back to front for display.
   */
  public void swap() {

    byte[] temp = frontBuffer;
    frontBuffer = backBuffer;
    backBuffer = temp;
  }

  /**
   * Reads a segment of the front buffer as byte data.
   *
   * @param beginAddress The starting index in the buffer.
   * @param endAddress   The ending index in the buffer.
   * @return An array of bytes read from the buffer.
   */
  public byte[] readFromFrontBufferAsBytes(final int beginAddress, final int endAddress) {

    if (beginAddress < 0 || endAddress > frontBuffer.length || beginAddress >= endAddress) {
      throw new IllegalArgumentException(

          "Invalid data positions or data length. (beginAddress: " + beginAddress + ", endAddress: "
              + endAddress + ")");
    }

    int length = endAddress - beginAddress;
    byte[] result = new byte[length];

    System.arraycopy(frontBuffer, beginAddress, result, 0, length);

    return result;
  }

  /**
   * Reads a segment of the front buffer as float data.
   *
   * @param beginAddress The starting index in the buffer.
   * @param endAddress   The ending index in the buffer.
   * @return An array of floats read from the buffer.
   * @throws MemoryException If invalid data positions are used.
   */
  public float[] readFromFrontBufferAsFloats(final int beginAddress, final int endAddress)
      throws MemoryException {

    int length = endAddress - beginAddress;

    final ByteBuffer byteBuffer = getByteBufferFromBuffer(frontBuffer, beginAddress, endAddress);

    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    float[] floatArray = new float[length];
    floatBuffer.get(floatArray, 0, length);

    return floatArray;
  }

  /**
   * Retrieves a ByteBuffer from the given buffer with specified start and end positions.
   *
   * @param buffer       The byte array buffer.
   * @param beginAddress The starting index in the buffer.
   * @param endAddress   The ending index in the buffer.
   * @return A ByteBuffer positioned at the specified data range.
   * @throws MemoryException If invalid data positions are used.
   */
  private ByteBuffer getByteBufferFromBuffer(final byte[] buffer, final int beginAddress,
      final int endAddress) throws MemoryException {

    if (beginAddress < 0 || endAddress > buffer.length / 4 || beginAddress >= endAddress) {
      throw new MemoryException(
          "Invalid data positions or data length. (beginAddress: " + beginAddress + ", endAddress: "
              + endAddress + ")");
    }

    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
    byteBuffer.order(ByteOrder.nativeOrder());
    byteBuffer.position(beginAddress * 4);
    return byteBuffer;
  }

  /**
   * Reads a segment of the front buffer as integer data.
   *
   * @param beginAddress The starting index in the buffer.
   * @param endAddress   The ending index in the buffer.
   * @return An array of integers read from the buffer.
   * @throws MemoryException If invalid data positions are used.
   */
  public int[] readFromPixelBufferAsInts(final int beginAddress, final int endAddress)
      throws MemoryException {

    int length = endAddress - beginAddress;

    final ByteBuffer byteBuffer = getByteBufferFromBuffer(pixelBuffer, beginAddress, endAddress);

    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    int[] intArray = new int[length];
    intBuffer.get(intArray, 0, length);

    return intArray;
  }

}
