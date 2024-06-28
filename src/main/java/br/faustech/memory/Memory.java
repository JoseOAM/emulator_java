package br.faustech.memory;

import br.faustech.comum.Component;
import java.nio.ByteBuffer;
import lombok.Getter;

public class Memory extends Component {

  private final byte[] memory;

  private final boolean writable;

  @Getter private final int memorySize;

  public Memory(final int[] addresses, final int memorySize) {

    super(addresses);

    this.memory = new byte[memorySize];
    this.memorySize = memorySize;
    this.writable = true;
  }

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

  public void writeFromInt(final int beginDataPosition, final int[] value) throws MemoryException {

    if (!this.writable) {
      throw new MemoryException("Memory is not writable");
    }

    int length = value.length * 4;
    byte[] bytes = new byte[length];

    ByteBuffer byteBuffer = ByteBuffer.allocate(length);
    for (final int j : value) {
      byteBuffer.putInt(j);
    }
    byteBuffer.flip();

    if (beginDataPosition + length <= this.memory.length) {
      byteBuffer.get(bytes);
      System.arraycopy(bytes, 0, this.memory, (beginDataPosition), length);
    } else {
      throw new MemoryException(
          String.format("Memory overflow at position %d", beginDataPosition + length - 1));
    }
  }

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
    int[] intArray = new int[length / 4];
    for (int i = 0; i < intArray.length; i++) {
      intArray[i] = byteBuffer.getInt();
    }

    return intArray;
  }

}
