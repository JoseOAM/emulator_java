package br.faustech.gui;

import br.faustech.comum.ArgsListener;
import br.faustech.comum.ConfigFile;
import br.faustech.cpu.CPU;
import br.faustech.cpu.Decoder;
import br.faustech.gpu.GPU;
import br.faustech.reader.ProgramUtils;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class GUI extends JFrame {
    private int[] programInstructions;
    private final JTextField[] memoryField = new JTextField[32];
    private final JTabbedPane centerTabbedPane;
    private final JTabbedPane lowerTabbedPane;
    private final JPanel contentPanel;
    private JTextArea programHexadecimalArea;
    private JTextArea programBinaryArea;
    private DefaultTableModel model;
    private final JSplitPane centerSplitPane;
    private final JSplitPane completeSplitPane;
    private final ArgsListener listener;
    private final List<String> recentFiles = new ArrayList<>();
    private AtomicBoolean darkModeEnabled = new AtomicBoolean();
    private final JMenu recentFilesMenu = new JMenu("Recent Files");
    JTextArea consoleTextArea = new JTextArea();
    private final ConfigFile configFile;
    private static final int MAX_RECENT_FILES = 5;
    private RegisterUpdater updater;
    private GPU gpu;
    private CPU cpu;
    private ProgramUtils programUtils;
    private String path;
    private final JButton runButton = new JButton("Run");
    private final JButton stopButton = new JButton("Stop");
    private final AtomicBoolean running = new AtomicBoolean(false);

    public GUI(ArgsListener listener, ConfigFile configFile, ProgramUtils programUtils) {
        super("RISC-V Emulator");
        this.listener = listener;
        this.configFile = configFile;
        this.programUtils = programUtils;

        configFile.loadHistory(recentFiles, darkModeEnabled);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                configFile.saveHistory(recentFiles, darkModeEnabled);

                System.exit(0);
            }
        });

        setSize(1000, 800);
        setLocationRelativeTo(null);
        setResizable(false);

        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.NORTH);

        setJMenuBar(createMenuBar());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(createActionBar(), BorderLayout.NORTH);

        centerTabbedPane = new JTabbedPane();
        contentPanel = new JPanel(new BorderLayout());
        lowerTabbedPane = new JTabbedPane();

        centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerTabbedPane, contentPanel);
        centerSplitPane.setResizeWeight(0);
        centerSplitPane.setEnabled(false);
        centerSplitPane.setDividerSize(0);

        completeSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerSplitPane, lowerTabbedPane);
        completeSplitPane.setResizeWeight(1);
        completeSplitPane.setEnabled(false);
        completeSplitPane.setDividerSize(0);

        add(completeSplitPane, BorderLayout.CENTER);

        if (darkModeEnabled.get()) {
            setColorsRecursively(getContentPane(), Color.DARK_GRAY, Color.WHITE);
            UIManager.put("Panel.background", Color.DARK_GRAY);
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("TextField.background", new Color(50, 50, 50));
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("ScrollPane.background", Color.DARK_GRAY);
        } else {
            setColorsRecursively(getContentPane(), Color.WHITE, Color.DARK_GRAY);
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("Label.foreground", Color.DARK_GRAY);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.DARK_GRAY);
            UIManager.put("ScrollPane.background", Color.WHITE);
        }

        setVisible(true);
    }

    void setColorsRecursively(Component component, Color bg, Color fg) {
        component.setBackground(bg);
        component.setForeground(fg);

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setColorsRecursively(child, bg, fg);
            }
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu optionsMenu = new JMenu("Options");
        JMenu exportMenu = new JMenu("Export");

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openFile());

        fileMenu.add(openItem);
        fileMenu.add(recentFilesMenu);

        menuBar.add(fileMenu);

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> openSettingsWindow());

        JMenuItem debugTabItem = new JMenuItem("Open Debug Tab");
        debugTabItem.addActionListener(e -> openDebugTab());

        JMenuItem consoleTabItem = new JMenuItem("Open Console Tab");
        consoleTabItem.addActionListener(e -> openConsoleTab());

        optionsMenu.add(settingsItem);
        optionsMenu.add(debugTabItem);
        optionsMenu.add(consoleTabItem);

        updateRecentFilesMenu();

        menuBar.add(optionsMenu);

        JMenuItem hexadecimalItem = new JMenuItem("Hexadecimal Code");
        hexadecimalItem.addActionListener(e -> exportHexadecimalCode());

        JMenuItem binaryItem = new JMenuItem("Binary Code");
        binaryItem.addActionListener(e -> exportBinaryCode());

        JMenuItem completeItem = new JMenuItem("Complete Code");
        completeItem.addActionListener(e -> exportCompleteCode());

        exportMenu.add(hexadecimalItem);
        exportMenu.add(binaryItem);
        exportMenu.add(completeItem);

        updateRecentFilesMenu();

        menuBar.add(exportMenu);

        return menuBar;
    }

    private JToolBar createActionBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        runButton.addActionListener(e -> startEmulator());
        stopButton.addActionListener(e -> stopEmulator());

        runButton.setEnabled(false);
        stopButton.setEnabled(false);

        toolBar.add(runButton);
        toolBar.add(stopButton);

        return toolBar;
    }

    private void openDebugTab() {
        String tabTitle = "Debug";

        for (int i = 0; i < centerTabbedPane.getTabCount(); i++) {
            if (centerTabbedPane.getTitleAt(i).equals(tabTitle)) {
                centerTabbedPane.removeTabAt(i);
                centerSplitPane.setDividerLocation(0);
                return;
            }
        }

        JPanel debugPanel = new JPanel(new BorderLayout());
        debugPanel.add(new JLabel(""), BorderLayout.NORTH);

        JPanel registerMemoryPanel = new JPanel(new GridLayout(32, 2, 5, 5));

        for (int i = 0; i < 32; i++) {
            memoryField[i] = new JTextField("0x00000000");
            memoryField[i].setEditable(false);

            registerMemoryPanel.add(new JLabel("Reg " + i + ":"));
            registerMemoryPanel.add(memoryField[i]);
        }

        JScrollPane scrollPane = new JScrollPane(registerMemoryPanel);
        debugPanel.add(scrollPane, BorderLayout.CENTER);


        int index = centerTabbedPane.getTabCount();
        centerTabbedPane.addTab(tabTitle, debugPanel);

        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(tabTitle + "  ");
        JButton closeButton = new JButton("X");
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setMargin(new Insets(0, 0, 0, 0));

        closeButton.addActionListener(e -> {
            centerTabbedPane.remove(debugPanel);
            centerSplitPane.setDividerLocation(0);
        });

        tabHeader.add(titleLabel);
        tabHeader.add(closeButton);

        centerTabbedPane.setTabComponentAt(index, tabHeader);

        centerTabbedPane.setSelectedComponent(debugPanel);
        centerSplitPane.setDividerLocation(0.2);
    }

    private void openConsoleTab() {
        String tabTitle = "Console";
        for (int i = 0; i < lowerTabbedPane.getTabCount(); i++) {
            if (lowerTabbedPane.getTitleAt(i).equals(tabTitle)) {
                lowerTabbedPane.removeTabAt(i);
                completeSplitPane.setDividerLocation(1000);
                return;
            }
        }

        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(new JLabel(""), BorderLayout.NORTH);

        consoleTextArea.setEditable(false);
        consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        consolePanel.add(scrollPane, BorderLayout.CENTER);


        int index = lowerTabbedPane.getTabCount();
        lowerTabbedPane.addTab(tabTitle, consolePanel);

        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(tabTitle + "  ");

        JButton closeButton = new JButton("X");
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setMargin(new Insets(0, 0, 0, 0));

        closeButton.addActionListener(e -> {
            lowerTabbedPane.remove(consolePanel);
            completeSplitPane.setDividerLocation(1000);
        });

        JButton clearButton = new JButton("Clear");
        clearButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        clearButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusable(false);
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.setMargin(new Insets(0, 0, 0, 0));

        clearButton.addActionListener(e -> {
            consoleTextArea.setText("");
        });

        tabHeader.add(titleLabel);
        tabHeader.add(Box.createHorizontalStrut(10));
        tabHeader.add(closeButton);
        tabHeader.add(Box.createHorizontalStrut(10));
        tabHeader.add(clearButton);

        lowerTabbedPane.setTabComponentAt(index, tabHeader);

        lowerTabbedPane.setSelectedComponent(consolePanel);
        completeSplitPane.setDividerLocation(0.8);

        if (darkModeEnabled.get()) {
            setColorsRecursively(getContentPane(), Color.DARK_GRAY, Color.WHITE);
            UIManager.put("Panel.background", Color.DARK_GRAY);
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("TextField.background", new Color(50, 50, 50));
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("ScrollPane.background", Color.DARK_GRAY);
        } else {
            setColorsRecursively(getContentPane(), Color.WHITE, Color.DARK_GRAY);
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("Label.foreground", Color.DARK_GRAY);
            UIManager.put("TextField.background", Color.WHITE);
            UIManager.put("TextField.foreground", Color.DARK_GRAY);
            UIManager.put("ScrollPane.background", Color.WHITE);
        }
    }

    private void exportHexadecimalCode() {
        if (model != null) {
            StringBuilder sb = new StringBuilder();
            for (int row = 0; row < model.getRowCount(); row++) {
                Object value = model.getValueAt(row, 1);
                if (value != null) {
                    sb.append(value.toString()).append("\n");
                }
            }

            StringSelection selection = new StringSelection(sb.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    private void exportBinaryCode() {
        if (model != null) {
            StringBuilder sb = new StringBuilder();
            for (int row = 0; row < model.getRowCount(); row++) {
                Object value = model.getValueAt(row, 2);
                if (value != null) {
                    sb.append(value.toString()).append("\n");
                }
            }

            StringSelection selection = new StringSelection(sb.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    private void exportCompleteCode() {
        if (model != null) {
            StringBuilder sb = new StringBuilder();
            for (int row = 0; row < model.getRowCount(); row++) {
                Object value = model.getValueAt(row, 3);
                if (value != null) {
                    sb.append(value.toString()).append("\n");
                }
            }

            StringSelection selection = new StringSelection(sb.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    public void consoleInfo(String message) {
        consoleTextArea.append(message + "\n");
        String[] lines = consoleTextArea.getText().split("\n");
        if (lines.length > 30) {
            StringBuilder trimmed = new StringBuilder();
            for (int i = lines.length - 30; i < lines.length; i++) {
                trimmed.append(lines[i]).append("\n");
            }
            consoleTextArea.setText(trimmed.toString());
        }
        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
    }


    private void openSettingsWindow() {
        SettingsWindow settingsWindow = new SettingsWindow(this);
        settingsWindow.setVisible(true);
    }

    private void openFileContentTab(File file) {
        try {
            programInstructions = programUtils.readFile(file);

            String[] columnNames = {"Line", "Hexadecimal", "Binary", "Code"};

            Object[][] tableData = new Object[programInstructions.length][4];
            for (int i = 0; i < programInstructions.length; i++) {
                String hex = String.format("0x%08X", programInstructions[i]);
                String binary = String.format("%32s", Integer.toBinaryString(programInstructions[i])).replace(' ', '0');
                String decodedInstruction = Decoder.decodeInstructionInFormat(programInstructions[i]);
                tableData[i][0] = "Line " + i;
                tableData[i][1] = hex;
                tableData[i][2] = binary;
                tableData[i][3] = decodedInstruction;
            }

            model = new DefaultTableModel(tableData, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            JTable contentTable = new JTable(model);
            contentTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            contentTable.setRowHeight(20);
            contentTable.setCellSelectionEnabled(true);

            TableColumnModel columnModel = contentTable.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(70);
            columnModel.getColumn(1).setPreferredWidth(120);
            columnModel.getColumn(2).setPreferredWidth(270);
            columnModel.getColumn(3).setPreferredWidth(250);

            JScrollPane tableScrollPane = new JScrollPane(contentTable);
            contentPanel.removeAll();
            contentPanel.add(tableScrollPane, BorderLayout.CENTER);

//            programHexadecimalArea = new JTextArea();
//            programBinaryArea = new JTextArea();
//
//            programHexadecimalArea.setEditable(false);
//            programBinaryArea.setEditable(false);
//
//            programHexadecimalArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//            programBinaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
//
//            StringBuilder hexadecimalContent = new StringBuilder();
//            for (int i = 0; i < programInstructions.length; i++) {
//                hexadecimalContent.append("Line ").append(i).append(": 0x")
//                        .append(String.format("%08X", programInstructions[i]))
//                        .append("\n");
//            }
//            programHexadecimalArea.setText(hexadecimalContent.toString());
//
//            StringBuilder binaryContent = new StringBuilder();
//            for (int i = 0; i < programInstructions.length; i++) {
//                binaryContent.append("Line ").append(i).append(": ")
//                        .append(String.format("%32s", Integer.toBinaryString(programInstructions[i])).replace(' ', '0'))
//                        .append("\n");
//            }
//            programBinaryArea.setText(binaryContent.toString());
//
//            JScrollPane scrollHexadecimalPane = new JScrollPane(programHexadecimalArea);
//            JScrollPane scrollBinaryPane = new JScrollPane(programBinaryArea);
//
//            contentPanel.removeAll();
//
//            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
//                    scrollHexadecimalPane, scrollBinaryPane);
//            splitPane.setResizeWeight(0.5);
//            contentPanel.add(splitPane, BorderLayout.CENTER);

            if (darkModeEnabled.get()) {
                setColorsRecursively(getContentPane(), Color.DARK_GRAY, Color.WHITE);
                UIManager.put("Panel.background", Color.DARK_GRAY);
                UIManager.put("Label.foreground", Color.WHITE);
                UIManager.put("TextField.background", new Color(50, 50, 50));
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("ScrollPane.background", Color.DARK_GRAY);
            } else {
                setColorsRecursively(getContentPane(), Color.WHITE, Color.DARK_GRAY);
                UIManager.put("Panel.background", Color.WHITE);
                UIManager.put("Label.foreground", Color.DARK_GRAY);
                UIManager.put("TextField.background", Color.WHITE);
                UIManager.put("TextField.foreground", Color.DARK_GRAY);
                UIManager.put("ScrollPane.background", Color.WHITE);
            }

            contentPanel.revalidate();
            contentPanel.repaint();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

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
            if (this.path != null) {
                stopEmulator();
            }
            this.path = path;
            openFileContentTab(selectedFile);
            consoleInfo("Program selected: " + path);
            runButton.setEnabled(true);
        }
    }

    private void startEmulator() {
        if (listener != null && path != null && !path.isBlank()) {
            try {
                consoleInfo("Emulator started");
                consoleInfo("Path: " + path);
                listener.onArgsSelected(path);
                running.set(true);
                runButton.setEnabled(false);
                stopButton.setEnabled(true);

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void stopEmulator() {
        if (cpu != null && cpu.isAlive()) {
            cpu.interrupt();
        }
        if (gpu != null && gpu.isAlive()) {
            gpu.interrupt();
            gpu.setShouldClose(true);
        }

        if (updater != null) {
            updater.stopUpdater();
        }

        running.set(false);
        runButton.setEnabled(true);
        stopButton.setEnabled(false);
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
                if (this.path != null) {
                    stopEmulator();
                }
                consoleInfo("Program selected: " + file);
                this.path = file;
                File selectedFile = new File(path);
                openFileContentTab(selectedFile);
                runButton.setEnabled(true);
                stopButton.setEnabled(false);
            });
            recentFilesMenu.add(menuItem);
        }
    }

    public void setRegisterUpdater(int[] registers) {
        RegisterUpdater updater = new RegisterUpdater(registers, memoryField);
        this.updater = updater;
        updater.execute();
    }

    public RegisterUpdater getRegisterUpdater() {
        return updater;
    }

    public List<String> getRecentFiles() {
        return recentFiles;
    }

    public AtomicBoolean getDarkModeEnabled() {
        return darkModeEnabled;
    }

    public String getPath() {
        return path;
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public boolean getRunning() {
        return running.get();
    }

    public void setGPU(GPU gpu) {
        this.gpu = gpu;
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
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

        JCheckBox darkModeCheckbox = new JCheckBox();
        darkModeCheckbox.setSelected(parent.getDarkModeEnabled().get());

        darkModeCheckbox.addActionListener(e -> {
            boolean darkMode = darkModeCheckbox.isSelected();
            parent.getDarkModeEnabled().set(darkMode);
            Color bg;
            Color fg;

            if (darkMode) {
                parent.setColorsRecursively(getContentPane(), Color.DARK_GRAY, Color.WHITE);
                UIManager.put("Panel.background", Color.DARK_GRAY);
                UIManager.put("Label.foreground", Color.WHITE);
                UIManager.put("TextField.background", new Color(50, 50, 50));
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("ScrollPane.background", Color.DARK_GRAY);
                bg = Color.DARK_GRAY;
                fg = Color.WHITE;
            } else {
                parent.setColorsRecursively(getContentPane(), Color.WHITE, Color.DARK_GRAY);
                UIManager.put("Panel.background", Color.WHITE);
                UIManager.put("Label.foreground", Color.DARK_GRAY);
                UIManager.put("TextField.background", Color.WHITE);
                UIManager.put("TextField.foreground", Color.DARK_GRAY);
                UIManager.put("ScrollPane.background", Color.WHITE);
                bg = Color.WHITE;
                fg = Color.DARK_GRAY;
            }

            SwingUtilities.invokeLater(() -> parent.setColorsRecursively(parent, bg, fg));
        });

        widthField.setColumns(10);
        heightField.setColumns(10);
        memoryField.setColumns(10);

        JLabel darkModeLabel = new JLabel("Dark Mode:");
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

        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        content.add(darkModeLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        content.add(darkModeCheckbox, gbc);

        add(content, BorderLayout.CENTER);
    }
}