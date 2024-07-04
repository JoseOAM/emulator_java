package br.faustech.reader;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryFileWriter {

  public static void main(String[] args) {

    String[] array = {"0b00000001000000000001000000110111", //lui 4097 mem
        "0b00000000000100101100000010110111", //lui 300 x
        "0b00000000000110010000000100110111", //lui 400 y
        "0b01111111000000001001000110110111", // LUI x3, 0xFF012
        "0b00100010000100011000000110010011", // ADDI x3, x3, 0xA21
        "0b00000000000100000010000000100011", //sw x
        "0b00000000001000000010000010100011", //sw y
        "0b00000000001100000010000100100011", //sw rgbaa

        "0b00000001000000000000000000110111", //lui 4096 mem
        "0b00000000000100000010000000100011", //sw swap frame buffer
        "0b00000000000100000000000001110011",

    };  // Exemplo de array de inteiros
    String fileName = "output.bin"; // Nome do arquivo de saída

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
