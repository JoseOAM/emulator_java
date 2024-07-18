package br.faustech.cpu;

import static br.faustech.Main.LOG;

import br.faustech.bus.Bus;
import br.faustech.comum.ComponentThread;
import br.faustech.memory.MemoryException;
import java.util.function.BiFunction;
import lombok.extern.java.Log;

/**
 * CPU class that extends ComponentThread to simulate a CPU execution environment. This class
 * handles the initialization of registers, the program counter, and executes instructions fetched
 * from memory.
 */
@Log
public class CPU extends ComponentThread {

  private final int[] registers = new int[32]; // 32 general-purpose registers

  private final int[] csrRegisters = new int[4096]; // CSR registers

  private final Bus bus;

  private int programCounter = 0;

  /**
   * Constructor for the CPU class.
   *
   * @param addresses array of addresses for the component
   * @param bus       the Bus instance for memory access
   */
  public CPU(final int[] addresses, final Bus bus) {

    super(addresses);
    this.bus = bus;
    initializeRegisters();
  }

  /**
   * Initializes the CPU registers with predefined values.
   */
  public void initializeRegisters() {
    // Stack Pointer (sp) to the top of the memory
    registers[2] = 4092;
    // Global Pointer (gp) to some midpoint in memory, e.g., for global data
    registers[3] = 2048;
    // Thread Pointer (tp) to some specific address for thread-local data
    registers[4] = 3000;
    // Frame Pointer (fp) to the start of the stack
    registers[8] = registers[2];
  }

  /**
   * The main execution loop of the CPU. Fetches and executes instructions continuously.
   */
  @Override
  public void run() {

    while (!isInterrupted()) {
      getNextInstructionInMemory();
    }
  }

  /**
   * Fetches the next instruction from memory and executes it.
   */
  public void getNextInstructionInMemory() {
    // Set the pc to the first memory position and start reading 4 bytes instruction and sending them to execution
    try {
      int instruction = bus.read(programCounter, programCounter + 4)[0];
      executeInstruction(instruction);
    } catch (MemoryException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Decodes and executes a given instruction.
   *
   * @param instruction the instruction to be executed
   * @throws MemoryException if there is an error accessing memory
   */
  public void executeInstruction(int instruction) throws MemoryException {

    String decodedInstruction = Decoder.decodeInstruction(instruction);
    // Parse the decoded instruction
    String[] parts = decodedInstruction.split(" ");
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
        throw new RuntimeException(String.format("Unknown operation: %s", operation));
    }
  }

  /**
   * Executes R-Type instructions which involve register-to-register operations.
   *
   * @param parts     the instruction parts
   * @param operation the operation to be performed
   */
  private void executeRType(String[] parts, BiFunction<Integer, Integer, Integer> operation) {

    int rd = getRegisterIndex(parts, 1);
    int rs1 = getRegisterIndex(parts, 2);
    int rs2 = getRegisterIndex(parts, 3);

    // Access the values in registers rs1 and rs2
    int value1 = registers[rs1];
    int value2 = registers[rs2];

    // Perform the operation and store the result in register rd
    registers[rd] = operation.apply(value1, value2);

    if (LOG) {
      log.info(String.format("Executing: %s rs1=%d rs2=%d -> rd=%d", parts[0], rs1, rs2, rd));
    }
  }

  /**
   * Executes U-Type instructions which involve immediate values.
   *
   * @param parts the instruction parts
   */
  private void executeUType(String[] parts) {

    int rd = getRegisterIndex(parts, 1);
    int imm = getImmediateValue(parts, 2) << 12;

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

    if (LOG) {
      log.info(String.format("Executing: %s imm=%d -> rd=%d", parts[0], imm, rd));
    }
  }

  /**
   * Executes J-Type instructions which involve jump operations.
   *
   * @param parts the instruction parts
   */
  private void executeJType(String[] parts) {

    int rd = getRegisterIndex(parts, 1);
    int imm = getImmediateValue(parts, 2);
    imm = signExtendImmediate(imm, 20);
    registers[rd] = programCounter;
    programCounter += imm - 4; // Adjust for the default increment

    if (LOG) {
      log.info(
          String.format("Executing: %s imm=%d -> rd=%d PC=%d", parts[0], imm, rd, programCounter));
    }
  }

  /**
   * Executes I-Type jump and link register instructions.
   *
   * @param parts the instruction parts
   */
  private void executeITypeJumpAndLinkRegister(String[] parts) {

    int rd = getRegisterIndex(parts, 1);
    int rs1 = getRegisterIndex(parts, 2);
    int imm = getImmediateValue(parts, 3);
    imm = signExtendImmediate(imm, 12);
    registers[rd] = programCounter;
    programCounter = (registers[rs1] + imm) & ~1;

    if (LOG) {
      log.info(String.format("Executing: %s rs1=%d imm=%d -> rd=%d PC=%d", parts[0], rs1, imm, rd,
          programCounter));
    }
  }

  /**
   * Executes I-Type load instructions which involve memory load operations.
   *
   * @param parts the instruction parts
   * @throws MemoryException if there is an error accessing memory
   */
  private void executeITypeLoad(String[] parts) throws MemoryException {

    int rd = getRegisterIndex(parts, 1);
    int rs1 = getRegisterIndex(parts, 2);
    int imm = getImmediateValue(parts, 3);
    imm = signExtendImmediate(imm, 12);
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

    if (LOG) {
      log.info(
          String.format("Executing: %s rs1=%d imm=%d -> rd=%d address=%d value=%d", parts[0], rs1,
              imm, rd, address, value));
    }
  }

  /**
   * Executes B-Type instructions which involve conditional branches.
   *
   * @param parts the instruction parts
   */
  private void executeBType(String[] parts) {

    int rs1 = getRegisterIndex(parts, 1);
    int rs2 = getRegisterIndex(parts, 2);
    int imm = getImmediateValue(parts, 3);
    imm = signExtendImmediate(imm, 12);
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

    if (LOG) {
      log.info(String.format("Executing: %s rs1=%d rs2=%d imm=%d -> PC=%d", parts[0], rs1, rs2, imm,
          programCounter));
    }
  }

  /**
   * Executes S-Type instructions which involve memory store operations.
   *
   * @param parts the instruction parts
   */
  private void executeSType(String[] parts) {

    int rs1 = getRegisterIndex(parts, 1);
    int rs2 = getRegisterIndex(parts, 2);
    int imm = getImmediateValue(parts, 3);
    imm = signExtendImmediate(imm, 12);
    int address = registers[rs1] + imm;
    if (address < 0) {
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

    if (LOG) {
      log.info(
          String.format("Executing: %s rs1=%d rs2=%d imm=%d -> address=%d, value=%d", parts[0], rs1,
              rs2, imm, address, registers[rs2]));
    }
  }

  /**
   * Executes I-Type immediate instructions which involve immediate values.
   *
   * @param parts the instruction parts
   */
  private void executeITypeImmediate(String[] parts) {

    int rd = getRegisterIndex(parts, 1);
    int rs1 = getRegisterIndex(parts, 2);
    int imm = getImmediateValue(parts, 3);
    imm = signExtendImmediate(imm, 12);
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

    if (LOG) {
      log.info(String.format("Executing: %s rs1=%d imm=%d -> rd=%d", parts[0], rs1, imm, rd));
    }
  }

  /**
   * Executes E-Type instructions which handle system calls and breaks.
   *
   * @param parts the instruction parts
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
   * Executes I-Type control and status register instructions.
   *
   * @param parts the instruction parts
   */
  private void executeITypeControlStatusRegister(String[] parts) {

    int rd = getRegisterIndex(parts, 1);
    int rs1 = getRegisterIndex(parts, 2);
    int csr = getImmediateValue(parts, 3);
    csr = signExtendImmediate(csr, 12);
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

    if (LOG) {
      log.info(String.format("Executing: %s rs1=%d csr=%d -> rd=%d", parts[0], rs1, csr, rd));
    }
  }

  /**
   * Retrieves the index of a register from the instruction parts.
   *
   * @param parts     the instruction parts
   * @param partIndex the index of the part to retrieve the register index from
   * @return the register index
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
   * Retrieves the immediate value from the instruction parts.
   *
   * @param parts     the instruction parts
   * @param partIndex the index of the part to retrieve the immediate value from
   * @return the immediate value
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
   * Sign-extends an immediate value to the specified bit width.
   *
   * @param immediate the immediate value to be sign-extended
   * @param bitWidth  the bit width to extend to
   * @return the sign-extended immediate value
   */
  public static int signExtendImmediate(int immediate, int bitWidth) {

    int mask = 1 << (bitWidth - 1);
    immediate =
        immediate & ((1 << bitWidth) - 1); // Ensure the immediate is at most 'bitWidth' bits

    // If the most significant bit is set, the value is negative
    if ((immediate & mask) != 0) {
      immediate = immediate | -(1 << bitWidth);
    }

    return immediate;
  }

  /**
   * Handles the "ecall" instruction by transferring control to the exception handler.
   */
  private static void handleEcall() {
    // TODO

    if (LOG) {
      log.info("ECALL: Transferred control to exception handler for syscall");
    }
  }

  /**
   * Handles the "ebreak" instruction by terminating the program via syscall exit.
   */
  private static void handleEbreak() {
    // TODO
    throw new RuntimeException("Program has terminated via syscall exit.");
  }

}
