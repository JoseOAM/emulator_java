package br.faustech.gpu;

import br.faustech.comum.Component;
import br.faustech.comum.ComponentType;
import java.util.Arrays;

public class FrameBuffer extends Component {

  private float[] frontBuffer;

  private float[] backBuffer;

  public FrameBuffer(final int bufferSize) {

    super(new byte[10], ComponentType.VIDEO_BUFFER);

    this.frontBuffer = new float[bufferSize];
    this.backBuffer = new float[bufferSize];
  }

  public synchronized void swap() {

    float[] temp = frontBuffer;
    frontBuffer = backBuffer;
    backBuffer = temp;
  }

  public synchronized void writeToBackBuffer(float[] data) {

    System.arraycopy(data, 0, backBuffer, 0, data.length);

  }

  public synchronized float[] readFromFrontBuffer() {

    return frontBuffer.clone();

  }

}
