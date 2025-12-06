package de.labystudio.game;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class MinecraftWindow {

    public static final int DEFAULT_WIDTH = 854;
    public static final int DEFAULT_HEIGHT = 480;

    private final Minecraft game;

    protected boolean fullscreen;
    protected boolean enableVsync;

    public int displayWidth = DEFAULT_WIDTH;
    public int displayHeight = DEFAULT_HEIGHT;
    
    private long window;
    private boolean cursorGrabbed = false;

    public MinecraftWindow(Minecraft game) {
        this.game = game;
    }

    public void init() {
        // Setup error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        
        // Required for macOS
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        // Create window
        window = glfwCreateWindow(DEFAULT_WIDTH, DEFAULT_HEIGHT, "3DGame", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        // Setup resize callback
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            displayWidth = width;
            displayHeight = height;
            if (displayWidth <= 0) displayWidth = 1;
            if (displayHeight <= 0) displayHeight = 1;
        });

        // Center window
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidmode != null) {
            glfwSetWindowPos(window, 
                (vidmode.width() - DEFAULT_WIDTH) / 2,
                (vidmode.height() - DEFAULT_HEIGHT) / 2);
        }

        // Make OpenGL context current
        glfwMakeContextCurrent(window);
        
        // Enable v-sync
        glfwSwapInterval(0);

        // Show window
        glfwShowWindow(window);
        
        // Initialize OpenGL bindings
        GL.createCapabilities();
        
        // Get actual framebuffer size
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetFramebufferSize(window, width, height);
        displayWidth = width[0];
        displayHeight = height[0];
    }

    public void toggleFullscreen() {
        fullscreen = !fullscreen;
        
        if (fullscreen) {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            if (mode != null) {
                glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
                displayWidth = mode.width();
                displayHeight = mode.height();
            }
        } else {
            glfwSetWindowMonitor(window, NULL, 100, 100, DEFAULT_WIDTH, DEFAULT_HEIGHT, GLFW_DONT_CARE);
            displayWidth = DEFAULT_WIDTH;
            displayHeight = DEFAULT_HEIGHT;
        }
    }

    public void update() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public void destroy() {
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }
    
    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }
    
    public long getWindow() {
        return window;
    }
    
    public void setCursorGrabbed(boolean grabbed) {
        if (cursorGrabbed != grabbed) {
            cursorGrabbed = grabbed;
            glfwSetInputMode(window, GLFW_CURSOR, grabbed ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
            
            // Reset cursor position when grabbing to avoid jumps
            if (grabbed) {
                glfwSetCursorPos(window, displayWidth / 2.0, displayHeight / 2.0);
            }
        }
    }
    
    public boolean isCursorGrabbed() {
        return cursorGrabbed;
    }
    
    public boolean isWindowFocused() {
        return glfwGetWindowAttrib(window, GLFW_FOCUSED) == GLFW_TRUE;
    }
}
