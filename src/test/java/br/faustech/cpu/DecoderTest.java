package br.faustech.cpu;

import static br.faustech.cpu.Decoder.decodeInstruction;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DecoderTest {

  @Test
  public void decoderTest() {
    //Set instruction ADD
    int instruction = 0b00000000000000011000001010110011;
    String type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("add rd=5, rs1=3, rs2=0", type);

    //Set instruction SUB
    instruction = 0b01000000000000011000001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("sub rd=5, rs1=3, rs2=0", type);

    //Set instruction SLL
    instruction = 0b00000000000000011001001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("sll rd=5, rs1=3, rs2=0", type);

    //Set instruction SLT
    instruction = 0b00000000000000011010001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("slt rd=5, rs1=3, rs2=0", type);

    //Set instruction SLTU
    instruction = 0b00000000000000011011001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("sltu rd=5, rs1=3, rs2=0", type);

    //Set instruction XOR
    instruction = 0b00000000000000011100001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("xor rd=5, rs1=3, rs2=0", type);

    //Set instruction SRL
    instruction = 0b00000000000000011101001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("srl rd=5, rs1=3, rs2=0", type);

    //Set instruction SRA
    instruction = 0b01000000000000011101001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("sra rd=5, rs1=3, rs2=0", type);

    //Set instruction OR
    instruction = 0b00000000000000011110001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("or rd=5, rs1=3, rs2=0", type);

    //Set instruction AND
    instruction = 0b00000000000000011111001010110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("and rd=5, rs1=3, rs2=0", type);

    //Set instruction LUI
    instruction = 0b00000000000000011101001010110111;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("lui rd=5, imm=29", type);

    //Set instruction AUIPC
    instruction = 0b00000000000000011000001010010111;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("auipc rd=5, imm=24", type);

    //Set instruction JAL
    instruction = 0b00000000000000011000001011101111;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("jal rd=5, imm=49152", type);

    //Set instruction JALR
    instruction = 0b00000000000000011000001011100111;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("jalr rd=5, rs1=3, imm=0", type);

    //Set instruction LB
    instruction = 0b00000000000000011000001010000011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("lb rd=5, rs1=3, imm=0", type);

    //Set instruction LH
    instruction = 0b00000000000000011001001010000011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("lh rd=5, rs1=3, imm=0", type);

    //Set instruction LW
    instruction = 0b00000000000000011010001010000011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("lw rd=5, rs1=3, imm=0", type);

    //Set instruction LBU
    instruction = 0b00000000000000011100001010000011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("lbu rd=5, rs1=3, imm=0", type);

    //Set instruction LHU
    instruction = 0b00000000000000011101001010000011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("lhu rd=5, rs1=3, imm=0", type);

    // Set instruction BEQ
    instruction = 0b00000000000000011000001011100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("beq rs1=3, rs2=0, imm=1026", type);

    // Set instruction BNE
    instruction = 0b00000000000000011001001011100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("bne rs1=3, rs2=0, imm=1026", type);

    // Set instruction BLT
    instruction = 0b00000000000000011100001011100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("blt rs1=3, rs2=0, imm=1026", type);

    // Set instruction BGE
    instruction = 0b00000000000000011101001011100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("bge rs1=3, rs2=0, imm=1026", type);

    // Set instruction BLTU
    instruction = 0b00000000000000011110001011100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("bltu rs1=3, rs2=0, imm=1026", type);

    // Set instruction BGEU
    instruction = 0b00000000000000011111001011100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("bgeu rs1=3, rs2=0, imm=1026", type);

    // Set instruction SB
    instruction = 0b00000000000000011000001010100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("sb rs1=3, rs2=0, imm=5", type);

    // Set instruction SH
    instruction = 0b00000000000000011001001010100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("sh rs1=3, rs2=0, imm=5", type);

    // Set instruction SW
    instruction = 0b00000000000000011010001010100011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("sw rs1=3, rs2=0, imm=5", type);

    // Set instruction ADDI
    instruction = 0b00000000000000011000001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("addi rd=5, rs1=3, imm=0", type);

    // Set instruction SLTI
    instruction = 0b00000000000000011010001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("slti rd=5, rs1=3, imm=0", type);

    // Set instruction SLTIU
    instruction = 0b00000000000000011011001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("sltiu rd=5, rs1=3, imm=0", type);

    // Set instruction XORI
    instruction = 0b00000000000000011100001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("xori rd=5, rs1=3, imm=0", type);

    // Set instruction ORI
    instruction = 0b00000000000000011110001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("ori rd=5, rs1=3, imm=0", type);

    // Set instruction ANDI
    instruction = 0b00000000000000011111001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("andi rd=5, rs1=3, imm=0", type);

    // Set instruction SLLI
    instruction = 0b00000000000000011001001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("slli rd=5, rs1=3, shamt=0", type);

    // Set instruction SRLI
    instruction = 0b00000000000000011101001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("srli rd=5, rs1=3, shamt=0", type);

    // Set instruction SRAI
    instruction = 0b01000000000000011101001010010011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("srai rd=5, rs1=3, shamt=0", type);

    // Set instruction ECALL
    instruction = 0b00000000000000011000001011110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("ecall", type);

    // Set instruction EBREAK
    instruction = 0b00000000000100011000001011110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("ebreak", type);

    // Set instruction CSRRW
    instruction = 0b00000000000000011001001011110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("csrrw rd=5, csr=0, rs1=3", type);

    // Set instruction CSRRS
    instruction = 0b00000000000000011010001011110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("csrrs rd=5, csr=0, rs1=3", type);

    // Set instruction CSRRC
    instruction = 0b00000000000000011011001011110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("csrrc rd=5, csr=0, rs1=3", type);

    // Set instruction CSRRWI
    instruction = 0b00000000000000011101001011110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("csrrwi rd=5, csr=0, rs1=3", type);

    // Set instruction CSRRSI
    instruction = 0b00000000000000011110001011110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("csrrsi rd=5, csr=0, rs1=3", type);

    // Set instruction CSRRCI
    instruction = 0b00000000000000011111001011110011;
    type = decodeInstruction(instruction);
    // Verify the result
    assertEquals("csrrci rd=5, csr=0, rs1=3", type);
  }

}
