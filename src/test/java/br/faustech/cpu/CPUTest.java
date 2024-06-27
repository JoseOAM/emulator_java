package br.faustech.cpu;

import br.faustech.bus.Bus;
import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.Test;

public class CPUTest {

  public Memory memory = new Memory(4096);

  public Bus bus = new Bus(null, memory);


  private byte[] intToByteArray(int value) {

    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
    byteBuffer.putInt(value);
    return byteBuffer.array();
  }

  @Test
  public void cpuTest() {

    try {
      // Defining instructions in memory
      int[] instructions = {
              0b00010001110000000010000000000011,//load
              0b00010010000000001010000010000011,//load
              0b00000000000100000000000100110011,
              0b00010000000100000000000110110011,
              0b00000000000100000001001000110011,
              0b00000000000100000010001010110011,
              0b00000000000100000011001100110011,
              0b00000000000100000100001110110011,
              0b00000000000100000101010000110011,
              0b01000000000100000101010010110011,
              0b00000000000100000110010100110011,
              0b00000000000100000111010110110011,
              0b00000000001100001001011000110111,
              0b00000000000000000000011010010111,
              0b00000001000000000000011101101111,
              0b00000000000000000000011111100111,
              0b00000000011000101000100001100011,
              0b00000000000000000000000000000000,
              0b00000000000100000001100001100011,
              0b00000000000000000000000000000000,
              0b00000000001000000100100001100011,
              0b00000000000000000000000000000000,
              0b00000000000100000101100001100011,
              0b00000000000000000000000000000000,
              0b00000000010000001110100001100011,
              0b00000000000000000000000000000000,
              0b00000000011000101111100001100011,
              0b00000000000000000000000000000000,
              0b00000001000000001000100000010011,
              0b00000001000000001010100010010011,
              0b00000001000000001011100100010011,
              0b00000001000000001100100110010011,
              0b00000001000000001110101000010011,
              0b00000001000000001111101010010011,
              0b00000000000000001001101100010011,
              0b00000000000100001101101110010011,
              0b00000000001000001101110000010011,
              0b00000000000000000000000001110011,
              0b00000000000000000001110011110011,
              0b00000000000100001010110101110011,
              0b00000000001000010011110111110011,
              0b00000000001100011101111001110011,
              0b00000000010000100110111011110011,
              0b00000000010100101111111101110011,
              0b00000000000011111010000000100011,//store
              0b00000000000111111010000010100011,//store
              0b00000000001011111010000100100011,//store
              0b00000000001111111010000110100011,//store
              0b00000000010011111010001000100011,//store
              0b00000000010111111010001010100011,//store
              0b00000000011011111010001100100011,//store
              0b00000000011111111010001110100011,//store
              0b00000000100011111010010000100011,//store
              0b00000000100111111010010010100011,//store
              0b00000000101011111010010100100011,//store
              0b00000000101111111010010110100011,//store
              0b00000000110011111010011000100011,//store
              0b00000000110111111010011010100011,//store
              0b00000000111011111010011100100011,//store
              0b00000000111111111010011110100011,//store
              0b00000001000011111010100000100011,//store
              0b00000001000111111010100010100011,//store
              0b00000001001011111010100100100011,//store
              0b00000001001111111010100110100011,//store
              0b00000001010011111010101000100011,//store
              0b00000001010111111010101010100011,//store
              0b00000001011011111010101100100011,//store
              0b00000001011111111010101110100011,//store
              0b00000001100011111010110000100011,//store
              0b00000001100111111010110010100011,//store
              0b00000000000100000000000001110011,//ebreak
              0b00000000000000000000000000000110, // valor 6 para teste
              0b00000000000000000000000000000101,  // valor 5 para teste

      };

      int position = 0;
      // Iterando sobre o array com um loop foreach
      for (int instructionCode : instructions) {
        byte[] instruction = intToByteArray(instructionCode);
        memory.write(instruction, position);
        position += instruction.length;
      }

      try {
        System.out.println(
            STR."Memory before execution: \{Arrays.toString(memory.read(0, position))}");
      } catch (MemoryException e) {
        throw new RuntimeException(e);
      }
      System.out.println(Arrays.toString(memory.read(188, 192)));
      CPU cpu = new CPU(memory, bus);
      cpu.start();

      while (cpu.isAlive()) {
        Thread.sleep(1000);
        cpu.interrupt();
      }
      try {
        System.out.println(
                STR."Memory after execution: \{Arrays.toString(memory.read(0, position))}");
      } catch (MemoryException e) {
        throw new RuntimeException(e);
      }
    } catch (MemoryException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


}

