package br.faustech.reader;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryFileWriter {

  public static void main(String[] args) {

    String[] array = {"00000001000000000001000000110111", //lui 4097 mem
        "00000000000100101100000010110111", //lui 300 x
        "00000000000110010000000100110111", //lui 400 y
        "01111111000000001001000110110111", // LUI x3, 0xFF012
        "00100010000100011000000110010011", // ADDI x3, x3, 0xA21
        "00000000000100000010000000100011", //sw x
        "00000000001000000010000010100011", //sw y
        "00000000001100000010000100100011", //sw rgbaa

        "00000001000000000000000000110111", //lui 4096 mem
        "00000000000100000010000000100011", //sw swap frame buffer
        "00000000000100000000000001110011",

    };  // Exemplo de array de inteiros
    String fileName = "output.txt"; // Nome do arquivo de saída

    writeIntArrayToBinaryFile(array, fileName);

    System.out.println("Array escrito no arquivo binário " + fileName);
  }

  public static void writeIntArrayToBinaryFile(String[] array, String fileName) {

    try (FileOutputStream fos = new FileOutputStream(
        fileName); DataOutputStream dos = new DataOutputStream(fos)) {

      for (String i : array) {
        dos.writeBytes(i + "\n");
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
