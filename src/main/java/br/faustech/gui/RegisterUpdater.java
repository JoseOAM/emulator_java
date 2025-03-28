package br.faustech.gui;

import javax.swing.*;
import java.util.List;

public class RegisterUpdater extends SwingWorker<Void, int[]> {
    private final int[] registers;
    private final JTextField[] registerField;
    private final JTextField[] memoryField;

    public RegisterUpdater(int[] registers, JTextField[] registerField, JTextField[] memoryField) {
        this.registers = registers;
        this.registerField = registerField;
        this.memoryField = memoryField;
    }

    @Override
    protected Void doInBackground() throws Exception {
        while (!isCancelled()) {
            publish(registers.clone());
            Thread.sleep(100);
        }
        return null;
    }

    @Override
    protected void process(List<int[]> chunks) {
        int[] latestRegisters = chunks.getLast();

        for (int i = 0; i < 32; i++) {
            String newRegisterValue = "0x" + Integer.toHexString(latestRegisters[i]);
            String newMemoryValue = "0x" + Integer.toHexString(latestRegisters[i]);

            if (!registerField[i].getText().equals(newRegisterValue)) {
                registerField[i].setText(newRegisterValue);
            }
            if (!memoryField[i].getText().equals(newMemoryValue)) {
                memoryField[i].setText(newMemoryValue);
            }
        }
    }
}


