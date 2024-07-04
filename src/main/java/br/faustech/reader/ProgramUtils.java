package br.faustech.reader;

import br.faustech.bus.Bus;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ProgramUtils {

  private final Bus bus;

  public ProgramUtils(Bus bus) {

    this.bus = bus;
  }

  /**
   * Reads the contents of a file if it has a .bin extension.
   *
   * @param file The file to read.
   * @return The content of the file as a String.
   * @throws FileNotFoundException    If the file is not found.
   * @throws IllegalArgumentException If the file does not have a .bin extension.
   */
  public String readFile(File file) throws FileNotFoundException {

    StringBuilder content = new StringBuilder();

    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        content.append(scanner.nextLine()).append("\n");
      }
    }

    return content.toString();
  }

  public void writeProgramInMemory(String program) {

    String[] lines = program.split("\n");
    int[] programBin = new int[lines.length];
    for (int i = 0; i < lines.length; i++) {
      programBin[i] = Integer.parseInt(lines[i].substring(2), 2);
    }
    bus.write(0, programBin);
  }

}
