package br.faustech.gpu;

import br.faustech.comum.RenderDataDto;
import lombok.extern.java.Log;
import org.lwjgl.opengl.GL46;

import java.nio.FloatBuffer;

/**
 * Handles the setup, updating, and drawing of render data for OpenGL.
 */
@Log
public class RenderData {

    private final int width, height; // Dimensions for the texture

    private final int bufferSize = FrameBuffer.getBufferSize(); // Size of the buffer

    private final int numVertices = bufferSize / 8; // Number of vertices to draw

    private int vao, vbo, textureId; // OpenGL object identifiers

    private int[] pboIds; // Array of Pixel Buffer Object identifiers

    private int nextPboIndex = 0; // Index of the next PBO to use

    /**
     * Constructs a RenderData instance with specified texture dimensions.
     *
     * @param width  the width of the texture
     * @param height the height of the texture
     */
    public RenderData(final int width, final int height) {

        this.width = width;
        this.height = height;
    }

    /**
     * Sets up OpenGL settings and initializes textures, buffers, and array objects.
     */
    public void setup() {

        GL46.glEnable(GL46.GL_TEXTURE_2D);
        GL46.glPixelStorei(GL46.GL_UNPACK_ALIGNMENT, 4);
        setupTexture();
        setupVAOAndVBO();
        setupPBOs();
        GL46.glPointSize(4.0f);
    }

    /**
     * Initializes the texture settings and allocates texture memory.
     */
    private void setupTexture() {

        textureId = GL46.glGenTextures();
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_REPEAT);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_REPEAT);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_NEAREST);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_NEAREST);
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, width, height, 0, GL46.GL_RGBA, GL46.GL_FLOAT, (FloatBuffer) null);
    }

    /**
     * Sets up the Vertex Array Object (VAO) and Vertex Buffer Object (VBO).
     */
    private void setupVAOAndVBO() {

        vao = GL46.glGenVertexArrays();
        GL46.glBindVertexArray(vao);
        vbo = GL46.glGenBuffers();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);

        int stride = 8 * Float.BYTES;
        GL46.glVertexAttribPointer(0, 2, GL46.GL_FLOAT, false, stride, 0);
        GL46.glEnableVertexAttribArray(0);
        GL46.glVertexAttribPointer(1, 4, GL46.GL_FLOAT, false, stride, 2 * Float.BYTES);
        GL46.glEnableVertexAttribArray(1);
        GL46.glVertexAttribPointer(2, 2, GL46.GL_FLOAT, false, stride, 6 * Float.BYTES);
        GL46.glEnableVertexAttribArray(2);
    }

    /**
     * Sets up the Pixel Buffer Objects (PBOs) for efficient texture streaming.
     */
    private void setupPBOs() {

        int pboCount = 2;
        pboIds = new int[pboCount];
        GL46.glGenBuffers(pboIds);
        for (int i = 0; i < pboCount; i++) {
            GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, pboIds[i]);
            GL46.glBufferData(GL46.GL_PIXEL_UNPACK_BUFFER, bufferSize, GL46.GL_STREAM_DRAW);
        }
    }

    /**
     * Draws the vertex data and updates the texture.
     *
     * @param dataDto the object containing the vertex and pixel data
     */
    public void draw(RenderDataDto dataDto) {
        // Update the VBO with new data
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, dataDto.vertex(), GL46.GL_STREAM_DRAW);

        int pboId = pboIds[nextPboIndex];
        nextPboIndex = (nextPboIndex + 1) % pboIds.length;

        GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, pboId);
        GL46.glBufferData(GL46.GL_PIXEL_UNPACK_BUFFER, dataDto.pixel(), GL46.GL_STREAM_DRAW);

        GL46.glUnmapBuffer(GL46.GL_PIXEL_UNPACK_BUFFER);

        GL46.glTexSubImage2D(GL46.GL_TEXTURE_2D, 0, 0, 0, width, height, GL46.GL_RGBA, GL46.GL_FLOAT, 0);

        GL46.glGenerateMipmap(GL46.GL_TEXTURE_2D);

        GL46.glDrawArrays(GL46.GL_POINTS, 0, numVertices);
    }

    /**
     * Cleans up OpenGL resources by deleting buffers, array objects, and textures.
     */
    public void cleanup() {

        GL46.glDeleteBuffers(vbo);
        GL46.glDeleteVertexArrays(vao);
        GL46.glDeleteTextures(textureId);
        GL46.glDeleteBuffers(pboIds);
    }

}
