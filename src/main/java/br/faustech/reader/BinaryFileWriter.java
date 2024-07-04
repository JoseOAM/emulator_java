package br.faustech.reader;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BinaryFileWriter {

  public static void main(String[] args) {

    String[] array = {"0b00000001000000000000000000110111", //lui 4096 mem
        "0b00000000000100101100000010110111", //lui 0 x
        "0b00000000000110010000000100110111", //lui 0 y
        "0b00000000000011111111000110110111", //lui 255 r
        "0b00000000000011111111001000110111", //lui 255 g
        "0b00000000000011111111001010110111", //lui 255 b
        "0b00000000000000000000001100110111", //lui 255 a
        "0b00000000000000000000001110110111", //lui 0 u
        "0b00000000000000000000010000110111", //lui 0 v
        "0b00000000000100000010000000100011", //sw 0 x
        "0b00000000001000000010000010100011", //sw 0 y
        "0b00000000001100000010000100100011", //sw 255 r
        "0b00000000010000000010000110100011", //sw 255 g
        "0b00000000010100000010001000100011", //sw 255 b
        "0b00000000011000000010001010100011", //sw 0 a
        "0b00000000011100000010001100100011", //sw 0 u
        "0b00000000100000000010001110100011", //sw 0 v
        "0b00000000000100000000000001110011"

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
