package de.labystudio.game.input;

import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Input handler for keyboard and mouse using GLFW
 */
public class Input {
    
    private final long window;
    
    // Mouse state
    private double mouseX;
    private double mouseY;
    private double lastMouseX;
    private double lastMouseY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    
    private boolean[] mouseButtons = new boolean[8];
    private boolean[] mouseButtonsPressed = new boolean[8];
    
    // Keyboard state
    private boolean[] keys = new boolean[GLFW_KEY_LAST];
    private boolean[] keysPressed = new boolean[GLFW_KEY_LAST];
    
    public Input(long window) {
        this.window = window;
        
        // Setup callbacks
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key >= 0 && key < GLFW_KEY_LAST) {
                if (action == GLFW_PRESS) {
                    keys[key] = true;
                    keysPressed[key] = true;
                } else if (action == GLFW_RELEASE) {
                    keys[key] = false;
                }
            }
        });
        
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (button >= 0 && button < 8) {
                if (action == GLFW_PRESS) {
                    mouseButtons[button] = true;
                    mouseButtonsPressed[button] = true;
                } else if (action == GLFW_RELEASE) {
                    mouseButtons[button] = false;
                }
            }
        });
        
        glfwSetCursorPosCallback(window, (win, x, y) -> {
            mouseX = x;
            mouseY = y;
        });
    }
    
    public void update() {
        // Calculate mouse delta
        mouseDeltaX = mouseX - lastMouseX;
        mouseDeltaY = mouseY - lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        
        // Clear "pressed" states (they only trigger once per press)
        for (int i = 0; i < keysPressed.length; i++) {
            keysPressed[i] = false;
        }
        for (int i = 0; i < mouseButtonsPressed.length; i++) {
            mouseButtonsPressed[i] = false;
        }
    }
    
    public boolean isKeyDown(int key) {
        if (key < 0 || key >= GLFW_KEY_LAST) return false;
        return keys[key];
    }
    
    public boolean isKeyPressed(int key) {
        if (key < 0 || key >= GLFW_KEY_LAST) return false;
        return keysPressed[key];
    }
    
    public boolean isMouseButtonDown(int button) {
        if (button < 0 || button >= 8) return false;
        return mouseButtons[button];
    }
    
    public boolean isMouseButtonPressed(int button) {
        if (button < 0 || button >= 8) return false;
        return mouseButtonsPressed[button];
    }
    
    public double getMouseDeltaX() {
        return mouseDeltaX;
    }
    
    public double getMouseDeltaY() {
        return mouseDeltaY;
    }
    
    public double getMouseX() {
        return mouseX;
    }
    
    public double getMouseY() {
        return mouseY;
    }
}
