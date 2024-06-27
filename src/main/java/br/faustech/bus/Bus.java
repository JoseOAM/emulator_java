package br.faustech.bus;

import static br.faustech.comum.ComponentType.FRAME_BUFFER;
import static br.faustech.comum.ComponentType.MEMORY;

import br.faustech.comum.ComponentType;
import br.faustech.gpu.FrameBuffer;
import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import lombok.extern.java.Log;
import org.apache.commons.lang3.ArrayUtils;

@Log
public class Bus {

  private final FrameBuffer frameBuffer;

  private final Memory memory;

  public Bus(final FrameBuffer frameBuffer, final Memory memory) {

    this.frameBuffer = frameBuffer;
    this.memory = memory;
  }

  public void write(int address, final int[] value) {

    ComponentType componentType = witchComponentType(address);

    try {
      switch (componentType) {
        case FRAME_BUFFER:
          frameBuffer.writeToBackBufferFromInts(address, value);
          break;
        case MEMORY:
          memory.writeFromInt(address, value);
          break;
        default:
          throw new IllegalArgumentException("Invalid component type");
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public int[] read(final int address, final int endDataPosition) {

    ComponentType componentType = witchComponentType(address);
    try {
      return switch (componentType) {
        case FRAME_BUFFER -> frameBuffer.readFromFrontBufferAsInts(address, endDataPosition);
        case MEMORY -> memory.readAsInt(address, endDataPosition);
      };
    } catch (MemoryException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public ComponentType witchComponentType(int address) {

    if (ArrayUtils.contains(frameBuffer.getAddresses(), address)) {
      return FRAME_BUFFER;
    } else if (ArrayUtils.contains(memory.getAddresses(), address)) {
      return MEMORY;
    } else {
      throw new IllegalArgumentException("Invalid component type");
    }
  }

}
