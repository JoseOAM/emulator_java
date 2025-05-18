package br.faustech.gui;

import javax.swing.*;
import java.util.List;

public class RegisterUpdater extends SwingWorker<Void, int[]> {
    private final int[] registerValues;
    private final JTextField[] registers;
    private volatile boolean running = true;

    public RegisterUpdater(int[] registersValues, JTextField[] registerField) {
        this.registerValues = registersValues;
        this.registers = registerField;
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (running && !isCancelled()) {
            publish(registerValues.clone());
            Thread.sleep(100);
        }
        return null;

    }

    @Override
    protected void process(List<int[]> chunks) {
        if (isCancelled() || registers == null) {
            return;
        }

        int[] latestRegisters = chunks.get(chunks.size() - 1);

        for (int i = 0; i < 32; i++) {
            if (registers[i] != null) {
                String newMemoryValue = "0x" + String.format("%08X", latestRegisters[i]);
                if (!registers[i].getText().equals(newMemoryValue)) {
                    registers[i].setText(newMemoryValue);
                }
            }
        }
    }

    public int getRegister(int pos) {
        return registerValues[pos];
    }

    public void stopUpdater() {
        running = false;
        cancel(true);
    }
}


