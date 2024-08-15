package br.faustech.memory;

import java.nio.ByteBuffer;
import lombok.Getter;

/**
 * Represents a simple memory model for storing and retrieving data.
 */
public class Memory {

  @Getter private static int memorySize; // Total size of the memory array

  private final byte[] memory; // Memory array to store data

  private final boolean writable; // Flag to indicate if the memory is writable

  /**
   * Constructs a memory component with specified size.
   *
   * @param memorySize The size of the memory to allocate.
   */
  public Memory(final int memorySize) {

    this.memory = new byte[memorySize]; // Allocate memory
    Memory.memorySize = memorySize;
    this.writable = true; // Set the memory as writable
  }

  /**
   * Writes data to memory at a specified position.
   *
   * @param beginDataPosition The starting position in memory to write data.
   * @param value             The data to be written as byte array.
   * @throws MemoryException If the memory is not writable or overflow occurs.
   */
  public void write(final int beginDataPosition, final byte[] value) throws MemoryException {

    if (!this.writable) {
      throw new MemoryException("Memory is not writable");
    }

    for (int i = 0; i < value.length; i++) {
      if (beginDataPosition + i < this.memory.length) {
        this.memory[beginDataPosition + i] = value[i];
      } else {
        throw new MemoryException(
            String.format("Memory overflow at position %d", beginDataPosition + i));
      }
    }
  }

  /**
   * Writes integer data to memory, converting to bytes first.
   *
   * @param beginDataPosition The start position in the memory.
   * @param value             The integer array to write, converted to bytes.
   * @throws MemoryException If the memory is not writable or if overflow occurs.
   */
  public void writeFromInt(final int beginDataPosition, final int[] value) throws MemoryException {

    if (!this.writable) {
      throw new MemoryException("Memory is not writable");
    }

    int length = value.length * 4; // Calculate byte length from int length
    byte[] bytes = new byte[length];

    ByteBuffer byteBuffer = ByteBuffer.allocate(length);
    for (final int j : value) {
      byteBuffer.putInt(j);
    }
    byteBuffer.flip(); // Prepare buffer for reading

    if (beginDataPosition + length <= this.memory.length) {
      byteBuffer.get(bytes);
      System.arraycopy(bytes, 0, this.memory, beginDataPosition, length);
    } else {
      throw new MemoryException(
          String.format("Memory overflow at position %d", beginDataPosition + length - 1));
    }
  }

  /**
   * Reads a range of bytes from memory.
   *
   * @param beginDataPosition The start position in memory to read.
   * @param endDataPosition   The end position in memory to read.
   * @return Array of bytes read from memory.
   * @throws MemoryException If the specified range is invalid.
   */
  public byte[] read(final int beginDataPosition, final int endDataPosition)
      throws MemoryException {

    if (beginDataPosition < 0 || endDataPosition > this.memory.length) {
      throw new MemoryException("Invalid range specified");
    }
    if (endDataPosition <= beginDataPosition) {
      throw new MemoryException("End position must be greater than begin position");
    }

    byte[] value = new byte[endDataPosition - beginDataPosition];
    for (int i = beginDataPosition, j = 0; i < endDataPosition; i++, j++) {
      value[j] = this.memory[i];
    }
    return value;
  }

  /**
   * Reads a range of bytes from memory and converts them to integers.
   *
   * @param beginDataPosition The start position in memory to read.
   * @param endDataPosition   The end position in memory to read.
   * @return Array of integers read from memory.
   * @throws MemoryException If the specified range is invalid.
   */
  public int[] readAsInt(final int beginDataPosition, final int endDataPosition)
      throws MemoryException {

    if (beginDataPosition < 0 || endDataPosition > this.memory.length) {
      throw new MemoryException("Invalid range specified");
    }
    if (endDataPosition <= beginDataPosition) {
      throw new MemoryException("End position must be greater than begin position");
    }

    int length = endDataPosition - beginDataPosition;
    byte[] value = new byte[length];

    System.arraycopy(this.memory, beginDataPosition, value, 0, length);

    ByteBuffer byteBuffer = ByteBuffer.wrap(value);
    int[] intArray = new int[length / 4]; // Calculate number of integers
    for (int i = 0; i < intArray.length; i++) {
      intArray[i] = byteBuffer.getInt(); // Convert bytes to integer
    }

    return intArray;
  }

}
