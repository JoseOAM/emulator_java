package br.faustech.cpu;
import lombok.Getter;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

public class CPUInterrupt extends JFrame implements KeyListener {
    @Getter
    private static long startTime = 0;
    private static long endTime = 0;
    private static long elapsedTime = 0;
    private static boolean keyPressedFlag = false;
    public static int interruptFlag = 0;
    public CPUInterrupt() {
        this.addKeyListener(this);
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.setVisible(true);    // Para que a janela apareça
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Para fechar o programa ao fechar a janela
    }

    public static int checkInterruption() {
        if(elapsedTime >= 1000000000){
            System.out.println("Timer de 1 segundo.");
            elapsedTime = 0;
            return  1;
        }
        else if (keyPressedFlag) {

            System.out.println("Tecla pressionada!");
            keyPressedFlag = false; // Reseta o flag após detectar a tecla pressionada
            return 2;
        }
        return 0;
    }

    public static void setStartTime() {
        startTime = System.nanoTime();
    }
    public static void setEndTime() {
        endTime = System.nanoTime();
        // Calculate the elapsed time in nanoseconds
        elapsedTime = elapsedTime + (endTime - startTime);
    }


    @Override
    public void keyPressed(KeyEvent e) {
        keyPressedFlag = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Você pode deixar isso vazio ou lidar com eventos de keyTyped se necessário
    }


}
