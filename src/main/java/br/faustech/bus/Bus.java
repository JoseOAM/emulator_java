package br.faustech.bus;

import static br.faustech.comum.ComponentType.FRAME_BUFFER;
import static br.faustech.comum.ComponentType.MEMORY;

import br.faustech.comum.ComponentType;
import br.faustech.gpu.FrameBuffer;
import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import lombok.extern.java.Log;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Represents a bus system that facilitates communication between different components like memory
 * and frame buffers.
 */
@Log // Lombok annotation to enable logging
public class Bus {

  private final FrameBuffer frameBuffer; // The frame buffer component

  private final Memory memory; // The memory component

  /**
   * Constructs a Bus with specified frame buffer and memory components.
   *
   * @param frameBuffer The frame buffer to manage.
   * @param memory      The memory to manage.
   */
  public Bus(final FrameBuffer frameBuffer, final Memory memory) {

    this.frameBuffer = frameBuffer;
    this.memory = memory;
  }

  /**
   * Writes integer data to the appropriate component based on the address.
   *
   * @param address The starting address for the data.
   * @param value   The integer array to write.
   */
  public void write(final int address, final int[] value) {

    ComponentType componentType = witchComponentType(address); // Determine which component to use

    try {
      switch (componentType) {
        case FRAME_BUFFER:
          // Write to frame buffer if the address corresponds to it
          frameBuffer.writeToBackBufferFromInts(address - memory.getMemorySize(), value);
          break;
        case MEMORY:
          // Write to memory if the address corresponds to it
          memory.writeFromInt(address, value);
          break;
        default:
          throw new IllegalArgumentException("Invalid component type");
      }
    } catch (Exception e) {
      log.severe(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Determines which component type corresponds to a given address.
   *
   * @param address The address to check against known component addresses.
   * @return The component type associated with the address.
   */
  public ComponentType witchComponentType(final int address) {

    if (ArrayUtils.contains(frameBuffer.getAddresses(), address)) {
      return FRAME_BUFFER;
    } else if (ArrayUtils.contains(memory.getAddresses(), address)) {
      return MEMORY;
    } else {
      throw new IllegalArgumentException("Invalid component type");
    }
  }

  /**
   * Reads integer data from the appropriate component based on the address.
   *
   * @param address         The starting address for reading data.
   * @param endDataPosition The end position for reading data.
   * @return The integer array read from the component.
   */
  public int[] read(final int address, final int endDataPosition) {

    ComponentType componentType = witchComponentType(
        address); // Determine which component to read from

    try {
      return switch (componentType) {
        case FRAME_BUFFER -> frameBuffer.readFromFrontBufferAsInts(
            address - memory.getMemorySize(), endDataPosition - memory.getMemorySize());
        case MEMORY -> memory.readAsInt(address, endDataPosition);
      };
    } catch (MemoryException e) {
      log.severe(e.getMessage());
      throw new RuntimeException(e.getMessage());
    }
  }

}
