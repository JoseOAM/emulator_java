package faustech.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import java.util.Arrays;
import org.junit.Test;

public class MemoryTest {

  @Test
  public void writeAndReadMemoryTest() throws MemoryException {

    Memory memory = new Memory(1024);

    final byte[] data = new byte[30];
    for (int i = 0; i < data.length; i++) {
      data[i] = (byte) (Math.random() * 256);
    }

    var data1 = new byte[5];
    data1[0] = data[0];
    data1[1] = data[1];
    data1[2] = data[2];
    data1[3] = data[3];
    data1[4] = data[4];

    var data2 = new byte[10];
    data2[0] = data[5];
    data2[1] = data[6];
    data2[2] = data[7];
    data2[3] = data[8];
    data2[4] = data[9];
    data2[5] = data[10];
    data2[6] = data[11];
    data2[7] = data[12];
    data2[8] = data[13];
    data2[9] = data[14];

    var data3 = new byte[15];
    data3[0] = data[15];
    data3[1] = data[16];
    data3[2] = data[17];
    data3[3] = data[18];
    data3[4] = data[19];
    data3[5] = data[20];
    data3[6] = data[21];
    data3[7] = data[22];
    data3[8] = data[23];
    data3[9] = data[24];
    data3[10] = data[25];
    data3[11] = data[26];
    data3[12] = data[27];
    data3[13] = data[28];
    data3[14] = data[29];

    memory.write(data1, 0);
    assertEquals(Arrays.toString(data1), Arrays.toString(memory.read(0, 5)));

    memory.write(data2, 10);
    assertEquals(Arrays.toString(data2), Arrays.toString(memory.read(10, 20)));

    memory.write(data3, 50);
    assertEquals(Arrays.toString(data3), Arrays.toString(memory.read(50, 65)));

    memory.write(data3, 0);
    assertEquals(Arrays.toString(data3), Arrays.toString(memory.read(0, 15)));

    assertThrows(MemoryException.class, () -> memory.read(0, 1025));

    assertThrows(MemoryException.class, () -> memory.write(data3, 1010));

    assertThrows(MemoryException.class, () -> memory.read(-1, 10));

    assertThrows(MemoryException.class, () -> memory.read(20, 10));
  }

}
