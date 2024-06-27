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
      // Definindo as instruções binárias em um array
      int[] instructions = {0b00000000010100011010000000000011, 0b00000000010000011010000010000011,
          0b00000000000100000000000100110011, 0b00010000000100000000000110110011,
          0b00000000000100000001001000110011, 0b00000000000100000010001010110011,
          0b00000000000100000011001100110011, 0b00000000000100000100001110110011,
          0b00000000000100000101010000110011, 0b01000000000100000101010010110011,
          0b00000000000100000110010100110011, 0b00000000000100000111010110110011,
          0b00000000001100001001011000110111, 0b00000000000000000000011010010111,
          0b00000000000000000000011101101111, 0b00000100000000000000011111100111,
          0b00000000011000101000100001100011, 0b00000000000100000001100001100011,
          0b00000000001000000100100001100011, 0b00000000000100000101100001100011,
          0b00000000010000001110100001100011, 0b00000000011000101111100001100011,
          0b00000001000000001000100000010011, 0b00000001000000001010100010010011,
          0b00000001000000001011100100010011, 0b00000001000000001100100110010011,
          0b00000001000000001110101000010011, 0b00000001000000001111101010010011,
          0b00000000000000001001101100010011, 0b00000000000100001101101110010011,
          0b00000000001000001101110000010011, 0b00000000000000000000000001110011,
          0b00000000000100000000000001110011, 0b00000000000000000001110011110011,
          0b00000000000100001010110101110011, 0b00000000001000010011110111110011,
          0b00000000001100011101111001110011, 0b00000000010000100110111011110011,
          0b00000000010100101111111101110011, 0b00000000010000011010000010000011};

      int position = 0;
      // Iterando sobre o array com um loop foreach
      for (int instructionCode : instructions) {
        byte[] instruction = intToByteArray(instructionCode);
        memory.write(instruction, position);
        position += instruction.length;
      }

      try {
        System.out.println(
            STR."Memory before execution: \{Arrays.toString(memory.read(0, position + 5))}");
      } catch (MemoryException e) {
        throw new RuntimeException(e);
      }

      CPU cpu = new CPU(memory, bus);
      cpu.start();

      while (true) {
        if (cpu.isAlive()) {
          continue;
        } else {
          Thread.sleep(1000);
          cpu.interrupt();
        }
      }
    } catch (MemoryException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}

