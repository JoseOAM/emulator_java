package br.faustech.cpu;

import java.util.Arrays;
import java.util.function.BiFunction;

public class CPU {

    private static int programCounter;
    private final int[] registers = new int[32]; // 32 general-purpose registers
    private final int[] memory = new int[4096];  // Temporary memory until actual memory and bus are built
    private final int[] csrRegisters = new int[4096]; // CSR registers

    public CPU() {
        programCounter = 0;
    }

    public void executeInstruction(int instruction) {
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

    private void executeSType(String[] parts) {
        int rs1 = getRegisterIndex(parts, 1);
        int rs2 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        int address = registers[rs1] + imm;
        if (address < 0 || address >= memory.length) {
            System.out.println("Memory access out of bounds: " + address);
            return;
        }

        switch (parts[0]) {
            case "sb":
                memory[address] = registers[rs2] & 0xFF;
                break;
            case "sh":
                memory[address] = registers[rs2] & 0xFFFF;
                break;
            case "sw":
                memory[address] = registers[rs2];
                break;
        }

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " rs2=" + rs2 + " -> mem[" + address + "]=" + registers[rs2]);
    }

    private void executeUType(String[] parts, BiFunction<Integer, Integer, Integer> operation) {
        int rd = getRegisterIndex(parts, 1);
        int imm = getImmediateValue(parts, 2);

        registers[rd] = operation.apply(imm, programCounter - 4);
        System.out.println("Executing: " + parts[0] + " imm=" + imm + " -> rd=" + rd);
    }

    private void executeBType(String[] parts) {
        int rs1 = getRegisterIndex(parts, 1);
        int rs2 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        boolean condition = false;
        switch (parts[0]) {
            case "beq":
                condition = (registers[rs1] == registers[rs2]);
                break;
            case "bne":
                condition = (registers[rs1] != registers[rs2]);
                break;
            case "blt":
                condition = (registers[rs1] < registers[rs2]);
                break;
            case "bge":
                condition = (registers[rs1] >= registers[rs2]);
                break;
            case "bltu":
                condition = (Integer.compareUnsigned(registers[rs1], registers[rs2]) < 0);
                break;
            case "bgeu":
                condition = (Integer.compareUnsigned(registers[rs1], registers[rs2]) >= 0);
                break;
        }

        if (condition) {
            programCounter += imm - 4; // Adjust for the default increment
        }

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " rs2=" + rs2 + " -> PC=" + programCounter);
    }

    private void executeJType(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int imm = getImmediateValue(parts, 2);

        registers[rd] = programCounter;
        programCounter += imm - 4; // Adjust for the default increment

        System.out.println("Executing: " + parts[0] + " imm=" + imm + " -> rd=" + rd + " PC=" + programCounter);
    }

    private void executeITypeJumpAndLinkRegister(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        registers[rd] = programCounter;
        programCounter = (registers[rs1] + imm) & ~1;

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " imm=" + imm + " -> rd=" + rd + " PC=" + programCounter);
    }

    private void executeITypeLoad(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        int address = registers[rs1] + imm;
        if (address < 0 || address >= memory.length) {
            System.out.println("Memory access out of bounds: " + address);
            return;
        }

        int value = memory[address];
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

    private void executeITypeControlStatusRegister(String[] parts) {
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

    private void executeITypeImmediate(String[] parts) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int imm = getImmediateValue(parts, 3);

        int result = 0;
        switch (parts[0]) {
            case "addi":
                result = registers[rs1] + imm;
                break;
            case "slti":
                result = (registers[rs1] < imm) ? 1 : 0;
                break;
            case "sltiu":
                result = (Integer.compareUnsigned(registers[rs1], imm) < 0) ? 1 : 0;
                break;
            case "xori":
                result = registers[rs1] ^ imm;
                break;
            case "ori":
                result = registers[rs1] | imm;
                break;
            case "andi":
                result = registers[rs1] & imm;
                break;
            case "slli":
                result = registers[rs1] << imm;
                break;
            case "srli":
                result = registers[rs1] >>> imm;
                break;
            case "srai":
                result = registers[rs1] >> imm;
                break;
        }

        registers[rd] = result;
        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " imm=" + imm + " -> rd=" + rd);
    }

    private void executeRType(String[] parts, BiFunction<Integer, Integer, Integer> operation) {
        int rd = getRegisterIndex(parts, 1);
        int rs1 = getRegisterIndex(parts, 2);
        int rs2 = getRegisterIndex(parts, 3);

        // Access the values in registers rs1 and rs2
        int value1 = registers[rs1];
        int value2 = registers[rs2];

        // Perform the operation and store the result in register rd
        registers[rd] = operation.apply(value1, value2);

        System.out.println("Executing: " + parts[0] + " rs1=" + rs1 + " rs2=" + rs2 + " -> rd=" + rd);
        System.out.println("Memory after execution: " + Arrays.toString(registers));
    }

    private int getRegisterIndex(String[] parts, int partIndex) {
        try {
            return Integer.parseInt(parts[partIndex].split("=")[1].replace(",", ""));
        } catch (Exception e) {
            System.out.println("Invalid register index in part: " + parts[partIndex]);
            return 0; // Default to register 0 if there's an error
        }
    }

    private int getImmediateValue(String[] parts, int partIndex) {
        try {
            return Integer.parseInt(parts[partIndex].split("=")[1].replace(",", ""));
        } catch (Exception e) {
            System.out.println("Invalid immediate value in part: " + parts[partIndex]);
            return 0; // Default to immediate value 0 if there's an error
        }
    }

    public static void main(String[] args) {
        CPU cpu = new CPU();

        // Set register values before executing the instruction
        cpu.registers[0] = 5;
        cpu.registers[3] = 6;

        int instruction = 0b00000000000000011001001010110011; // Example 'add' instruction
        cpu.executeInstruction(instruction);
    }
}
