package br.faustech.cpu;

import br.faustech.Main;
import lombok.Getter;
import lombok.Setter;

public abstract class CPUInterrupt extends Thread {

    @Getter
    protected static boolean isInterruptEnabled = false;

    @Setter
    protected static int interruptData;

    @Setter
    protected static boolean keyPressedFlag = false;

    @Getter
    protected static long startTime = 0;

    public static int checkInterruption() {
        long elapsedTime = System.currentTimeMillis() - startTime;

        if (elapsedTime >= Main.getClockSpeed()) {
            isInterruptEnabled = false;
            setStartTime();
            Main.info("Timer shot at: " + elapsedTime + " miliseconds");
            return 1;
        } else if (keyPressedFlag) {
            isInterruptEnabled = false;
            keyPressedFlag = false;
            Main.info("Key pressed: " + interruptData);
            return interruptData;
        }
        return 0;
    }

    public static void setStartTime() {
        startTime = System.currentTimeMillis();
    }
}

