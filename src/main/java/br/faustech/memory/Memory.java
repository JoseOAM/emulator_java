package br.faustech.memory;

import br.faustech.comum.Component;
import br.faustech.comum.ComponentType;

public class Memory extends Component {

  private final byte[] memory;

  private final boolean writable;

  public Memory(final int memorySize) {

    super(ComponentType.MEMORY.toString().getBytes(), ComponentType.MEMORY);

    this.memory = new byte[memorySize];
    this.writable = true;
  }

  public void write(final byte[] value, final int beginDataPosition) throws MemoryException {

    if (this.writable) {
      for (int i = 0; i < value.length; i++) {
        if (beginDataPosition + i < this.memory.length) {
          this.memory[beginDataPosition + i] = value[i];
        } else {
          throw new MemoryException(
              String.format("Memory overflow at position %d", beginDataPosition + i));
        }
      }
    } else {
      throw new MemoryException("Memory is not writable");
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

}
