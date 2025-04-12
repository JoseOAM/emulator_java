package br.faustech.gui;

import javax.swing.*;
import java.util.List;

public class RegisterUpdater extends SwingWorker<Void, int[]> {
    private final int[] registers;
    private final JTextField[] memoryField;
    private volatile boolean running = true;

    public RegisterUpdater(int[] registers, JTextField[] memoryField) {
        this.registers = registers;
        this.memoryField = memoryField;
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (running && !isCancelled()) {
            publish(registers.clone());
            Thread.sleep(100);
        }
        return null;

    }

    @Override
    protected void process(List<int[]> chunks) {
        if (isCancelled() || memoryField == null) {
            return;
        }

        int[] latestRegisters = chunks.get(chunks.size() - 1);

        for (int i = 0; i < 32; i++) {
            if (memoryField[i] != null) {
                String newMemoryValue = "0x" + Integer.toHexString(latestRegisters[i]);
                if (!memoryField[i].getText().equals(newMemoryValue)) {
                    memoryField[i].setText(newMemoryValue);
                }
            }
        }
    }



    public void stopUpdater() {
        running = false;
        cancel(true);
    }
}


