package br.faustech.cpu;

public class Decoder {

  // Method to decode instructions
  public static String decodeInstruction(int instruction) {
    // Isolate the first 7 bits of the instruction, those are responsible for selecting the type of the instruction
    int opcode = instruction & 0x7F; // 0x7F = 0b01111111 (máscara para 7 bits)

    // Compare de opcode we receive to find its respective type
    return switch (opcode) {
      case 0x33 -> // 0b0110011
          decodeRType(instruction);            //R-Type
      case 0x67 -> // 0b1100111
          decodeITypeJumpAndLinkRegister(instruction);        //I-Type-jalr
      case 0x03 -> // 0b0000011
          decodeITypeLoad(instruction);        //I-Type-immediate
      case 0x13 -> // 0b0010011
          decodeITypeImmediate(instruction);   //I-Type-Load
      case 0x73 -> // 0b1110011
          decodeITypeControlStatusRegister(instruction);         //I-Type-Csr
      case 0x23 -> // 0b0100011
          decodeSType(instruction);            //S-Type
      case 0x63 -> // 0b1100011
          decodeBType(instruction);            //B-Type
      case 0x37 -> // 0b0110111
          decodeUType(instruction);            //U-Type
      case 0x17 -> // 0b0010111
          decodeUType(instruction);            //U-Type
      case 0x6F -> // 0b1101111
          decodeJType(instruction);            //J-Type
      default -> "Unknown Type";
    };
  }

  //Decode the R Type instruction
  private static String decodeRType(int instruction) {
    //Build the R-type instruction format

    int funct3 = (instruction >> 12)
        & 0x7; // Used to find which instruction from the Type is being called in the function
    int funct7 = (instruction >> 25)
        & 0x7F;// Usually is used in the Immediate or signal control in operation.
    int rd = (instruction >> 7) & 0x1F;     // Destination Register
    int rs1 = (instruction >> 15) & 0x1F;   // Source Register 1
    int rs2 = (instruction >> 20) & 0x1F;   // Source Register 2

    String operation;

    switch (funct3) {
      case 0b000:
        if (funct7 == 0) {
          operation = "add";
          //Add
        } else {
          operation = "sub";
          //Subtract
        }
        break;
      case 0b001:
        operation = "sll";
        break;     //Shift Left Logical
      case 0b010:
        operation = "slt";
        break;     //Set Less Than
      case 0b011:
        operation = "sltu";
        break;    //Set Less Than Unsigned
      case 0b100:
        operation = "xor";
        break;     //Exclusive Or
      case 0b101:
        if (funct7 == 0) {
          operation = "srl";
          //Shift Right Logical
        } else {
          operation = "sra";
          //Shift Right Arithmetic
        }
        break;
      case 0b110:
        operation = "or";
        break;      //Or
      case 0b111:
        operation = "and";
        break;     //And
      default:
        operation = "unknown";
        break;
    }

    return String.format("%s rd=%d, rs1=%d, rs2=%d", operation, rd, rs1, rs2);
  }

  //All the four methods bellow Decode the I Type instruction
  private static String decodeITypeJumpAndLinkRegister(int instruction) {
    //Build the I-type instruction format for Jump and Link Register
    int imm = instruction >> 20;
    int rs1 = (instruction >> 15) & 0x1F;
    int rd = (instruction >> 7) & 0x1F;

    String operation = "jalr";  //Jump And Link Register

    return String.format("%s rd=%d, rs1=%d, imm=%d", operation, rd, rs1, imm);
  }

  private static String decodeITypeLoad(int instruction) {
    //Build the I-type instruction format for Loads
    int imm = instruction >> 20;
    int rs1 = (instruction >> 15) & 0x1F;
    int funct3 = (instruction >> 12) & 0x7;
    int rd = (instruction >> 7) & 0x1F;

    String operation = switch (funct3) {
      case 0b000 -> "lb";    //Load Byte
      case 0b001 -> "lh";    //Load Halfword
      case 0b010 -> "lw";    //Load Word
      case 0b100 -> "lbu";   //Load Byte Unsigned
      case 0b101 -> "lhu";   //Load Halfword Unsigned
      default -> "unknown";
    };

    return String.format("%s rd=%d, rs1=%d, imm=%d", operation, rd, rs1, imm);
  }

  private static String decodeITypeImmediate(int instruction) {
    //Build the I-type instruction format for Immediates
    int imm = instruction >> 20;
    int rs1 = (instruction >> 15) & 0x1F;
    int funct3 = (instruction >> 12) & 0x7;
    int rd = (instruction >> 7) & 0x1F;

    String operation = "";
    switch (funct3) {
      case 0b000:
        operation = "addi";
        break;  //Add Immediate
      case 0b010:
        operation = "slti";
        break;  //Set Less Than Immediate
      case 0b011:
        operation = "sltiu";
        break; //Set Less Than Immediate Unsigned
      case 0b100:
        operation = "xori";
        break;  //Exclusive Or Immediate
      case 0b110:
        operation = "ori";
        break;   //Or Immediate
      case 0b111:
        operation = "andi";
        break;  //And Immediate
      case 0b001:
        operation = "slli";                 //Shift Left Logical Immediate
        imm &= 0x1F;
        return String.format("%s rd=%d, rs1=%d, shamt=%d", operation, rd, rs1, imm);
      case 0b101:
        if ((imm & 0xFE0) == 0) {
          operation = "srli";             //Shift Right Logical Immediate
        } else {
          operation = "srai";             //Shift Right Arithmetic Immediate
        }
        imm &= 0x1F; // Keep only the relevant immediate bits
        return String.format("%s rd=%d, rs1=%d, shamt=%d", operation, rd, rs1, imm);
      default:
        operation = "unknown";
        break;
    }

    return String.format("%s rd=%d, rs1=%d, imm=%d", operation, rd, rs1, imm);
  }

  private static String decodeITypeControlStatusRegister(int instruction) {
    //Build the I-type instruction format for Control Status Register Atomic Operations
    int csr = instruction >> 20;
    int rs1_or_zimm = (instruction >> 15) & 0x1F;
    int funct3 = (instruction >> 12) & 0x7;
    int rd = (instruction >> 7) & 0x1F;

    String operation = "";
    switch (funct3) {
      case 0b000:
        if (csr == 0) {
          operation = "ecall";
          //Trap to Debugger
        } else {
          operation = "ebreak";
          //Trap to Operating System
        }
        return String.format("%s", operation);
      case 0b001:
        operation = "csrrw";
        break;     //Atomic Read/Write
      case 0b010:
        operation = "csrrs";
        break;     //Atomic Read and Set
      case 0b011:
        operation = "csrrc";
        break;     //Atomic Read and Clear
      case 0b101:
        operation = "csrrwi";
        break;    //Atomic Read/Write Immediate
      case 0b110:
        operation = "csrrsi";
        break;    //Atomic Read and Set Immediate
      case 0b111:
        operation = "csrrci";
        break;    //Atomic Read and Clear Immediate
      default:
        operation = "unknown";
        break;
    }

    return String.format("%s rd=%d, csr=%d, rs1_or_zimm=%d", operation, rd, csr, rs1_or_zimm);

  }

  //Decode the S Type instruction
  private static String decodeSType(int instruction) {
    //Build the S-type instruction format
    int imm11_5 = (instruction >> 25) & 0x7F;
    int rs2 = (instruction >> 20) & 0x1F;
    int rs1 = (instruction >> 15) & 0x1F;
    int funct3 = (instruction >> 12) & 0x7;
    int imm4_0 = (instruction >> 7) & 0x1F;

    int imm = (imm11_5 << 5) | imm4_0;

    String operation = switch (funct3) {
      case 0b000 -> "sb";    //Store Byte
      case 0b001 -> "sh";    //Store Halfword
      case 0b010 -> "sw";    //Store Word
      default -> "unknown";
    };

    return String.format("%s rs1=%d, rs2=%d, imm=%d", operation, rs1, rs2, imm);
  }

  //Decode the B Type instruction
  private static String decodeBType(int instruction) {
    //Build the B-type instruction format
    int imm12 = (instruction >> 31) & 0x1;
    int imm10_5 = (instruction >> 25) & 0x3F;
    int rs2 = (instruction >> 20) & 0x1F;
    int rs1 = (instruction >> 15) & 0x1F;
    int funct3 = (instruction >> 12) & 0x7;
    int imm4_1 = (instruction >> 8) & 0xF;
    int imm11 = (instruction >> 7) & 0x1;

    // Unify the immediate field in one.
    int imm = (imm12 << 12) | (imm11 << 11) | (imm10_5 << 5) | (imm4_1 << 1);

    String operation = switch (funct3) {
      case 0b000 -> "beq";   //Branch Equal
      case 0b001 -> "bne";   //Branch Not Equal
      case 0b100 -> "blt";   //Branch Less Than
      case 0b101 -> "bge";   //Branch Greater or Equal
      case 0b110 -> "bltu";  //Branch Less Than Unsigned
      case 0b111 -> "bgeu";  //Branch Greater or Equal Unsigned
      default -> "unknown";
    };

    return String.format("%s rs1=%d, rs2=%d, imm=%d", operation, rs1, rs2, imm);
  }

  //Decode the U Type instruction
  private static String decodeUType(int instruction) {
    //Build the U-type instruction format
    int imm31_12 = (instruction >> 12);
    int rd = (instruction >> 7) & 0x1F;
    int opcode = instruction & 0x7F;

    String operation = switch (opcode) {
      case 0b0110111 -> "lui";   //Load Upper Immediate
      case 0b0010111 -> "auipc"; //Add Upper Immediate to PC
      default -> "unknown";
    };

    return String.format("%s rd=%d, imm=%d", operation, rd, imm31_12);
  }

  //Decode the J Type instruction
  private static String decodeJType(int instruction) {
    //Build the J-type instruction format
    int imm20 = (instruction >> 31) & 0x1;
    int imm10_1 = (instruction >> 21) & 0x3FF;
    int imm11 = (instruction >> 20) & 0x1;
    int imm19_12 = (instruction >> 12) & 0xFF;
    int rd = (instruction >> 7) & 0x1F;

    // Unify the immediate field in one.
    int imm = (imm20 << 20) | (imm19_12 << 12) | (imm11 << 11) | (imm10_1 << 1);

    String operation = "jal"; // J-Type is always Jump and Link

    return String.format("%s rd=%d, imm=%d", operation, rd, imm);
  }

}
