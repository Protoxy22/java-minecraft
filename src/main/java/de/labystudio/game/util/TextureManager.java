package de.labystudio.game.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureManager {
    private static int lastId = Integer.MIN_VALUE;

    /**
     * Load a texture into OpenGL
     *
     * @param resourceName Resource path of the image
     * @param mode         Texture filter mode (GL_NEAREST, GL_LINEAR)
     * @return Texture id of OpenGL
     */
    public static int loadTexture(String resourceName, int mode) {
        // Generate a new texture id
        int id = glGenTextures();

        // Bind this texture id
        bind(id);

        // Set texture filter mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mode);

        // Read from resources
        try (InputStream inputStream = TextureManager.class.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find texture: " + resourceName);
            }
            
            // Read entire stream into buffer
            ByteBuffer imageBuffer = readInputStream(inputStream);
            
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                
                // Load image using STB
                ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
                if (image == null) {
                    throw new RuntimeException("Failed to load texture: " + resourceName + " - " + STBImage.stbi_failure_reason());
                }
                
                int width = w.get();
                int height = h.get();
                
                // Upload texture to GPU
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
                glGenerateMipmap(GL_TEXTURE_2D);
                
                // Free image memory
                STBImage.stbi_image_free(image);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Could not load texture " + resourceName, exception);
        }

        return id;
    }
    
    private static ByteBuffer readInputStream(InputStream input) throws IOException {
        ByteBuffer buffer = BufferUtils.createByteBuffer(8192);
        
        try (ReadableByteChannel channel = Channels.newChannel(input)) {
            while (channel.read(buffer) != -1) {
                if (buffer.remaining() == 0) {
                    // Resize buffer
                    ByteBuffer newBuffer = BufferUtils.createByteBuffer(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
            }
        }
        
        buffer.flip();
        return buffer;
    }

    /**
     * Bind the texture to OpenGL using the id from {@link #loadTexture(String, int)}
     *
     * @param id Texture id
     */
    public static void bind(int id) {
        if (id != lastId) {
            glBindTexture(GL_TEXTURE_2D, id);
            lastId = id;
        }
    }
}