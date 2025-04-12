package br.faustech.gui;

import br.faustech.comum.ArgsListener;
import br.faustech.comum.ConfigFile;
import br.faustech.cpu.CPU;
import br.faustech.gpu.GPU;
import br.faustech.reader.ProgramUtils;
import lombok.Getter;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class GUI extends JFrame {
    private final JTextField[] memoryField = new JTextField[32];
    private final JTabbedPane tabbedPane;
    private final JPanel contentPanel;
    private final JSplitPane splitPane;
    private final ArgsListener listener;
    private final List<String> recentFiles = new ArrayList<>();
    private AtomicBoolean darkModeEnabled = new AtomicBoolean();
    private final JMenu recentFilesMenu = new JMenu("Recent Files");
    private final ConfigFile configFile;
    private static final int MAX_RECENT_FILES = 5;
    private RegisterUpdater updater;
    private GPU gpu;
    private CPU cpu;
    private ProgramUtils programUtils;
    private boolean running = false;
    private String path;

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

        tabbedPane = new JTabbedPane();
        contentPanel = new JPanel(new BorderLayout());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, contentPanel);
        splitPane.setResizeWeight(0);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(0);

        add(splitPane, BorderLayout.CENTER);


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

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openFile());

        fileMenu.add(openItem);
        fileMenu.add(recentFilesMenu);

        menuBar.add(fileMenu);

        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> openSettingsWindow());

        JMenuItem debugTabItem = new JMenuItem("Open Debug Tab");
        debugTabItem.addActionListener(e -> openDebugTab());

        optionsMenu.add(settingsItem);
        optionsMenu.add(debugTabItem);

        updateRecentFilesMenu();

        menuBar.add(optionsMenu);

        return menuBar;
    }

    private JToolBar createActionBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton runButton = new JButton("Run");
        runButton.addActionListener(e -> startEmulator());

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopEmulator());

        toolBar.add(runButton);
        toolBar.add(stopButton);

        return toolBar;
    }

    private void openDebugTab() {
        String tabTitle = "Debug";

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(tabTitle)) {
                tabbedPane.removeTabAt(i);
                splitPane.setDividerLocation(0);
                return;
            }
        }

        JPanel debugPanel = new JPanel(new BorderLayout());
        debugPanel.add(new JLabel("Debug Info"), BorderLayout.NORTH);

        JPanel registerMemoryPanel = new JPanel(new GridLayout(32, 2, 5, 5));

        for (int i = 0; i < 32; i++) {
            memoryField[i] = new JTextField("0x0000");
            memoryField[i].setEditable(false);

            registerMemoryPanel.add(new JLabel("Reg " + i + ":"));
            registerMemoryPanel.add(memoryField[i]);
        }

        JScrollPane scrollPane = new JScrollPane(registerMemoryPanel);
        debugPanel.add(scrollPane, BorderLayout.CENTER);


        int index = tabbedPane.getTabCount();
        tabbedPane.addTab(tabTitle, debugPanel);

        JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(tabTitle + "  ");
        JButton closeButton = new JButton("X");
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusable(false);
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setMargin(new Insets(0, 0, 0, 0));

        closeButton.addActionListener(e -> {
            tabbedPane.remove(debugPanel);
            splitPane.setDividerLocation(0);
        });

        tabHeader.add(titleLabel);
        tabHeader.add(closeButton);

        tabbedPane.setTabComponentAt(index, tabHeader);

        tabbedPane.setSelectedComponent(debugPanel);
        splitPane.setDividerLocation(0.2);
    }


    private void openSettingsWindow() {
        SettingsWindow settingsWindow = new SettingsWindow(this);
        settingsWindow.setVisible(true);
    }

    private void openFileContentTab(File file) {
        try {
            int[] programInstructions = programUtils.readFile(file);
            JTextArea programHexadecimalArea = new JTextArea();
            JTextArea programDecimalArea = new JTextArea();

            programHexadecimalArea.setEditable(false);
            programDecimalArea.setEditable(false);

            programHexadecimalArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            programDecimalArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            StringBuilder hexadecimalContent = new StringBuilder();
            for (int i = 0; i < programInstructions.length; i++) {
                hexadecimalContent.append("Line ").append(i).append(": 0x")
                        .append(Integer.toHexString(programInstructions[i])).append("\n");
            }
            programHexadecimalArea.setText(hexadecimalContent.toString());

            StringBuilder decimalContent = new StringBuilder();
            for (int i = 0; i < programInstructions.length; i++) {
                decimalContent.append("Line ").append(i).append(": ")
                        .append(Integer.toBinaryString(programInstructions[i])).append("\n");
            }
            programDecimalArea.setText(decimalContent.toString());

            JScrollPane scrollHexadecimalPane = new JScrollPane(programHexadecimalArea);
            JScrollPane scrollDecimalPane = new JScrollPane(programDecimalArea);

            contentPanel.removeAll();

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    scrollHexadecimalPane, scrollDecimalPane);
            splitPane.setResizeWeight(0.5);

            contentPanel.add(splitPane, BorderLayout.CENTER);

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
            this.path = path;
            openFileContentTab(selectedFile);
        }
    }

    private void startEmulator() {
        if (listener != null && path != null && !path.isBlank()) {
            try {
                listener.onArgsSelected(path);
                running = true;
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
                this.path = file;
                File selectedFile = new File(path);
                openFileContentTab(selectedFile);
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
        this.running = running;
    }

    public boolean getRunning() {
        return running;
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
                UIManager.put("Panel.background", Color.DARK_GRAY);
                UIManager.put("Label.foreground", Color.WHITE);
                UIManager.put("TextField.background", new Color(50, 50, 50));
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("ScrollPane.background", Color.DARK_GRAY);
                bg = Color.DARK_GRAY;
                fg = Color.WHITE;
            } else {
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