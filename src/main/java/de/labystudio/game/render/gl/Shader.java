package de.labystudio.game.render.gl;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class Shader {
    
    private final int programId;
    
    public Shader(String vertexPath, String fragmentPath) {
        int vertexShader = loadShader(vertexPath, GL_VERTEX_SHADER);
        int fragmentShader = loadShader(fragmentPath, GL_FRAGMENT_SHADER);
        
        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);
        
        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(programId);
            throw new RuntimeException("Failed to link shader program: " + log);
        }
        
        glDetachShader(programId, vertexShader);
        glDetachShader(programId, fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }
    
    private int loadShader(String path, int type) {
        String source = readFile(path);
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            throw new RuntimeException("Failed to compile shader " + path + ": " + log);
        }
        
        return shader;
    }
    
    private String readFile(String path) {
        StringBuilder source = new StringBuilder();
        try (InputStream in = Shader.class.getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader file: " + path, e);
        }
        return source.toString();
    }
    
    public void bind() {
        glUseProgram(programId);
    }
    
    public void unbind() {
        glUseProgram(0);
    }
    
    public void delete() {
        glDeleteProgram(programId);
    }
    
    public int getUniformLocation(String name) {
        int location = glGetUniformLocation(programId, name);
        if (location == -1) {
            System.err.println("Warning: Uniform '" + name + "' not found in shader program " + programId);
        }
        return location;
    }
    
    public void setUniform(String name, int value) {
        glUniform1i(getUniformLocation(name), value);
    }
    
    public void setUniform(String name, float value) {
        glUniform1f(getUniformLocation(name), value);
    }
    
    public void setUniform(String name, float x, float y, float z, float w) {
        glUniform4f(getUniformLocation(name), x, y, z, w);
    }
    
    public void setUniform(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(getUniformLocation(name), false, buffer);
        }
    }
    
    public int getProgramId() {
        return programId;
    }
}
