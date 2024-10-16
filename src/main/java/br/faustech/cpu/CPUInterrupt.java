package br.faustech.cpu;

import br.faustech.Main;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an abstract CPU interrupt handler that extends the Thread class.
 * This class manages interrupt states, handles interrupt data, and monitors key presses.
 */
public abstract class CPUInterrupt extends Thread {

    /**
     * Indicates whether interrupts are currently enabled.
     */
    @Getter
    protected static boolean isInterruptEnabled = false;

    /**
     * Stores data related to the current interrupt.
     */
    @Setter
    protected static int interruptData;

    /**
     * Flag to indicate if a key has been pressed.
     */
    @Setter
    protected static boolean keyPressedFlag = false;

    /**
     * Records the start time of the interrupt monitoring.
     */
    @Getter
    protected static long startTime = 0;

    /**
     * Checks if an interrupt condition has been met based on elapsed time or key press.
     *
     * @return 1 if the elapsed time has exceeded the clock speed, 2 if a key has been pressed, or 0 otherwise.
     */
    public static int checkInterruption() {
        long elapsedTime = System.currentTimeMillis() - startTime;

        if (elapsedTime >= Main.getClockSpeed()) {
            isInterruptEnabled = false;
            setStartTime();
            return 1;
        } else if (keyPressedFlag) {
            isInterruptEnabled = false;
            keyPressedFlag = false;
            return interruptData + 2;
        }
        return 0;
    }

    /**
     * Sets the start time to the current system time in milliseconds.
     */
    public static void setStartTime() {
        startTime = System.currentTimeMillis();
    }
}
