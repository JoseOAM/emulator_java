package br.faustech.gpu;

import br.faustech.bus.Bus;
import br.faustech.memory.FrameBuffer;
import br.faustech.memory.Memory;
import org.junit.jupiter.api.Test;

import java.lang.Thread.State;


public class GPUTest {

    private static final String VIDEO_PATH = "";

    private static final int WIDTH = 320;

    private static final int HEIGHT = 240;

    private static final int FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4;

    private static final int MEMORY_SIZE = 4096;

    @Test
    public void gpuTest() {

        FrameBuffer frameBuffer = new FrameBuffer(FRAME_BUFFER_SIZE);

        Bus bus = new Bus(frameBuffer, new Memory(MEMORY_SIZE));

        VideoFrameToVertexArray videoProcessor = new VideoFrameToVertexArray(VIDEO_PATH, WIDTH, HEIGHT, bus, frameBuffer);
        videoProcessor.start();

        GPU gpu = new GPU(WIDTH, HEIGHT, frameBuffer);
        gpu.start();

        while (gpu.isAlive()) {
            if (gpu.getState() == State.TERMINATED) {
                if (videoProcessor.getState() != State.TERMINATED) {
                    videoProcessor.interrupt();
                }
                if (gpu.getState() != State.TERMINATED) {
                    gpu.interrupt();
                }
            }
        }
    }
}
