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

  private final byte[] pixelBuffer; // Buffer to store pixel data

  /**
   * Constructs a FrameBuffer with specified memory addresses and buffer size.
   *
   * @param addresses  The memory addresses.
   * @param bufferSize The size of each buffer.
   */
  public FrameBuffer(final int[] addresses, final int bufferSize) {

    super(addresses);
    this.frontBuffer = new byte[bufferSize * 8 * 4];  // Initialize front buffer
    this.backBuffer = new byte[bufferSize * 8 * 4];   // Initialize back buffer
    this.pixelBuffer = new byte[bufferSize];          // Initialize pixel buffer
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
  public void writeToBackBufferFromBytes(final int beginDataPosition, final byte[] data)
      throws MemoryException {

    if (beginDataPosition < 0 || beginDataPosition + data.length > backBuffer.length) {
      throw new MemoryException("Invalid data positions or data length.");
    }
    System.arraycopy(data, 0, backBuffer, beginDataPosition, data.length);
  }

  /**
   * Writes pixel data to the back buffer starting from a specified position.
   *
   * @param beginDataPosition The starting position in the back buffer.
   * @param data              The pixel data as an integer.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writePixel(final int beginDataPosition, final int data) throws MemoryException {

    this.writeToPixelBufferFromInts(beginDataPosition, new int[]{data});

    // Calculate normalized coordinates for texture mapping
    int x = beginDataPosition / GPU.getWidth();
    int y = beginDataPosition / GPU.getWidth();

    float normX = (x / (float) GPU.getWidth()) * 2 - 1;
    float normY = ((GPU.getHeight() - y) / (float) GPU.getHeight()) * 2 - 1;

    this.writeToBackBufferFromFloats(8 * (y * GPU.getWidth() + x),
        new float[]{normX, normY, ((data >> 16) & 0xFF) / 255.0f,   // r
            ((data >> 8) & 0xFF) / 255.0f,                          // g
            (data & 0xFF) / 255.0f,                                 // b
            ((data >> 24) & 0xFF) / 255.0f,                         // a
            x / (float) GPU.getWidth(),                             // u
            y / (float) GPU.getHeight()                             // v
        });
  }

  /**
   * Writes integer data to the back buffer, converting them to bytes before storing.
   *
   * @param beginDataPosition The starting index where data is to be written.
   * @param data              The integer data to be converted and written.
   * @throws MemoryException If the write operation exceeds buffer limits.
   */
  public void writeToPixelBufferFromInts(final int beginDataPosition, final int[] data)
      throws MemoryException {

    if (beginDataPosition < 0 || beginDataPosition + data.length > pixelBuffer.length / 4) {
      throw new MemoryException("Invalid data positions or data length.");
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.nativeOrder());
    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    intBuffer.put(data);

    byteBuffer.rewind();
    byteBuffer.get(pixelBuffer, beginDataPosition * 4, byteBuffer.remaining());
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
   * Reads a segment of the front buffer as byte data.
   *
   * @param beginDataPosition The starting index in the buffer.
   * @param endDataPosition   The ending index in the buffer.
   * @return An array of bytes read from the buffer.
   */
  public byte[] readFromFrontBufferAsBytes(final int beginDataPosition, final int endDataPosition) {

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

    int length = endDataPosition - beginDataPosition;

    final ByteBuffer byteBuffer = getByteBufferFromBuffer(frontBuffer, beginDataPosition,
        endDataPosition);

    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    float[] floatArray = new float[length];
    floatBuffer.get(floatArray, 0, length);

    return floatArray;
  }

  /**
   * Retrieves a ByteBuffer from the given buffer with specified start and end positions.
   *
   * @param buffer            The byte array buffer.
   * @param beginDataPosition The starting index in the buffer.
   * @param endDataPosition   The ending index in the buffer.
   * @return A ByteBuffer positioned at the specified data range.
   * @throws MemoryException If invalid data positions are used.
   */
  private ByteBuffer getByteBufferFromBuffer(final byte[] buffer, final int beginDataPosition,
      final int endDataPosition) throws MemoryException {

    if (beginDataPosition < 0 || endDataPosition > buffer.length / 4
        || beginDataPosition >= endDataPosition) {
      throw new MemoryException("Invalid data positions.");
    }

    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
    byteBuffer.order(ByteOrder.nativeOrder());
    byteBuffer.position(beginDataPosition * 4);
    return byteBuffer;
  }

  /**
   * Reads a segment of the front buffer as integer data.
   *
   * @param beginDataPosition The starting index in the buffer.
   * @param endDataPosition   The ending index in the buffer.
   * @return An array of integers read from the buffer.
   * @throws MemoryException If invalid data positions are used.
   */
  public int[] readFromPixelBufferAsInts(final int beginDataPosition, final int endDataPosition)
      throws MemoryException {

    int length = endDataPosition - beginDataPosition;

    final ByteBuffer byteBuffer = getByteBufferFromBuffer(pixelBuffer, beginDataPosition,
        endDataPosition);

    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    int[] intArray = new int[length];
    intBuffer.get(intArray, 0, length);

    return intArray;
  }

}
