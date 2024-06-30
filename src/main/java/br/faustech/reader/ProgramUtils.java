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
   * @param filename The name of the file to read.
   * @return The content of the file as a String.
   * @throws FileNotFoundException    If the file is not found.
   * @throws IllegalArgumentException If the file does not have a .bin extension.
   */
  public String readFile(String filename) throws FileNotFoundException {

    File file = new File(filename);

    // Check if the file extension is .bin
    if (!filename.toLowerCase().endsWith(".bin")) {
      throw new IllegalArgumentException("Invalid file type. The file must be a .bin file.");
    }

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
