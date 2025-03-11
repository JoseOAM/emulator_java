package br.faustech.gui;

import br.faustech.comum.ArgsListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;

public class GUI extends JFrame {
    private final JTextField pathField;
    private final JFormattedTextField widthField;
    private final JFormattedTextField heightField;
    private final JFormattedTextField memoryField;

    public GUI(ArgsListener listener) {
        super("RISC-V Emulator");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());

        NumberFormat amountFormat = NumberFormat.getIntegerInstance();
        amountFormat.setGroupingUsed(false);

        pathField = new JTextField();
        pathField.setText("");
        widthField = new JFormattedTextField(amountFormat);
        widthField.setValue(320);
        heightField = new JFormattedTextField(amountFormat);
        heightField.setValue(240);
        memoryField = new JFormattedTextField(amountFormat);
        memoryField.setValue(4194304);

        pathField.setColumns(30);
        widthField.setColumns(10);
        heightField.setColumns(10);
        memoryField.setColumns(10);

        JLabel pathLabel = new JLabel("Path:");
        JLabel widthLabel = new JLabel("Width:");
        JLabel heightLabel = new JLabel("Height:");
        JLabel memoryLabel = new JLabel("Memory:");

        JButton button = new JButton("Start");

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] args = new String[4];
                args[0] = pathField.getText();
                args[1] = widthField.getText();
                args[2] = heightField.getText();
                args[3] = memoryField.getText();
                if (listener != null && args[0] != null) {
                    try {
                        listener.onArgsSelected(args);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                else {
                    throw new IllegalArgumentException("Program file name not provided.");
                }
            }
        });

        gbc.gridx = 0; gbc.gridy = 0;
        content.add(pathLabel, gbc);
        gbc.gridx = 1;
        content.add(pathField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        content.add(widthLabel, gbc);
        gbc.gridx = 1;
        content.add(widthField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        content.add(heightLabel, gbc);
        gbc.gridx = 1;
        content.add(heightField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        content.add(memoryLabel, gbc);
        gbc.gridx = 1;
        content.add(memoryField, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        content.add(button, gbc);

        add(content, BorderLayout.CENTER);
        setVisible(true);
    }
}