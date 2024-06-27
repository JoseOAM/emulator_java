package br.faustech.gpu;

import br.faustech.comum.Component;
import br.faustech.memory.MemoryException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import lombok.Getter;

public class FrameBuffer extends Component {

  private byte[] frontBuffer;

  private byte[] backBuffer;

  @Getter private final int bufferSize;

  public FrameBuffer(final int[] addresses, final int bufferSize) {

    super(addresses);
    this.frontBuffer = new byte[bufferSize]; // 4 bytes per float
    this.backBuffer = new byte[bufferSize]; // 4 bytes per float
    this.bufferSize = bufferSize;
  }

  public void swap() {

    byte[] temp = frontBuffer;
    frontBuffer = backBuffer;
    backBuffer = temp;
  }

  public void writeToBackBuffer(final int beginDataPosition, final byte[] data)
      throws MemoryException {

    if (beginDataPosition < 0 || beginDataPosition + data.length > backBuffer.length) {
      throw new MemoryException("Invalid data positions or data length.");
    }

    System.arraycopy(data, 0, backBuffer, beginDataPosition, data.length);
    this.swap();
  }

  public void writeToBackBufferFromFloats(final int beginDataPosition, final float[] data)
      throws MemoryException {

    if (beginDataPosition < 0 || beginDataPosition + data.length > backBuffer.length / 4) {
      throw new MemoryException("Invalid data positions or data length.");
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    floatBuffer.put(data);

    byteBuffer.rewind();
    byteBuffer.get(backBuffer, beginDataPosition * 4, byteBuffer.remaining());

    this.swap();
  }

  public void writeToBackBufferFromInts(final int beginDataPosition, final int[] data)
      throws MemoryException {

    if (beginDataPosition < 0 || beginDataPosition + data.length > backBuffer.length / 4) {
      throw new MemoryException("Invalid data positions or data length.");
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    IntBuffer intBuffer = byteBuffer.asIntBuffer();
    intBuffer.put(data);

    byteBuffer.rewind();
    byteBuffer.get(backBuffer, beginDataPosition * 4, byteBuffer.remaining());

    this.swap();
  }

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
