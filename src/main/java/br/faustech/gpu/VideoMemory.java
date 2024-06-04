package br.faustech.gpu;

public class VideoMemory {

  private byte[] frontBuffer = new byte[1920 * 1080 * 4];

  private byte[] backBuffer = new byte[1920 * 1080 * 4];

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
