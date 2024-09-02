package br.faustech.memory;

import br.faustech.comum.RenderDataDto;
import br.faustech.gpu.GPU;
import lombok.Getter;
import lombok.extern.java.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * A class representing a framebuffer that manages two buffers for double buffering.
 */
@Log
public class FrameBuffer {

    @Getter
    private static int bufferSize; // Size of each buffer

    private byte[] frontPixelBuffer; // Buffer to store pixel data

    private byte[] backPixelBuffer; // Buffer to store pixel data

    private byte[] frontVertexBuffer; // Buffer currently displayed

    private byte[] backVertexBuffer; // Buffer to write new data to

    /**
     * Constructs a FrameBuffer with specified memory addresses and buffer size.
     *
     * @param bufferSize The size of each buffer.
     */
    public FrameBuffer(final int bufferSize) {

        final int size = bufferSize * 8;
        this.frontPixelBuffer = new byte[size];  // Initialize pixel buffer
        this.backPixelBuffer = new byte[size];  // Initialize pixel buffer
        this.frontVertexBuffer = new byte[size];  // Initialize front buffer
        this.backVertexBuffer = new byte[size];   // Initialize back buffer
        FrameBuffer.bufferSize = bufferSize * 2;
    }

    /**
     * Writes pixel data to the back buffer starting from a specified position.
     *
     * @param beginAddress The starting position in the back buffer.
     * @param data         The pixel data as an array of integers.
     * @throws MemoryException If the write operation exceeds buffer limits.
     */
    public void writePixel(int beginAddress, final int[] data) throws MemoryException {

        int width = GPU.getWidth();
        int height = GPU.getHeight();

        for (int i = 0; i < data.length; i++) {
            int color = data[i];

            // Calculate normalized coordinates for texture mapping
            int x = ((beginAddress / 4) + (i * 4)) % width;
            int y = ((beginAddress / 4) + (i * 4)) / width;

            float normX = (x / (float) width) * 2 - 1;
            float normY = ((height - y) / (float) height) * 2 - 1;

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float u = x / (float) width;
            float v = y / (float) height;

            this.writeToPixelBufferFromFloats(beginAddress * 4, new float[]{r, g, b, 1});

            this.writeToVertexBufferFromFloats((y * width + x) * 32, new float[]{normX, normY, r, g, b, 1, u, v});
        }
    }

    /**
     * Writes float data to the pixel buffer, converting them to bytes before storing.
     *
     * @param beginAddress The starting index where data is to be written.
     * @param data         The float data to be converted and written.
     * @throws MemoryException If the write operation exceeds buffer limits.
     */

    public void writeToVertexBufferFromFloats(final int beginAddress, final float[] data) throws MemoryException {
        this.writeToBufferFromFloats(this.backVertexBuffer, beginAddress, data);
    }

    /**
     * Writes float data to the pixel buffer, converting them to bytes before storing.
     *
     * @param beginAddress The starting index where data is to be written.
     * @param data         The float data to be converted and written.
     * @throws MemoryException If the write operation exceeds buffer limits.
     */
    public void writeToPixelBufferFromFloats(final int beginAddress, final float[] data) throws MemoryException {
        this.writeToBufferFromFloats(this.backPixelBuffer, beginAddress, data);
    }

    /**
     * Writes float data to the buffer, converting them to bytes before storing.
     *
     * @param buffer       The buffer to write data to.
     * @param beginAddress The starting index where data is to be written.
     * @param data         The float data to be converted and written.
     * @throws MemoryException If the write operation exceeds buffer limits.
     */
    public void writeToBufferFromFloats(final byte[] buffer, final int beginAddress, final float[] data) throws MemoryException {

        checkAddressRange(beginAddress, data.length, buffer);

        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4).order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(data);

        byteBuffer.rewind();
        byteBuffer.get(buffer, beginAddress, byteBuffer.remaining());
    }

    /**
     * Checks if the address range is valid for the given data length.
     *
     * @param beginAddress The starting index in the buffer.
     * @param data         The length of the data to be written.
     * @param backBuffer   The buffer to write data to.
     * @throws MemoryException If the address range is invalid.
     */
    private void checkAddressRange(int beginAddress, int data, byte[] backBuffer) throws MemoryException {
        int endAddress = beginAddress + data;
        if (beginAddress < 0 || endAddress > backBuffer.length) {
            throw new MemoryException("Invalid data positions or data length. (beginAddress: " + beginAddress + ", endAddress: " + endAddress + ")");
        }
    }

    /**
     * Swaps the front and back buffers, promoting the back to front for display.
     */
    public void swap() {

        byte[] temp = frontVertexBuffer;
        frontVertexBuffer = backVertexBuffer;
        backVertexBuffer = temp;

        temp = frontPixelBuffer;
        frontPixelBuffer = backPixelBuffer;
        backPixelBuffer = temp;
    }

    /**
     * Retrieves the render data from the front buffer.
     *
     * @return A RenderDataDto object containing the vertex and pixel data.
     * @throws MemoryException If invalid data positions are used.
     */
    public RenderDataDto getRenderData() throws MemoryException {
        return RenderDataDto.builder().vertex(readFromVertexBufferAsFloats(0, bufferSize)).pixel(readFromPixelBufferAsFloats(0, bufferSize)).build();
    }

    /**
     * Reads a segment of the front buffer as integer data.
     *
     * @param beginAddress The starting index in the buffer.
     * @param endAddress   The ending index in the buffer.
     * @return An array of integers read from the buffer.
     * @throws MemoryException If invalid data positions are used.
     */
    public float[] readFromPixelBufferAsFloats(final int beginAddress, final int endAddress) throws MemoryException {
        return this.readFromBufferAsFloats(frontPixelBuffer, beginAddress, endAddress);
    }

    /**
     * Reads a segment of the front buffer as float data.
     *
     * @param beginAddress The starting index in the buffer.
     * @param endAddress   The ending index in the buffer.
     * @return An array of floats read from the buffer.
     * @throws MemoryException If invalid data positions are used.
     */
    public float[] readFromVertexBufferAsFloats(final int beginAddress, final int endAddress) throws MemoryException {
        return this.readFromBufferAsFloats(frontVertexBuffer, beginAddress, endAddress);
    }

    /**
     * Reads a segment of the given buffer as float data.
     *
     * @param beginAddress The starting index in the buffer.
     * @param endAddress   The ending index in the buffer.
     * @return An array of floats read from the buffer.
     * @throws MemoryException If invalid data positions are used.
     */
    public float[] readFromBufferAsFloats(final byte[] buffer, final int beginAddress, final int endAddress) throws MemoryException {

        int length = endAddress - beginAddress;

        final ByteBuffer byteBuffer = getByteBufferFromBuffer(buffer, beginAddress, endAddress);

        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        float[] floatArray = new float[length];
        floatBuffer.get(floatArray, 0, length);

        return floatArray;
    }

    /**
     * Retrieves a ByteBuffer from the given buffer with specified start and end positions.
     *
     * @param buffer       The byte array buffer.
     * @param beginAddress The starting index in the buffer.
     * @param endAddress   The ending index in the buffer.
     * @return A ByteBuffer positioned at the specified data range.
     * @throws MemoryException If invalid data positions are used.
     */
    private ByteBuffer getByteBufferFromBuffer(final byte[] buffer, final int beginAddress, final int endAddress) throws MemoryException {

        if (beginAddress < 0 || endAddress > buffer.length || beginAddress >= endAddress) {
            throw new MemoryException("Invalid data positions or data length. (beginAddress: " + beginAddress + ", endAddress: " + endAddress + ")");
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.position(beginAddress);
        return byteBuffer;
    }

    /**
     * Reads a segment of the front buffer as integer data.
     *
     * @param beginAddress The starting index in the buffer.
     * @param endAddress   The ending index in the buffer.
     * @return An array of integers read from the buffer.
     * @throws MemoryException If invalid data positions are used.
     */
    public int[] readFromPixelBufferAsInts(final int beginAddress, final int endAddress) throws MemoryException {

        int length = endAddress - beginAddress;

        final ByteBuffer byteBuffer = getByteBufferFromBuffer(frontPixelBuffer, beginAddress, endAddress);

        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        int[] intArray = new int[length];
        intBuffer.get(intArray, 0, length);

        return intArray;
    }

}
