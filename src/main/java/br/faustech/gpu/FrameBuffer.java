package br.faustech.gpu;

import br.faustech.comum.Component;
import br.faustech.memory.MemoryException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import lombok.Getter;

/**
 * A class representing a framebuffer that manages two buffers for double buffering.
 */
public class FrameBuffer extends Component {

  @Getter private static int bufferSize; // Size of each buffer

  private byte[] frontBuffer; // Buffer currently displayed

  private byte[] backBuffer; // Buffer to write new data to

  /**
   * Constructs a FrameBuffer with specified memory addresses and buffer size.
   *
   * @param addresses  The memory addresses.
   * @param bufferSize The size of each buffer.
   */
  public FrameBuffer(final int[] addresses, final int bufferSize) {

    super(addresses);
    this.frontBuffer = new byte[bufferSize]; // Initialize front buffer
    this.backBuffer = new byte[bufferSize]; // Initialize back buffer
    FrameBuffer.bufferSize = bufferSize;
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
   * Writes data to the back buffer starting from a specified position.
   *
   * @param beginDataPosition The starting position in the back buffer.
   * @param data              The byte data to be written.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writeToBackBuffer(final int beginDataPosition, final byte[] data)
      throws MemoryException {

    if (beginDataPosition < 0 || beginDataPosition + data.length > backBuffer.length) {
      throw new MemoryException("Invalid data positions or data length.");
    }

    System.arraycopy(data, 0, backBuffer, beginDataPosition, data.length);
  }

  /**
   * Writes float data to the back buffer, converting them to bytes before storing.
   *
   * @param beginDataPosition The starting index where data is to be written.
   * @param data              The float data to be converted and written.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writeToBackBufferFromFloats(final int beginDataPosition, final float[] data)
      throws MemoryException {

    if (beginDataPosition < 0 || beginDataPosition + data.length > backBuffer.length / 4) {
      throw new MemoryException("Invalid data positions or data length.");
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.nativeOrder());
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    floatBuffer.put(data);

    byteBuffer.rewind();
    byteBuffer.get(backBuffer, beginDataPosition * 4, byteBuffer.remaining());
  }

  /**
   * Writes integer data to the back buffer, converting them to bytes before storing.
   *
   * @param beginDataPosition The starting index where data is to be written.
   * @param data              The integer data to be converted and written.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writeToBackBufferFromInts(final int beginDataPosition, final int[] data)
      throws MemoryException {

    if (beginDataPosition < 0 || beginDataPosition + data.length > backBuffer.length / 4) {
      throw new MemoryException("Invalid data positions or data length.");
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.nativeOrder());
    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    intBuffer.put(data);

    byteBuffer.rewind();
    byteBuffer.get(backBuffer, beginDataPosition * 4, byteBuffer.remaining());
  }

  /**
   * Reads a segment of the front buffer as byte data.
   *
   * @param beginDataPosition The starting index in the buffer.
   * @param endDataPosition   The ending index in the buffer.
   * @return An array of bytes read from the buffer.
   */
  public byte[] readFromFrontBuffer(final int beginDataPosition, final int endDataPosition) {

    if (beginDataPosition < 0 || endDataPosition > frontBuffer.length
        || beginDataPosition >= endDataPosition) {
      throw new IllegalArgumentException("Invalid data positions.");
    }

    int length = endDataPosition - beginDataPosition;
    byte[] result = new byte[length];

    System.arraycopy(frontBuffer, beginDataPosition, result, 0, length);

    return result;
  }

  /**
   * Reads a segment of the front buffer as float data.
   *
   * @param beginDataPosition The starting index in the buffer.
   * @param endDataPosition   The ending index in the buffer.
   * @return An array of floats read from the buffer.
   * @throws MemoryException If invalid data positions are used.
   */
  public float[] readFromFrontBufferAsFloats(final int beginDataPosition, final int endDataPosition)
      throws MemoryException {

    if (beginDataPosition < 0 || endDataPosition > frontBuffer.length / 4
        || beginDataPosition >= endDataPosition) {
      throw new MemoryException("Invalid data positions.");
    }

    int length = endDataPosition - beginDataPosition;

    ByteBuffer byteBuffer = ByteBuffer.wrap(frontBuffer);
    byteBuffer.order(ByteOrder.nativeOrder());
    byteBuffer.position(beginDataPosition * 4);

    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    float[] floatArray = new float[length];
    floatBuffer.get(floatArray, 0, length);

    return floatArray;
  }

  /**
   * Reads a segment of the front buffer as integer data.
   *
   * @param beginDataPosition The starting index in the buffer.
   * @param endDataPosition   The ending index in the buffer.
   * @return An array of integers read from the buffer.
   * @throws MemoryException If invalid data positions are used.
   */
  public int[] readFromFrontBufferAsInts(final int beginDataPosition, final int endDataPosition)
      throws MemoryException {

    if (beginDataPosition < 0 || endDataPosition > frontBuffer.length / 4
        || beginDataPosition >= endDataPosition) {
      throw new MemoryException("Invalid data positions.");
    }

    int length = endDataPosition - beginDataPosition;

    ByteBuffer byteBuffer = ByteBuffer.wrap(frontBuffer);
    byteBuffer.order(ByteOrder.nativeOrder());
    byteBuffer.position(beginDataPosition * 4);

    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    int[] intArray = new int[length];
    intBuffer.get(intArray, 0, length);

    return intArray;
  }

}
