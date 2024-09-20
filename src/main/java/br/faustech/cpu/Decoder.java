package br.faustech.cpu;

/**
 * Class responsible for decoding CPU instructions.
 */
public class Decoder {

    /**
     * Decodes a given instruction and returns a human-readable string.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the decoded instruction.
     */
    public static String decodeInstruction(int instruction) {

        int opcode = instruction & 0x7F; // Extract the 7-bit opcode

        return switch (opcode) {
            case 0x33 -> decodeRType(instruction);              // R-Type
            case 0x67 -> decodeITypeJumpAndLinkRegister(instruction); // I-Type-jalr
            case 0x03 -> decodeITypeLoad(instruction);          // I-Type-load
            case 0x13 -> decodeITypeImmediate(instruction);     // I-Type-immediate
            case 0x73 -> decodeITypeControlStatusRegister(instruction); // I-Type-csr
            case 0x23 -> decodeSType(instruction);              // S-Type
            case 0x63 -> decodeBType(instruction);              // B-Type
            case 0x37, 0x17 -> decodeUType(instruction);        // U-Type
            case 0x6F -> decodeJType(instruction);              // J-Type
            default -> "Unknown Type";                          // Default case for unknown opcode
        };
    }

    /**
     * Decodes an R-Type instruction.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the R-Type instruction.
     */
    private static String decodeRType(int instruction) {

        int funct3 = (instruction >> 12) & 0x7;
        int funct7 = (instruction >> 25) & 0x7F;
        int rd = (instruction >> 7) & 0x1F;
        int rs1 = (instruction >> 15) & 0x1F;
        int rs2 = (instruction >> 20) & 0x1F;

        String operation = switch (funct3) {
            case 0b000 -> (funct7 == 0) ? "add" : "sub";
            case 0b001 -> "sll";
            case 0b010 -> "slt";
            case 0b011 -> "sltu";
            case 0b100 -> "xor";
            case 0b101 -> (funct7 == 0) ? "srl" : "sra";
            case 0b110 -> "or";
            case 0b111 -> "and";
            default -> "unknown";
        };

        return String.format("%s rd=%d, rs1=%d, rs2=%d", operation, rd, rs1, rs2);
    }

    /**
     * Decodes an I-Type instruction for Jump and Link Register.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the I-Type jalr instruction.
     */
    private static String decodeITypeJumpAndLinkRegister(int instruction) {

        int imm = instruction >> 20;
        int rs1 = (instruction >> 15) & 0x1F;
        int rd = (instruction >> 7) & 0x1F;

        return String.format("jalr rd=%d, rs1=%d, imm=%d", rd, rs1, imm);
    }

    /**
     * Decodes an I-Type instruction for Loads.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the I-Type load instruction.
     */
    private static String decodeITypeLoad(int instruction) {

        int imm = instruction >> 20;
        int rs1 = (instruction >> 15) & 0x1F;
        int funct3 = (instruction >> 12) & 0x7;
        int rd = (instruction >> 7) & 0x1F;

        String operation = switch (funct3) {
            case 0b000 -> "lb";
            case 0b001 -> "lh";
            case 0b010 -> "lw";
            case 0b100 -> "lbu";
            case 0b101 -> "lhu";
            default -> "unknown";
        };

        return String.format("%s rd=%d, rs1=%d, imm=%d", operation, rd, rs1, imm);
    }

    /**
     * Decodes an I-Type instruction for Immediates.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the I-Type immediate instruction.
     */
    private static String decodeITypeImmediate(int instruction) {

        int imm = instruction >> 20;
        int rs1 = (instruction >> 15) & 0x1F;
        int funct3 = (instruction >> 12) & 0x7;
        int rd = (instruction >> 7) & 0x1F;

        String operation = switch (funct3) {
            case 0b000 -> "addi";
            case 0b010 -> "slti";
            case 0b011 -> "sltiu";
            case 0b100 -> "xori";
            case 0b110 -> "ori";
            case 0b111 -> "andi";
            case 0b001 -> {
                imm &= 0x1F;
                yield String.format("slli rd=%d, rs1=%d, shamt=%d", rd, rs1, imm);
            }
            case 0b101 -> {
                String op = (imm & 0xFE0) == 0 ? "srli" : "srai";
                imm &= 0x1F;
                yield String.format("%s rd=%d, rs1=%d, shamt=%d", op, rd, rs1, imm);
            }
            default -> "unknown";
        };

        return String.format("%s rd=%d, rs1=%d, imm=%d", operation, rd, rs1, imm);
    }

    /**
     * Decodes an I-Type instruction for Control Status Register Atomic Operations.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the I-Type CSR instruction.
     */
    private static String decodeITypeControlStatusRegister(int instruction) {

        int csr = instruction >> 20;
        int rs1_or_zimm = (instruction >> 15) & 0x1F;
        int funct3 = (instruction >> 12) & 0x7;
        int rd = (instruction >> 7) & 0x1F;
        String operation = "";
        switch (funct3) {
            case 0b000 -> {
                if (csr == 0) {
                    operation = "ecall";
                } else if (csr == 0x01) {
                    operation = "ebreak";
                } else if (csr > 0x01) {
                    operation = "mret";
                }
            }
            case 0b001 -> operation = "csrrw";
            case 0b010 -> operation = "csrrs";
            case 0b011 -> operation = "csrrc";
            case 0b101 -> operation = "csrrwi";
            case 0b110 -> operation = "csrrsi";
            case 0b111 -> operation = "csrrci";
            default -> operation = "unknown";
        }

        return (funct3 == 0b000) ? operation
                : String.format("%s rd=%d, csr=%d, rs1_or_zimm=%d", operation, rd, csr, rs1_or_zimm);
    }

    /**
     * Decodes an S-Type instruction.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the S-Type instruction.
     */

    private static String decodeSType(int instruction) {

        int imm11_5 = (instruction >> 25) & 0x7F;
        int rs2 = (instruction >> 20) & 0x1F;
        int rs1 = (instruction >> 15) & 0x1F;
        int funct3 = (instruction >> 12) & 0x7;
        int imm4_0 = (instruction >> 7) & 0x1F;
        int imm = (imm11_5 << 5) | imm4_0;

        String operation = switch (funct3) {
            case 0b000 -> "sb";
            case 0b001 -> "sh";
            case 0b010 -> "sw";
            default -> "unknown";
        };

        return String.format("%s rs1=%d, rs2=%d, imm=%d", operation, rs1, rs2, imm);
    }

    /**
     * Decodes a B-Type instruction.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the B-Type instruction.
     */
    private static String decodeBType(int instruction) {

        int imm12 = (instruction >> 31) & 0x1;
        int imm10_5 = (instruction >> 25) & 0x3F;
        int rs2 = (instruction >> 20) & 0x1F;
        int rs1 = (instruction >> 15) & 0x1F;
        int funct3 = (instruction >> 12) & 0x7;
        int imm4_1 = (instruction >> 8) & 0xF;
        int imm11 = (instruction >> 7) & 0x1;
        int imm = (imm12 << 12) | (imm11 << 11) | (imm10_5 << 5) | (imm4_1 << 1);

        String operation = switch (funct3) {
            case 0b000 -> "beq";
            case 0b001 -> "bne";
            case 0b100 -> "blt";
            case 0b101 -> "bge";
            case 0b110 -> "bltu";
            case 0b111 -> "bgeu";
            default -> "unknown";
        };

        return String.format("%s rs1=%d, rs2=%d, imm=%d", operation, rs1, rs2, imm);
    }

    /**
     * Decodes a U-Type instruction.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the U-Type instruction.
     */
    private static String decodeUType(int instruction) {

        int imm31_12 = instruction >> 12;
        int rd = (instruction >> 7) & 0x1F;
        int opcode = instruction & 0x7F;

        String operation = switch (opcode) {
            case 0b0110111 -> "lui";
            case 0b0010111 -> "auipc";
            default -> "unknown";
        };

        return String.format("%s rd=%d, imm=%d", operation, rd, imm31_12);
    }

    /**
     * Decodes a J-Type instruction.
     *
     * @param instruction The 32-bit instruction to decode.
     * @return A string representation of the J-Type instruction.
     */
    private static String decodeJType(int instruction) {

        int imm20 = (instruction >> 31) & 0x1;
        int imm10_1 = (instruction >> 21) & 0x3FF;
        int imm11 = (instruction >> 20) & 0x1;
        int imm19_12 = (instruction >> 12) & 0xFF;
        int rd = (instruction >> 7) & 0x1F;
        int imm = (imm20 << 20) | (imm19_12 << 12) | (imm11 << 11) | (imm10_1 << 1);

        return String.format("jal rd=%d, imm=%d", rd, imm);
    }

}
