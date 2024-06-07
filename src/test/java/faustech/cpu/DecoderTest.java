package faustech.cpu;

import static br.faustech.cpu.Decoder.decodeInstruction;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DecoderTest {

  @Test
  public void decoderTest() {

    int instruction = 0b00000000000000011101001011110011;
    String type = decodeInstruction(instruction);

    // Verify the result
    assertEquals("csrrwi rd=5, csr=0, rs1_or_zimm=3", type);
  }

}
