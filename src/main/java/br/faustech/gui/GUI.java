package br.faustech.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {
    private final JPanel content;
    private final JTextField pathField;
    private final JButton button;

    public GUI() {
        super("RISC-V Emulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        content = new JPanel();
        content.setLayout(new FlowLayout());

        pathField = new JTextField(30);

        button = new JButton("Start");

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = pathField.getText();
                JOptionPane.showMessageDialog(null, "Path = " + path);
            }
        });

        content.add(pathField);
        content.add(button);
        add(content);

        setVisible(true);
    }
}