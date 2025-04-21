package br.faustech;

import br.faustech.bus.Bus;
import br.faustech.comum.ConfigFile;
import br.faustech.cpu.CPU;
import br.faustech.gpu.GPU;
import br.faustech.gui.GUI;
import br.faustech.comum.ArgsListener;
import br.faustech.memory.FrameBuffer;
import br.faustech.memory.Memory;
import br.faustech.reader.ProgramUtils;
import lombok.Getter;
import lombok.extern.java.Log;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import java.nio.file.Paths;

@Log
public class Main {
    private static final int WIDTH = 320;

    private static final int HEIGHT = 240;

    private static final int FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4; // 4 bytes per pixel

    private static final int MEMORY_SIZE = 4194304; // 4MB

    @Getter
    private static int clockSpeed = 1000; // 1Hz

    private static boolean LOG = true;

    private static GPU gpu;

    private static CPU cpu;

    private static GUI gui;

    private static ConfigFile configFile;

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        LOG = args.length > 1 && args[1].equals("--log");
        configFile = new ConfigFile();
        ProgramUtils programUtils = new ProgramUtils();

        gui = new GUI(new ArgsListener() {
            @Override
            public void onArgsSelected(String path) {
                new Thread(() -> {
                    try {
                        final FrameBuffer frameBuffer = new FrameBuffer(FRAME_BUFFER_SIZE);
                        final Bus bus = new Bus(frameBuffer, new Memory(MEMORY_SIZE));

                        programUtils.setUtils(bus);
                        programUtils.writeProgramInMemory(programUtils.readFile(new File(path)));

                        gpu = new GPU(WIDTH, HEIGHT, frameBuffer, Paths.get(gui.getPath()).getFileName().toString());
                        cpu = new CPU(bus, gui);
                        gpu.start();
                        cpu.start();

                        gui.setGPU(gpu);
                        gui.setCPU(cpu);

                        while (gpu.isAlive()) {
                            if (gpu.getState() == Thread.State.TERMINATED) {
                                gui.getRegisterUpdater().stopUpdater();
                                gui.consoleInfo("Emulator stopped");
                                gpu.interrupt();
                                cpu.interrupt();
                                configFile.saveHistory(gui.getRecentFiles(), gui.getDarkModeEnabled());
                                gui.setRunning(false);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }, configFile, programUtils);
    }

    public static void info(String message) {
        if (LOG) {
            log.info(message);
        }
    }
}
