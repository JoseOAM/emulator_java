package br.faustech.gpu;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;

import java.nio.IntBuffer;

/**
 * Manages the compilation, linking, and usage of a shader program in OpenGL.
 */
public class ShaderProgram {

    private int programId; // Identifier for the compiled shader program

    /**
     * Loads and compiles vertex and fragment shaders, links them into a program.
     */
    public void loadShaders() {
        // Compile the vertex shader
        int vertexShader = compileShader(GL46.GL_VERTEX_SHADER, """
                   #version 460
                     layout (location = 0) in vec2 vertexPosition;
                     layout (location = 1) in vec4 vertexColor;
                     layout (location = 2) in vec2 texCoord;
                     out vec2 TexCoord;
                     out vec4 outColor;
                     void main() {
                         gl_Position = vec4(vertexPosition, 0.0, 1.0);
                         TexCoord = texCoord;
                         outColor = vertexColor;
                     }
                """);

        // Compile the fragment shader
        int fragmentShader = compileShader(GL46.GL_FRAGMENT_SHADER, """
                    #version 460
                     in vec2 TexCoord;
                     in vec4 outColor;
                     out vec4 FragColor;
                     uniform sampler2D ourTexture;
                     void main() {
                         FragColor = texture(ourTexture, TexCoord) * outColor;
                     }
                """);

        // Create the shader program and attach the compiled shaders
        programId = GL46.glCreateProgram();
        GL46.glAttachShader(programId, vertexShader);
        GL46.glAttachShader(programId, fragmentShader);
        GL46.glLinkProgram(programId); // Link the shaders into a usable program
        checkLinkStatus(programId); // Check for errors in linking

        // Clean up the individual shaders as they are no longer needed after linking
        GL46.glDeleteShader(vertexShader);
        GL46.glDeleteShader(fragmentShader);
    }

    /**
     * Compiles a shader from source code.
     *
     * @param type   The type of shader to compile (GL_VERTEX_SHADER or GL_FRAGMENT_SHADER).
     * @param source The GLSL source code for the shader.
     * @return The compiled shader's identifier.
     */
    private int compileShader(int type, String source) {

        int shader = GL46.glCreateShader(type); // Create the shader
        GL46.glShaderSource(shader, source); // Set the source code
        GL46.glCompileShader(shader); // Compile the shader
        checkCompileStatus(shader); // Check for compilation errors
        return shader;
    }

    /**
     * Checks the link status of the shader program.
     *
     * @param program The program identifier.
     */
    private void checkLinkStatus(int program) {

        IntBuffer status = BufferUtils.createIntBuffer(1); // Buffer for reading status
        GL46.glGetProgramiv(program, GL46.GL_LINK_STATUS, status);
        if (status.get(0) == GL46.GL_FALSE) {
            // If linking failed, throw an exception with the log
            throw new RuntimeException(
                    String.format("Program link error: %s", GL46.glGetProgramInfoLog(program)));
        }
    }

    /**
     * Checks the compilation status of a shader.
     *
     * @param shader The shader identifier.
     */
    private void checkCompileStatus(int shader) {

        IntBuffer status = BufferUtils.createIntBuffer(1); // Buffer for reading status
        GL46.glGetShaderiv(shader, GL46.GL_COMPILE_STATUS, status);
        if (status.get(0) == GL46.GL_FALSE) {
            // If compilation failed, throw an exception with the log
            throw new RuntimeException(
                    String.format("Shader compile error: %s", GL46.glGetShaderInfoLog(shader)));
        }
    }

    /**
     * Activates this shader program for use in rendering.
     */
    public void use() {

        GL46.glUseProgram(programId);
    }

    /**
     * Cleans up resources associated with the shader program.
     */
    public void cleanup() {

        GL46.glDeleteProgram(programId);
    }

}
