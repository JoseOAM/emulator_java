package br.faustech;

import br.faustech.gpu.GPU;
import br.faustech.gpu.VideoFrameToByteArray;
import br.faustech.gpu.VideoMemory;
import java.util.Scanner;

public class Main {

  public static void main(String[] args) {

    VideoMemory videoMemory = new VideoMemory();

    Scanner scan = new Scanner(System.in);
    String videoPath = scan.nextLine();
    scan.close();

    VideoFrameToByteArray videoProcessor = new VideoFrameToByteArray(videoMemory, videoPath);

    Thread thread = new Thread(videoProcessor);
    thread.start();

    GPU gpu = new GPU(800, 600, videoMemory);
    while (gpu.isRunning()) {
      gpu.render();
    }
    gpu.cleanUp();

    // Stop the video processor thread
    if (thread.getState() != Thread.State.TERMINATED) {
      thread.interrupt();
    }
  }

}