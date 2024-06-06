package br.faustech.memory;

import br.faustech.comum.Component;
import br.faustech.comum.ComponentType;
import lombok.Getter;
import lombok.Setter;

//instance the memory class

@Getter
public class Memory extends Component {
    //Set memory array
    private byte[] memory;

    //set a flag to verify if memory is writable
    @Setter
    private boolean writable;


    //instance memory function
    //At this function, it has a memorySize variable that will define memory length according to necessity
    //Also, sets writable as true
    public Memory(final int memorySize) {

        byte[] address = new byte[10];
        for (int i = 0; i < address.length; i++) {
            address[i] = (byte) (Math.random() * 256);
        }

        super(address, ComponentType.MEMORY);

        this.memory = new byte[memorySize];
        this.writable = true;
    }

    //This function is set to write in the memory array. It has 2 variables, one is value, that will be allocated to the array
    //Variable beginDataPosition exists to set the start position of the array, and then, verify if the memory location will not be prior to that position
    //when is about to write
    //At the same time, there's a verification, with some exceptions being set.
    public void write(final byte[] value, final int beginDataPosition) throws MemoryException {
        if (this.writable) {
            for (int i = 0; i < value.length; i++) {
                if (beginDataPosition + i < this.memory.length) {
                    this.memory[beginDataPosition + i] = value[i];
                } else {
                    throw new MemoryException(String.format("Memory overflow at position %d", beginDataPosition + i));
                }
            }
        } else {
            throw new MemoryException("Memory is not writable");
        }
    }

    //Function Read is defined with two variables, one being well known, and the other is the end of array.
    //This one is set to read memory array and verify what's in a specific address.
    //Some exceptions were made to verify if the range is existent or if the position is valid.
    //Other than that, while having a memory read writable flag is set to false.
    public byte[] read(final int beginDataPosition, final int endDataPosition) {
        if (beginDataPosition < 0 || endDataPosition > this.memory.length) {
            throw new IllegalArgumentException("Invalid range specified");
        }
        if (endDataPosition <= beginDataPosition) {
            throw new IllegalArgumentException("End position must be greater than begin position");
        }

        byte[] value = new byte[endDataPosition - beginDataPosition];
        for (int i = beginDataPosition, j = 0; i < endDataPosition; i++, j++) {
            value[j] = this.memory[i];
        }
        return value;
    }

}
