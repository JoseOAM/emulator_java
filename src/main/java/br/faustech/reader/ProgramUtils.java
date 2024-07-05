package br.faustech.reader;

import br.faustech.bus.Bus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Utility class for reading program files (.bin and .txt) and writing programs into memory.
 */
public class ProgramUtils {

  private final Bus bus; // Bus instance used for writing programs into memory

  /**
   * Constructs a ProgramUtils instance with a specified Bus instance.
   *
   * @param bus The Bus instance to use for memory operations.
   */
  public ProgramUtils(Bus bus) {

    this.bus = bus;
  }

  /**
   * Reads the contents of a file and determines its type (.bin or .txt) to process accordingly.
   *
   * @param file The file to read.
   * @return An array of integers representing the program instructions.
   * @throws IOException              If an error occurs while reading the file.
   * @throws IllegalArgumentException If the file extension is neither .bin nor .txt.
   */
  public int[] readFile(File file) throws IOException {

    String fileName = file.getName();
    if (fileName.endsWith(".bin")) {
      return readBinaryFile(file);
    } else if (fileName.endsWith(".txt")) {
      return readTxtFile(file);
    } else {
      throw new IllegalArgumentException("File must have .bin or .txt extension.");
    }
  }

  /**
   * Reads the contents of a binary file (.bin) and converts them into machine instructions.
   *
   * @param file The .bin file to read.
   * @return An array of integers representing the machine instructions.
   * @throws IOException              If an error occurs while reading the file.
   * @throws IllegalArgumentException If the file does not have a .bin extension.
   */
  public int[] readBinaryFile(File file) throws IOException {

    String fileName = file.getName();
    if (!fileName.endsWith(".bin")) {
      throw new IllegalArgumentException("File must have .bin extension.");
    }

    try (FileInputStream fis = new FileInputStream(file)) {
      long fileSize = file.length();
      byte[] buffer = new byte[(int) fileSize];
      fis.read(buffer);

      int[] programBin = new int[buffer.length / 4]; // Each integer is 4 bytes
      int index = 0;

      for (int i = 0; i < programBin.length; i++) {
        int value = ((buffer[index++] & 0xFF) << 24) |
            ((buffer[index++] & 0xFF) << 16) |
            ((buffer[index++] & 0xFF) << 8) |
            (buffer[index++] & 0xFF);
        programBin[i] = value;
      }

      return programBin;
    }
  }

  /**
   * Reads the contents of a text file (.txt) and converts them into machine instructions.
   *
   * @param file The .txt file to read.
   * @return An array of integers representing the machine instructions.
   * @throws FileNotFoundException    If the file is not found.
   * @throws IllegalArgumentException If the file does not have a .txt extension.
   */
  public int[] readTxtFile(File file) throws FileNotFoundException {

    if (!file.exists()) {
      throw new FileNotFoundException(String.format("File %s not found.", file.getName()));
    }

    StringBuilder content = new StringBuilder();
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine()).append("\n");
      }
    }

    String program = content.toString();
    String[] lines = program.split("\n");
    int[] programBin = new int[lines.length];

    for (int i = 0; i < lines.length; i++) {
      String firstBit = lines[i].substring(0, 1);
      String line = lines[i].substring(1);
      programBin[i] = Integer.parseInt((firstBit.equals("0")) ? line : "-" + line, 2);
    }

    return programBin;
  }

  /**
   * Writes the program instructions into memory starting from address 0.
   *
   * @param programBin The array of integers representing program instructions.
   */
  public void writeProgramInMemory(int[] programBin) {

    bus.write(0, programBin); // Write programBin data to memory starting at address 0
  }

}
