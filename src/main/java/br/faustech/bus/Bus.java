package br.faustech.bus;

import br.faustech.comum.Component;
import br.faustech.comum.ComponentType;
import br.faustech.gpu.FrameBuffer;
import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import lombok.extern.java.Log;

@Log
public class Bus extends Component {

  private final FrameBuffer frameBuffer;

  private final Memory memory;

  public Bus(final FrameBuffer frameBuffer, final Memory memory) {

    super(ComponentType.BUS.toString().getBytes(), ComponentType.BUS);
    this.frameBuffer = frameBuffer;
    this.memory = memory;
  }

  public void write(ComponentType componentType, final byte[] value) {

    try {
      switch (componentType) {
        case FRAME_BUFFER:
          frameBuffer.writeToBackBuffer(value);
          break;
        case MEMORY:
          int beginDataPosition = value[0];
          byte[] valueArray = new byte[value.length - 1];
          System.arraycopy(value, 1, valueArray, 0, valueArray.length);
          memory.write(valueArray, beginDataPosition);
          break;
        default:
          throw new IllegalArgumentException("Invalid component type");
      }
    } catch (MemoryException e) {
      log.severe(e.getMessage());
    }
  }

  public byte[] read(ComponentType componentType, final byte[] value) {

    try {

      switch (componentType) {
        case FRAME_BUFFER:
          return frameBuffer.readFromFrontBuffer();
        case MEMORY:
          int beginDataPosition = value[0];
          int endDataPosition = value[1];
          return memory.read(beginDataPosition, endDataPosition);
        default:
          throw new IllegalArgumentException("Invalid component type");
      }
    } catch (MemoryException e) {
      log.severe(e.getMessage());
      return new byte[0];
    }
  }

}
