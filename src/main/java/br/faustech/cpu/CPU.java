package br.faustech.cpu;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.function.BiFunction;
import br.faustech.bus.Bus;
import br.faustech.comum.ComponentType;
import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import lombok.Getter;


public class CPU {

    private static int programCounter;
    @Getter
    private static int[] registers = new int[32]; // 32 general-purpose registers
    private static final int[] csrRegisters = new int[4096]; // CSR registers
    private  static Memory memory;  // Assuming a Memory class with a constructor that takes the size
    private static Bus bus;
    public CPU(Memory memory, Bus bus) {
        programCounter = 0;
        CPU.memory = memory;
        CPU.bus = bus;
    }

    public static void executeInstruction(int instruction) {
        String decodedInstruction = Decoder.decodeInstruction(instruction);

        // Parse the decoded instruction
        String[] parts = decodedInstruction.split(" ");
        if (parts.length < 2) {
            System.out.println("Invalid instruction format: " + decodedInstruction);
            return;
        }

        String operation = parts[0];
        programCounter += 4; // Increment PC for next instruction, by default

        switch (operation) {
            case "add":
                executeRType(parts, Integer::sum);
                break;
            case "sub":
                executeRType(parts, (a, b) -> a - b);
                break;
            case "sll":
                executeRType(parts, (a, b) -> a << b);
                break;
            case "slt":
                executeRType(parts, (a, b) -> a < b ? 1 : 0);
                break;
            case "sltu":
                executeRType(parts, (a, b) -> Integer.compareUnsigned(a, b) < 0 ? 1 : 0);
                break;
            case "xor":
                executeRType(parts, (a, b) -> a ^ b);
                break;
            case "srl":
                executeRType(parts, (a, b) -> a >>> b);
                break;
            case "sra":
                executeRType(parts, (a, b) -> a >> b);
                break;
            case "or":
                executeRType(parts, (a, b) -> a | b);
                break;
            case "and":
                executeRType(parts, (a, b) -> a & b);
                break;
            case "lui":
                executeUType(parts, (imm, pc) -> imm << 12);
                break;
            case "auipc":
                executeUType(parts, (imm, pc) -> (imm << 12) + pc);
                break;
            case "jal":
                executeJType(parts);
                break;
            case "jalr":
                executeITypeJumpAndLinkRegister(parts);
                break;
            case "lb":
            case "lh":
            case "lw":
            case "lbu":
            case "lhu":
                executeITypeLoad(parts);
                break;
            case "beq":
            case "bne":
            case "blt":
            case "bge":
            case "bltu":
            case "bgeu":
                executeBType(parts);
                break;
            case "sb":
            case "sh":
            case "sw":
                executeSType(parts);
                break;
            case "addi":
            case "slti":
            case "sltiu":
            case "xori":
            case "ori":
            case "andi":
            case "slli":
            case "srli":
            case "srai":
                executeITypeImmediate(parts);
                break;
            case "ecall":
            case "ebreak":
            case "csrrw":
            case "csrrs":
            case "csrrc":
            case "csrrwi":
            case "csrrsi":
            case "csrrci":
                executeITypeControlStatusRegister(parts);
                break;
            default:
                System.out.println("Unknown operation: " + operation);
                programCounter -= 4; // Revert PC increment if the operation is unknown
        }
    }

    private static void executeSType(String[] parts) {
        int rs1 = getRegisterIndex(parts, 1);
        int rs2 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        int address = registers[rs1] + imm;
        if (address < 0 || address >= 4096) {
            System.out.println("Memory access out of bounds: " + address);
            return;
        }

        switch (parts[0]) {
            case "sb":
                bus.write(ComponentType.MEMORY, new byte[]{(byte) address, (byte) (registers[rs2] & 0xFF)});
                break;
            case "sh":
                bus.write(ComponentType.MEMORY, new byte[]{(byte) address, (byte) (registers[rs2] & 0xFFFF)});
                break;
            case "sw":
                bus.write(ComponentType.MEMORY, new byte[]{(byte) address, (byte) registers[rs2]});
                break;
        }

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " rs2=" + rs2 + " -> mem[" + address + "]=" + registers[rs2]);
    }

    private static void executeUType(String[] parts, BiFunction<Integer, Integer, Integer> operation) {
        int rd = getRegisterIndex(parts, 1);
        int imm = getImmediateValue(parts, 2);

        registers[rd] = operation.apply(imm, programCounter - 4);
        System.out.println("Executing: " + parts[0] + " imm=" + imm + " -> rd=" + rd);
    }

    private static void executeBType(String[] parts) {
        int rs1 = getRegisterIndex(parts, 1);
        int rs2 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        boolean condition = switch (parts[0]) {
            case "beq" -> (registers[rs1] == registers[rs2]);
            case "bne" -> (registers[rs1] != registers[rs2]);
            case "blt" -> (registers[rs1] < registers[rs2]);
            case "bge" -> (registers[rs1] >= registers[rs2]);
            case "bltu" -> (Integer.compareUnsigned(registers[rs1], registers[rs2]) < 0);
            case "bgeu" -> (Integer.compareUnsigned(registers[rs1], registers[rs2]) >= 0);
            default -> false;
        };

        if (condition) {
            programCounter += imm - 4; // Adjust for the default increment
        }

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " rs2=" + rs2 + " -> PC=" + programCounter);
    }

    private static void executeJType(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int imm = getImmediateValue(parts, 2);

        registers[rd] = programCounter;
        programCounter += imm - 4; // Adjust for the default increment

        System.out.println("Executing: " + parts[0] + " imm=" + imm + " -> rd=" + rd + " PC=" + programCounter);
    }

    private static void executeITypeJumpAndLinkRegister(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        registers[rd] = programCounter;
        programCounter = (registers[rs1] + imm) & ~1;

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " imm=" + imm + " -> rd=" + rd + " PC=" + programCounter);
    }

    private static void executeITypeLoad(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        int address = registers[rs1] + imm;
        if (address < 0) {
            System.out.println("Memory access out of bounds: " + address);
            return;
        }

        byte[] data = bus.read(ComponentType.MEMORY, new byte[]{(byte) address, (byte) (address + 1)});
        int value = data[0]; // Assuming read returns the data in the expected format

        switch (parts[0]) {
            case "lb":
                registers[rd] = (byte) value;
                break;
            case "lh":
                registers[rd] = (short) value;
                break;
            case "lw":
                registers[rd] = value;
                break;
            case "lbu":
                registers[rd] = value & 0xFF;
                break;
            case "lhu":
                registers[rd] = value & 0xFFFF;
                break;
        }

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " imm=" + imm + " -> rd=" + rd);
    }

    private static void executeITypeControlStatusRegister(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int csr = getImmediateValue(parts, 3);

        int csrValue = csrRegisters[csr];
        switch (parts[0]) {
            case "csrrw":
                csrRegisters[csr] = registers[rs1];
                registers[rd] = csrValue;
                break;
            case "csrrs":
                csrRegisters[csr] |= registers[rs1];
                registers[rd] = csrValue;
                break;
            case "csrrc":
                csrRegisters[csr] &= ~registers[rs1];
                registers[rd] = csrValue;
                break;
            case "csrrwi":
                csrRegisters[csr] = rs1;
                registers[rd] = csrValue;
                break;
            case "csrrsi":
                csrRegisters[csr] |= rs1;
                registers[rd] = csrValue;
                break;
            case "csrrci":
                csrRegisters[csr] &= ~rs1;
                registers[rd] = csrValue;
                break;
        }

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " csr=" + csr + " -> rd=" + rd);
    }

    private static void executeITypeImmediate(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        int result = switch (parts[0]) {
            case "addi" -> registers[rs1] + imm;
            case "slti" -> (registers[rs1] < imm) ? 1 : 0;
            case "sltiu" -> (Integer.compareUnsigned(registers[rs1], imm) < 0) ? 1 : 0;
            case "xori" -> registers[rs1] ^ imm;
            case "ori" -> registers[rs1] | imm;
            case "andi" -> registers[rs1] & imm;
            case "slli" -> registers[rs1] << imm;
            case "srli" -> registers[rs1] >>> imm;
            case "srai" -> registers[rs1] >> imm;
            default -> 0;
        };

        registers[rd] = result;
        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " imm=" + imm + " -> rd=" + rd);
    }

    private static void executeRType(String[] parts, BiFunction<Integer, Integer, Integer> operation) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int rs2 = getRegisterIndex(parts, 3);

        // Access the values in registers rs1 and rs2
        int value1 = registers[rs1];
        int value2 = registers[rs2];

        // Perform the operation and store the result in register rd
        registers[rd] = operation.apply(value1, value2);

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " rs2=" + rs2 + " -> rd=" + rd);
    }

    private static int getRegisterIndex(String[] parts, int partIndex) {
        try {
            return Integer.parseInt(parts[partIndex].split("=")[1].replace(",", ""));
        } catch (Exception e) {
            System.out.println("Invalid register index in part: " + parts[partIndex]);
            return 0; // Default to register 0 if there's an error
        }
    }

    private static int getImmediateValue(String[] parts, int partIndex) {
        try {
            return Integer.parseInt(parts[partIndex].split("=")[1].replace(",", ""));
        } catch (Exception e) {
            System.out.println("Invalid immediate value in part: " + parts[partIndex]);
            return 0; // Default to immediate value 0 if there's an error
        }
    }
}
