package br.faustech.memory;

import br.faustech.comum.Component;
import br.faustech.comum.ComponentType;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Memory extends Component {

  private final byte[] memory;

  @Setter private boolean writable;

  public Memory(final int memorySize) {

    byte[] address = new byte[10];
    for (int i = 0; i < address.length; i++) {
      address[i] = (byte) (Math.random() * 256);
    }

    super(address, ComponentType.MEMORY);

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
