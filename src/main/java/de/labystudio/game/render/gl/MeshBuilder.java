package de.labystudio.game.render.gl;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Modern replacement for the old Tessellator using VAO/VBO
 */
public class MeshBuilder {
    
    public static final MeshBuilder instance = new MeshBuilder(0x200000);
    
    private final int maxVertices;
    private FloatBuffer vertexBuffer;
    private int vertexCount;
    
    private float x, y, z;
    private float u, v;
    private float r, g, b, a;
    
    private boolean building;
    
    // VBO/VAO IDs
    private int vao;
    private int vbo;
    
    public MeshBuilder(int maxVertices) {
        this.maxVertices = maxVertices;
        this.vertexBuffer = MemoryUtil.memAllocFloat(maxVertices * 9); // 3 pos + 2 uv + 4 color
        
        // Create VAO and VBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        
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
        
        // Default values
        r = g = b = a = 1.0f;
    }
    
    public void begin() {
        if (building) {
            throw new IllegalStateException("Already building!");
        }
        building = true;
        vertexCount = 0;
        vertexBuffer.clear();
    }
    
    public void end() {
        if (!building) {
            throw new IllegalStateException("Not building!");
        }
        building = false;
    }
    
    public void draw() {
        if (building) {
            throw new IllegalStateException("Still building!");
        }
        
        if (vertexCount == 0) {
            return;
        }
        
        vertexBuffer.flip();
        
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
        
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        
        vertexBuffer.clear();
        vertexCount = 0;
    }
    
    public void pos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void tex(float u, float v) {
        this.u = u;
        this.v = v;
    }
    
    public void color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    public void color(float r, float g, float b) {
        color(r, g, b, 1.0f);
    }
    
    public void color(int r, int g, int b, int a) {
        color(r / 255f, g / 255f, b / 255f, a / 255f);
    }
    
    public void color(int r, int g, int b) {
        color(r / 255f, g / 255f, b / 255f, 1.0f);
    }
    
    public void vertex() {
        if (!building) {
            throw new IllegalStateException("Not building!");
        }
        
        if (vertexCount >= maxVertices) {
            throw new IllegalStateException("Too many vertices!");
        }
        
        vertexBuffer.put(x).put(y).put(z);
        vertexBuffer.put(u).put(v);
        vertexBuffer.put(r).put(g).put(b).put(a);
        
        vertexCount++;
    }
    
    public void addVertex(float x, float y, float z) {
        pos(x, y, z);
        vertex();
    }
    
    public void addVertexWithUV(float x, float y, float z, float u, float v) {
        pos(x, y, z);
        tex(u, v);
        vertex();
    }
    
    public void cleanup() {
        if (vertexBuffer != null) {
            MemoryUtil.memFree(vertexBuffer);
            vertexBuffer = null;
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
            vao = 0;
        }
        if (vbo != 0) {
            glDeleteBuffers(vbo);
            vbo = 0;
        }
    }
}
