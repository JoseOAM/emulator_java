package br.faustech.gpu;

import br.faustech.comum.Component;
import br.faustech.comum.ComponentType;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class FrameBuffer extends Component {

  private byte[] frontBuffer;

  private byte[] backBuffer;

  public FrameBuffer(final int bufferSize) {

    super(new byte[10], ComponentType.VIDEO_BUFFER);
    this.frontBuffer = new byte[bufferSize * 4]; // 4 bytes per float
    this.backBuffer = new byte[bufferSize * 4];
  }

  public void swap() {

    byte[] temp = frontBuffer;
    frontBuffer = backBuffer;
    backBuffer = temp;
  }

  public void writeToBackBuffer(byte[] data) {

    System.arraycopy(data, 0, backBuffer, 0, data.length);
  }

  public byte[] readFromFrontBuffer() {

    return frontBuffer.clone();
  }

  public float[] readFromFrontBufferAsFloats() {

    ByteBuffer byteBuffer = ByteBuffer.wrap(frontBuffer);
    byteBuffer.order(ByteOrder.nativeOrder()); // Ensure correct byte order
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    float[] floatArray = new float[floatBuffer.capacity()];
    floatBuffer.get(floatArray);
    return floatArray;
  }

  public void writeToBackBufferFromFloats(float[] data) {

    ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    floatBuffer.put(data);
    backBuffer = byteBuffer.array();
  }

}
