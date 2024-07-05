package br.faustech.cpu;

import br.faustech.bus.Bus;
import br.faustech.comum.ComponentThread;
import br.faustech.gpu.FrameBuffer;
import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import java.util.function.BiFunction;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 * CPU class represents a simulated CPU implementing a subset of RISC-V instructions.
 */
@Log
public class CPU extends ComponentThread {

  /** General-purpose registers. */
  @Getter private static final int[] registers = new int[32];

  /** CSR (Control and Status Register) registers. */
  private static final int[] csrRegisters = new int[4096];

  /** Program counter (PC). */
  @Getter private static int programCounter;

  private static Bus bus;

  /**
   * Constructor for CPU class.
   *
   * @param addresses Memory addresses for CPU operation.
   * @param bus       Bus object for memory access.
   */
  public CPU(final int[] addresses, final Bus bus) {

    super(addresses);
    programCounter = 0;
    CPU.bus = bus;
  }

  @Override
  public void run() {

    programCounter = 0;
    // TODO: Implement the CPU execution loop
    while (true) {
      getNextInstructionInMemory();
    }
  }

  /**
   * Retrieves the next instruction from memory and executes it.
   */
  public static void getNextInstructionInMemory() {

    try {
      int instruction = bus.read(programCounter, programCounter + 4)[0];
      executeInstruction(instruction);
    } catch (MemoryException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Executes the given instruction.
   *
   * @param instruction The instruction to execute.
   * @throws MemoryException If there is an issue with memory access.
   */
  public static void executeInstruction(int instruction) throws MemoryException {

    String decodedInstruction = Decoder.decodeInstruction(instruction);
    String[] parts = decodedInstruction.split(" ");

    String operation = parts[0];
    programCounter += 4; // Increment PC for next instruction, by default

    switch (operation) {
      case "add":
      case "sub":
      case "sll":
      case "slt":
      case "sltu":
      case "xor":
      case "srl":
      case "sra":
      case "or":
      case "and":
        executeRType(parts, (a, b) -> {
          switch (operation) {
            case "add":
              return Integer.sum(a, b);
            case "sub":
              return a - b;
            case "sll":
              return a << b;
            case "slt":
              return (a < b) ? 1 : 0;
            case "sltu":
              return (Integer.compareUnsigned(a, b) < 0) ? 1 : 0;
            case "xor":
              return a ^ b;
            case "srl":
              return a >>> b;
            case "sra":
              return a >> b;
            case "or":
              return a | b;
            case "and":
              return a & b;
            default:
              throw new RuntimeException("Unsupported operation: " + operation);
          }
        });
        break;
      case "lui":
      case "auipc":
        executeUType(parts);
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
        executeEType(parts);
        break;
      case "csrrw":
      case "csrrs":
      case "csrrc":
      case "csrrwi":
      case "csrrsi":
      case "csrrci":
        executeITypeControlStatusRegister(parts);
        break;
      default:
        programCounter -= 4; // Revert PC increment if the operation is unknown
        throw new RuntimeException("Unknown operation: " + operation);
    }
  }

  /**
   * Executes R-Type instructions (register-register operations).
   *
   * @param parts     The parts of the decoded instruction.
   * @param operation The operation to perform.
   */
  private static void executeRType(String[] parts,
      BiFunction<Integer, Integer, Integer> operation) {

    int rd = getRegisterIndex(parts, 1);
    int rs1 = getRegisterIndex(parts, 2);
    int rs2 = getRegisterIndex(parts, 3);

    int value1 = registers[rs1];
    int value2 = registers[rs2];

    registers[rd] = operation.apply(value1, value2);

    log.info(String.format("Executing: %s rs1=%d rs2=%d -> rd=%d", parts[0], rs1, rs2, rd));
  }

  /**
   * Executes U-Type instructions (upper immediate).
   *
   * @param parts The parts of the decoded instruction.
   */
  private static void executeUType(String[] parts) {

    int rd = getRegisterIndex(parts, 1);
    int imm = getImmediateValue(parts, 2);

    switch (parts[0]) {
      case "lui":
        registers[rd] = imm;
        break;
      case "auipc":
        programCounter -= 4; // Adjust for the default increment
        registers[rd] = imm + programCounter;
        programCounter += 4;
        break;
    }

    log.info(String.format("Executing: %s imm=%d -> rd=%d", parts[0], imm, rd));
  }

  /**
   * Executes J-Type instructions (jump and link).
   *
   * @param parts The parts of the decoded instruction.
   */
  private static void executeJType(String[] parts) {

    int rd = getRegisterIndex(parts, 1);
    int imm = getImmediateValue(parts, 2);

    registers[rd] = programCounter;
    programCounter += imm - 4; // Adjust for the default increment

    log.info(
        String.format("Executing: %s imm=%d -> rd=%d PC=%d", parts[0], imm, rd, programCounter));
  }

  /**
   * Executes I-Type instructions for jump and link register.
   *
   * @param parts The parts of the decoded instruction.
   */
  private static void executeITypeJumpAndLinkRegister(String[] parts) {

    int rd = getRegisterIndex(parts, 1);
    int rs1 = getRegisterIndex(parts, 2);
    int imm = getImmediateValue(parts, 3);

    registers[rd] = programCounter;
    programCounter = (registers[rs1] + imm) & ~1;

    log.info(String.format("Executing: %s rs1=%d imm=%d -> rd=%d PC=%d", parts[0], rs1, imm, rd,
        programCounter));
  }

  /**
   * Executes I-Type instructions for loads.
   *
   * @param parts The parts of the decoded instruction.
   * @throws MemoryException If there is an issue with memory access.
   */
  private static void executeITypeLoad(String[] parts) throws MemoryException {

    int rd = getRegisterIndex(parts, 1);
    int rs1 = getRegisterIndex(parts, 2);
    int imm = getImmediateValue(parts, 3);

    int address = registers[rs1] + imm;
    if (address < 0) {
      throw new MemoryException(String.format("Memory access out of bounds: %d", address));
    }

    int value = bus.read(address, address + 4)[0];

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

    log.info(
        String.format("Executing: %s rs1=%d imm=%d -> rd=%d address=%d value=%d", parts[0], rs1,
            imm, rd, address, value));
  }

  /**
   * Executes B-Type instructions (branch).
   *
   * @param parts The parts of the decoded instruction.
   */
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

    log.info(String.format("Executing: %s rs1=%d rs2=%d imm=%d -> PC=%d", parts[0], rs1, rs2, imm,
        programCounter));
  }

  /**
   * Executes S-Type instructions (store).
   *
   * @param parts The parts of the decoded instruction.
   */
  private static void executeSType(String[] parts) {

    int rs1 = getRegisterIndex(parts, 1);
    int rs2 = getRegisterIndex(parts, 2);
    int imm = getImmediateValue(parts, 3);

    int address = (registers[rs1] + imm);
    if (address < 0 || address >= Memory.getMemorySize() + FrameBuffer.getBufferSize()) {
      throw new RuntimeException(String.format("Memory access out of bounds: %d", address));
    }

    switch (parts[0]) {
      case "sb":
        bus.write(address, new int[]{registers[rs2] & 0xFF});
        break;
      case "sh":
        bus.write(address, new int[]{(registers[rs2] & 0xFFFF)});
        break;
      case "sw":
        bus.write(address, new int[]{registers[rs2]});
        break;
    }

    log.info(
        String.format("Executing: %s rs1=%d rs2=%d imm=%d -> address=%d", parts[0], rs1, rs2, imm,
            address));
  }

  /**
   * Executes I-Type instructions with immediate values.
   *
   * @param parts The parts of the decoded instruction.
   */
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
    log.info(String.format("Executing: %s rs1=%d imm=%d -> rd=%d", parts[0], rs1, imm, rd));
  }

  /**
   * Executes E-Type instructions (environment).
   *
   * @param parts The parts of the decoded instruction.
   */
  private static void executeEType(String[] parts) {

    switch (parts[0]) {
      case "ecall":
        handleEcall();
        break;
      case "ebreak":
        handleEbreak();
        break;
    }
  }

  /**
   * Executes I-Type instructions for control and status register (CSR) operations.
   *
   * @param parts The parts of the decoded instruction.
   */
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

    log.info(String.format("Executing: %s rs1=%d csr=%d -> rd=%d", parts[0], rs1, csr, rd));
  }

  /**
   * Retrieves the register index from the parts of a decoded instruction.
   *
   * @param parts     The parts of the decoded instruction.
   * @param partIndex The index of the part containing the register index.
   * @return The register index.
   */
  private static int getRegisterIndex(String[] parts, int partIndex) {

    try {
      return Integer.parseInt(parts[partIndex].split("=")[1].replace(",", ""));
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Invalid register index in part: %s", parts[partIndex]));
    }
  }

  /**
   * Retrieves the immediate value from the parts of a decoded instruction.
   *
   * @param parts     The parts of the decoded instruction.
   * @param partIndex The index of the part containing the immediate value.
   * @return The immediate value.
   */
  private static int getImmediateValue(String[] parts, int partIndex) {

    try {
      return Integer.parseInt(parts[partIndex].split("=")[1].replace(",", ""));
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("Invalid immediate value in part: %s", parts[partIndex]));
    }
  }

  /**
   * Handles an ecall instruction.
   */
  private static void handleEcall() {
    //TODO: Implement handling of ecall instruction
    log.info("ECALL: Transferred control to exception handler for syscall");
  }

  /**
   * Handles an ebreak instruction.
   */
  private static void handleEbreak() {
    //TODO: Implement handling of ebreak instruction
    throw new RuntimeException("Program has terminated via syscall exit.");
  }

}
