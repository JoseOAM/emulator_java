package br.faustech.cpu;

import br.faustech.gpu.Window;
import org.lwjgl.glfw.GLFW;

/**
 * Custom exception that signals an "ebreak" condition in the CPU execution.
 * This exception will also trigger the closing of the GLFW window.
 */
public class EbreakException extends RuntimeException {

    /**
     * Constructs a new EbreakException with the specified detail message.
     * When this exception is thrown, it will signal GLFW to close the application window.
     *
     * @param message the detail message that explains the cause of the exception.
     */
    public EbreakException(String message) {
        super(message);
        GLFW.glfwSetWindowShouldClose(Window.getWindow(), true);
    }
}
