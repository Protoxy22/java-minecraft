package de.labystudio.game.render.gl;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Represents a chunk mesh stored in GPU memory using VBO
 */
public class ChunkMesh {
    
    private int vao;
    private int vbo;
    private int vertexCount;
    private boolean built;
    
    public ChunkMesh() {
        this.vao = glGenVertexArrays();
        this.vbo = glGenBuffers();
        this.built = false;
    }
    
    public void upload(FloatBuffer data, int vertices) {
        if (data == null || vertices == 0) {
            this.vertexCount = 0;
            this.built = false;
            return;
        }
        
        this.vertexCount = vertices;
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
        data.flip();
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        
        // Position attribute (location 0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        
        // Texture coordinate attribute (location 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        
        // Color attribute (location 2)
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 9 * Float.BYTES, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        this.built = true;
    }
    
    public void render() {
        if (!built || vertexCount == 0) {
            return;
        }
        
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }
    
    public void delete() {
        if (vao != 0) {
            glDeleteVertexArrays(vao);
            vao = 0;
        }
        if (vbo != 0) {
            glDeleteBuffers(vbo);
            vbo = 0;
        }
        built = false;
    }
    
    public boolean isBuilt() {
        return built;
    }
}
