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

    if (!file.exists()) {
      throw new FileNotFoundException(String.format("File %s not found.", file.getName()));
    }

    String fileName = file.getName();
    if (!fileName.endsWith(".bin")) {
      throw new IllegalArgumentException("File must have .bin extension.");
    }

    long fileSize = file.length();
    if (fileSize % 4 != 0) {
      throw new IOException(
          "The file size is not a multiple of 4 bytes, so it cannot be read as 32-bit integers.");
    }

    int[] programBin = new int[(int) (fileSize / 4)];

    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[4]; // Buffer to hold 4 bytes at a time
      for (int i = 0; i < programBin.length; i++) {
        if (fis.read(buffer) != 4) {
          throw new IOException("Error reading the binary file.");
        }
        programBin[i] =
            ((buffer[3] & 0xFF) << 24) | ((buffer[2] & 0xFF) << 16) | ((buffer[1] & 0xFF) << 8) | (
                buffer[0] & 0xFF);
      }
    }

    return programBin;
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

    String fileName = file.getName();
    if (!fileName.endsWith(".txt")) {
      throw new IllegalArgumentException("File must have .txt extension.");
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
      long longValue = Long.parseLong(lines[i], 2);
      programBin[i] = (int) longValue;
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
