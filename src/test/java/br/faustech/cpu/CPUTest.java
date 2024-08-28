package br.faustech.cpu;

import br.faustech.bus.Bus;
import br.faustech.gpu.FrameBuffer;
import br.faustech.gpu.GPU;
import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class CPUTest {

    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;
    private static final int FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4;
    private static final int MEMORY_SIZE = 4096;

    @Test
    public void cpuTest() {

        try {
            // Defining instructions in memory
            int[] instructions = {0b11111110000000010000000100010011, 0b00000000000100010010111000100011, 0b00000000100000010010110000100011, 0b00000010000000010000010000010011, 0b11111111001101001001011110110111, 0b00000000000001111000011110010011, 0b01001011111001111000011110010011, 0b11111110111101000010000000100011, 0b00000000000000000001011110110111, 0b00000000000001111000011110010011, 0b00000000010001111000011110010011, 0b11111110111101000010010000100011, 0b00000000000000000001011110110111, 0b00000000000001111000011110010011, 0b11111110111101000010001000100011, 0b11111110000001000010011000100011, 0b00000010100000000000000001101111, 0b11111110110001000010011110000011, 0b00000000001001111001011110010011, 0b11111110100001000010011100000011, 0b00000000111101110000011110110011, 0b11111110000001000010011100000011, 0b00000000111001111010000000100011, 0b11111110110001000010011110000011, 0b00000000000101111000011110010011, 0b11111110111101000010011000100011, 0b11111110110001000010011100000011, 0b00000000000000010011011110110111, 0b00000000000001111000011110010011, 0b10111111111101111000011110010011, 0b11111100111001111101011011100011, 0b11111110010001000010011110000011, 0b00000000000100000000011100010011, 0b00000000111001111010000000100011, 0b00000000000000000000011110010011, 0b00000000111100000000010100110011, 0b00000001110000010010000010000011, 0b00000001100000010010010000000011, 0b00000010000000010000000100010011, 0b00000000000000001000000001100111,};
            final Memory memory = new Memory(MEMORY_SIZE);
            final FrameBuffer frameBuffer = new FrameBuffer(FRAME_BUFFER_SIZE);
            final Bus bus = new Bus(frameBuffer, memory);
            final GPU gpu = new GPU(WIDTH, HEIGHT, frameBuffer);
            gpu.start();

            int position = 0;
            for (int instructionCode : instructions) {
                int[] instruction = new int[1];
                instruction[0] = instructionCode;
                bus.write(position, instruction);
                position += 4;
            }

            System.out.printf("Memory before execution: %s%n", Arrays.toString(memory.read(32, 64)));

            CPU cpu = new CPU(bus);
            cpu.start();

            while (gpu.isAlive()) {
                Thread.sleep(1500);
                cpu.interrupt();
                gpu.interrupt();
            }

            System.out.printf("Memory after execution: %s%n%n", Arrays.toString(memory.read(32, 64)));

        } catch (MemoryException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}