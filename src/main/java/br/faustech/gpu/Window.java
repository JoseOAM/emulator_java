package br.faustech.gpu;

import br.faustech.cpu.CPUInterrupt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Handles the creation and management of a window using GLFW.
 */
@Getter
@RequiredArgsConstructor
public class Window {

    private final int width;  // Width of the window

    private final int height; // Height of the window

    private final String title; // Title of the window

    private long window; // Native handle to the GLFW window

    /**
     * Initializes and creates a window. Throws IllegalStateException if window creation fails.
     */
    public void init() {
        // Create a new GLFW window
        window = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        if (window == 0) {
            throw new IllegalStateException("Failed to create window");
        }

        // Center the window on the screen
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        assert vidMode != null; // Ensure video mode is available
        GLFW.glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);

        // Make the OpenGL context current on this window
        GLFW.glfwMakeContextCurrent(window);
        // Enable v-sync
        GLFW.glfwSwapInterval(1);
        // Show the window
        GLFW.glfwShowWindow(window);
        // Create capabilities for OpenGL
        GL.createCapabilities();

        // Set key callback
        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (CPUInterrupt.isInterruptEnabled() && action == GLFW.GLFW_PRESS) {
                CPUInterrupt.setInterruptData(key);
            }
        });
    }

    /**
     * Sets a resize callback for the window.
     *
     * @param callback A callback to handle framebuffer size changes.
     */
    public void setResizeCallback(GLFWFramebufferSizeCallbackI callback) {

        GLFW.glfwSetFramebufferSizeCallback(window, callback);
    }

    /**
     * Sets the window icon.
     */
    protected void setIcon() {

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Load the window icon image
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);
            ByteBuffer icon = STBImage.stbi_load("src/main/resources/images/icon.png", w, h, comp, 4);
            if (icon == null) {
                return;
            }
            GLFWImage.Buffer icons = GLFWImage.malloc(1);
            icons.position(0).width(w.get(0)).height(h.get(0)).pixels(icon);
            // Set the window icon
            GLFW.glfwSetWindowIcon(window, icons);
            STBImage.stbi_image_free(icon);
        }
    }

    /**
     * Checks if the window should be closed.
     *
     * @return true if the window should close, false otherwise.
     */
    public boolean shouldClose() {

        return GLFW.glfwWindowShouldClose(window);
    }

    /**
     * Swaps the front and back buffers of the window.
     */
    public void swapBuffers() {

        GLFW.glfwSwapBuffers(window);
    }

    /**
     * Processes all pending GLFW events.
     */
    public void pollEvents() {

        GLFW.glfwPollEvents();
    }

    /**
     * Destroys the window and releases resources.
     */
    public void cleanup() {

        GLFW.glfwDestroyWindow(window);
    }

}
