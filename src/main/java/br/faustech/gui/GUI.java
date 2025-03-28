package br.faustech.gui;

import br.faustech.comum.ArgsListener;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

public class GUI extends JFrame {
    private final JTextField[] registerField = new JTextField[32];
    private final JTextField[] memoryField = new JTextField[32];
    private final ArgsListener listener;
    private final List<String> recentFiles = new ArrayList<>();
    private static final int MAX_RECENT_FILES = 5;
    private final JMenu recentFilesMenu = new JMenu("Recent Files");
    private RegisterUpdater updater;

    public GUI(ArgsListener listener) {
        super("RISC-V Emulator");
        this.listener = listener;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setResizable(false);

        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.NORTH);

        setJMenuBar(createMenuBar());
        setLayout(new GridLayout(32, 3, 5, 5));

        for(int i = 0; i < 32; i++) {
            add(new JLabel("Reg " + i + ":"));

            registerField[i] = new JTextField("0x0000");
            registerField[i].setEditable(false);
            add(registerField[i]);

            memoryField[i] = new JTextField("0x0000");
            memoryField[i].setEditable(false);
            add(memoryField[i]);
        }

        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu optionsMenu = new JMenu("Options");

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openFile());

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> openSettingsWindow());

        optionsMenu.add(settingsItem);
        fileMenu.add(openItem);
        fileMenu.add(recentFilesMenu);

        menuBar.add(fileMenu);
        menuBar.add(optionsMenu);

        return menuBar;
    }

    private void openSettingsWindow() {
        SettingsWindow settingsWindow = new SettingsWindow(this);
        settingsWindow.setVisible(true);
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a Program File");

        fileChooser.setCurrentDirectory(new File("src/test/demos"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            addToRecentFiles(path);
            if (listener != null) {
                try {
                    listener.onArgsSelected(path);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else {
                throw new IllegalArgumentException("Unknown error.");
            }
        }
    }

    private void addToRecentFiles(String filePath) {
        if (!recentFiles.contains(filePath)) {
            if (recentFiles.size() >= MAX_RECENT_FILES) {
                recentFiles.removeLast();
            }
            recentFiles.addFirst(filePath);
        }
        updateRecentFilesMenu();
    }

    private void updateRecentFilesMenu() {
        recentFilesMenu.removeAll(); // Clear the existing menu items

        for (String file : recentFiles) {
            JMenuItem menuItem = new JMenuItem(file);
            menuItem.addActionListener(e -> {
                openFileFromHistory(file);
            });
            recentFilesMenu.add(menuItem);
        }
    }

    private void openFileFromHistory(String filePath) {
        if (listener != null) {
            try {
                listener.onArgsSelected(filePath);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            throw new IllegalArgumentException("Unknown error.");
        }
    }

    public void updateRegister(int[] registers) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 32; i++) {
                String newRegisterValue = "0x" + Integer.toHexString(registers[i]);
                String newMemoryValue = "0x" + Integer.toHexString(registers[i]);

                if (!registerField[i].getText().equals(newRegisterValue)) {
                    registerField[i].setText(newRegisterValue);
                }
                if (!memoryField[i].getText().equals(newMemoryValue)) {
                    memoryField[i].setText(newMemoryValue);
                }
            }
        });
    }

    public void setRegisterUpdater(int[] registers) {
        RegisterUpdater updater = new RegisterUpdater(registers, registerField, memoryField);
        updater.execute();;
        this.updater = updater;
    }
}

class SettingsWindow extends JDialog {
    public SettingsWindow(GUI parent) {
        super(parent, "Settings", true);

        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());

        NumberFormat amountFormat = NumberFormat.getIntegerInstance();
        amountFormat.setGroupingUsed(false);

        JFormattedTextField widthField = new JFormattedTextField(amountFormat);
        widthField.setValue(320);
        widthField.setEditable(false);
        JFormattedTextField heightField = new JFormattedTextField(amountFormat);
        heightField.setValue(240);
        heightField.setEditable(false);
        JFormattedTextField memoryField = new JFormattedTextField(amountFormat);
        memoryField.setValue(4194304);
        memoryField.setEditable(false);

        widthField.setColumns(10);
        heightField.setColumns(10);
        memoryField.setColumns(10);

        JLabel widthLabel = new JLabel("Width:");
        JLabel heightLabel = new JLabel("Height:");
        JLabel memoryLabel = new JLabel("Memory:");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        content.add(widthLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        content.add(widthField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        content.add(heightLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        content.add(heightField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        content.add(memoryLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        content.add(memoryField, gbc);

        add(content, BorderLayout.CENTER);
    }
}