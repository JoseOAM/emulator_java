package br.faustech.gpu;

import br.faustech.comum.Component;
import br.faustech.comum.ComponentType;

public class VideoBuffer extends Component {

  private byte[] frontBuffer;

  private byte[] backBuffer;

  public VideoBuffer(final int frontBufferSize, final int backBufferSize) {

    super(new byte[10], ComponentType.VIDEO_BUFFER);

    this.frontBuffer = new byte[frontBufferSize];
    this.backBuffer = new byte[backBufferSize];
  }

  public synchronized void swap() {

    byte[] temp = frontBuffer;
    frontBuffer = backBuffer;
    backBuffer = temp;
  }

  public synchronized void writeToBackBuffer(byte[] data) {

    System.arraycopy(data, 0, backBuffer, 0, data.length);

  }

  public synchronized byte[] readFromFrontBuffer() {

    return frontBuffer.clone();

  }

}
