package br.faustech.cpu;

import lombok.Getter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

public class CPUInterrupt {
    @Getter
    private static long startTime = 0;
    private static boolean keyPressedFlag = false;

    public static int checkInterruption() {
        long currentTime = System.currentTimeMillis();  // Captura o tempo atual
        long elapsedTime = currentTime - startTime;  // Calcula o tempo decorrido

        if (elapsedTime >= 10) {  // Verifica se já passou 30 segundos
            System.out.println("Timer shot at: " + elapsedTime + " miliseconds");
            setStartTime();  // Reinicia o tempo inicial para a próxima medição
            return 1;
        }
        else if (keyPressedFlag) {
            //System.out.println("Tecla pressionada!");
            keyPressedFlag = false; // Reseta o flag após detectar a tecla pressionada
            return 2;
        }
        return 0;
    }

    public static void setStartTime() {
        startTime = System.currentTimeMillis();  // Captura o tempo inicial
    }
}

