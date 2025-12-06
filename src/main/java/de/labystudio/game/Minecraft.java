package de.labystudio.game;

import de.labystudio.game.input.Input;
import de.labystudio.game.player.Player;
import de.labystudio.game.render.gl.Camera;
import de.labystudio.game.render.gui.FontRenderer;
import de.labystudio.game.render.gui.GuiRenderer;
import de.labystudio.game.util.*;
import de.labystudio.game.world.World;
import de.labystudio.game.world.WorldRenderer;
import de.labystudio.game.world.block.Block;
import de.labystudio.game.world.chunk.ChunkSection;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Minecraft implements Runnable {

    private final MinecraftWindow gameWindow = new MinecraftWindow(this);
    protected final GuiRenderer gui = new GuiRenderer();

    // Game
    private final Timer timer = new Timer(20.0F);
    private World world;
    private WorldRenderer worldRenderer;
    private FontRenderer fontRenderer;
    private Camera camera;
    private Input input;

    // Player
    private Player player;
    private Block pickedBlock = Block.STONE;

    // States
    private boolean paused = false;
    private boolean running = true;
    private int fps;

    public void init() throws IOException {
        // Setup display
        this.gameWindow.init();
        this.input = new Input(this.gameWindow.getWindow());
        this.gui.init(this.gameWindow);
        this.gui.loadTextures();

        // Setup camera
        this.camera = new Camera(85.0f, 
            (float) this.gameWindow.displayWidth / (float) this.gameWindow.displayHeight,
            0.05f,
            (float) Math.pow(WorldRenderer.RENDER_DISTANCE * ChunkSection.SIZE, 2));

        // Setup rendering
        this.world = new World();
        this.worldRenderer = new WorldRenderer(this.world);
        this.worldRenderer.setCamera(this.camera);
        this.player = new Player(this.world);
        this.player.setInput(this.input);
        this.fontRenderer = new FontRenderer(this.gui, "/font.png");

        // Grab cursor
        this.gameWindow.setCursorGrabbed(true);
    }

    public void run() {
        try {
            this.init();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        long lastTime = System.currentTimeMillis();
        int frames = 0;
        try {
            while (!this.gameWindow.shouldClose() && this.running) {
                // Update input
                this.input.update();
                
                // Timer ticks
                this.timer.advanceTime();
                for (int i = 0; i < this.timer.ticks; i++) {
                    this.tick();
                }

                // Render
                glViewport(0, 0, this.gameWindow.displayWidth, this.gameWindow.displayHeight);
                this.render(this.timer.partialTicks);
                this.gameWindow.update();
                checkError();

                // FPS counter
                frames++;
                while (System.currentTimeMillis() >= lastTime + 1000L) {
                    this.fps = frames;

                    lastTime += 1000L;
                    frames = 0;
                }

                // Escape - pause game
                if (this.input.isKeyPressed(GLFW_KEY_ESCAPE) || !this.gameWindow.isWindowFocused()) {
                    this.paused = true;
                    this.gameWindow.setCursorGrabbed(false);
                }

                // F11 - Toggle fullscreen
                if (this.input.isKeyPressed(GLFW_KEY_F11)) {
                    this.gameWindow.toggleFullscreen();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Shutdown
            this.world.save();
            this.worldRenderer.cleanup();
            this.gameWindow.destroy();

            System.exit(0);
        }
    }

    public void shutdown() {
        this.running = false;
    }

    public void tick() {
        this.player.onTick();
        this.world.onTick();
        this.worldRenderer.onTick();
    }

    private void setupCamera(float partialTicks) {
        // Update camera aspect ratio if window resized
        float aspectRatio = (float) this.gameWindow.displayWidth / (float) this.gameWindow.displayHeight;
        camera.setAspectRatio(aspectRatio);
        
        // Update FOV with modifier
        camera.setFov(85.0f + this.player.getFOVModifier());
        camera.updateProjectionMatrix();
        
        // Set camera position (player eye position)
        double x = this.player.prevX + (this.player.x - this.player.prevX) * partialTicks;
        double y = this.player.prevY + (this.player.y - this.player.prevY) * partialTicks;
        double z = this.player.prevZ + (this.player.z - this.player.prevZ) * partialTicks;
        
        camera.setPosition((float) x, (float) (y + this.player.getEyeHeight()), (float) z);
        camera.setRotation(this.player.pitch, this.player.yaw);
        camera.updateViewMatrix();
    }


    public void render(float partialTicks) {
        double mouseMoveX = this.input.getMouseDeltaX();
        double mouseMoveY = this.input.getMouseDeltaY();

        if (!this.paused) {
            this.player.turn((float) mouseMoveX, (float) mouseMoveY);
        }

        // Calculate the target block of the player
        HitResult hitResult = this.getTargetBlock();

        // Handle mouse input
        if (this.input.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
            // Resume game if paused
            if (this.paused) {
                this.paused = false;
                this.gameWindow.setCursorGrabbed(true);
            } else {
                // Destroy block
                if (hitResult != null) {
                    this.world.setBlockAt(hitResult.x, hitResult.y, hitResult.z, 0);
                }
            }
        }

        // Place block
        if (this.input.isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
            if (hitResult != null) {
                int x = hitResult.x + hitResult.face.x;
                int y = hitResult.y + hitResult.face.y;
                int z = hitResult.z + hitResult.face.z;

                BoundingBox placedBoundingBox = new BoundingBox(x, y, z, x + 1, y + 1, z + 1);

                // Don't place blocks if the player is standing there
                if (!placedBoundingBox.intersects(this.player.boundingBox)) {
                    this.world.setBlockAt(x, y, z, this.pickedBlock.getId());
                }
            }
        }

        // Pick block
        if (this.input.isMouseButtonPressed(GLFW_MOUSE_BUTTON_MIDDLE)) {
            if (hitResult != null) {
                short typeId = this.world.getBlockAt(hitResult.x, hitResult.y, hitResult.z);
                if (typeId != 0) {
                    this.pickedBlock = Block.getById(typeId);
                }
            }
        }
        
        // Save world
        if (this.input.isKeyPressed(GLFW_KEY_ENTER)) {
            this.world.save();
        }

        // Clear color and depth buffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Camera
        this.setupCamera(partialTicks);

        int cameraChunkX = (int) this.player.x >> 4;
        int cameraChunkZ = (int) this.player.z >> 4;

        // Setup fog
        this.worldRenderer.setupFog(this.player.isHeadInWater());

        // Setup rendering for solid blocks
        glDisable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        // Render solid blocks
        this.worldRenderer.render(cameraChunkX, cameraChunkZ, EnumWorldBlockLayer.SOLID);

        // Enable alpha and disable face culling
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);

        // Render cutout blocks (Leaves, glass, water..)
        this.worldRenderer.render(cameraChunkX, cameraChunkZ, EnumWorldBlockLayer.CUTOUT);

        // Render selection (TODO: implement with modern OpenGL)
        // if (hitResult != null) {
        //     this.renderSelection(hitResult);
        // }

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        this.gui.setupCamera();
        this.gui.renderCrosshair();

        // Enable alpha
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        this.fontRenderer.drawString("FPS: " + this.fps, 2, 2);
    }

    public void renderSelection(HitResult hitResult) {
        // TODO: Implement selection rendering with modern OpenGL
        // This would require a separate shader for line rendering
    }

    private HitResult getTargetBlock() {
        double yaw = Math.toRadians(-this.player.yaw + 90);
        double pitch = Math.toRadians(-this.player.pitch);

        double xzLen = Math.cos(pitch);
        double vectorX = xzLen * Math.cos(yaw);
        double vectorY = Math.sin(pitch);
        double vectorZ = xzLen * Math.sin(-yaw);

        double targetX = this.player.x - vectorX;
        double targetY = this.player.y + this.player.getEyeHeight() - 0.08D - vectorY;
        double targetZ = this.player.z - vectorZ;

        int shift = -1;

        int prevAirX = (int) (targetX < 0 ? targetX + shift : targetX);
        int prevAirY = (int) (targetY < 0 ? targetY + shift : targetY);
        int prevAirZ = (int) (targetZ < 0 ? targetZ + shift : targetZ);

        for (int i = 0; i < 800; i++) {
            targetX += vectorX / 10D;
            targetY += vectorY / 10D;
            targetZ += vectorZ / 10D;

            int hitX = (int) (targetX < 0 ? targetX + shift : targetX);
            int hitY = (int) (targetY < 0 ? targetY + shift : targetY);
            int hitZ = (int) (targetZ < 0 ? targetZ + shift : targetZ);

            EnumBlockFace targetFace = null;
            for (EnumBlockFace type : EnumBlockFace.values()) {
                if (prevAirX == hitX + type.x && prevAirY == hitY + type.y && prevAirZ == hitZ + type.z) {
                    targetFace = type;
                    break;
                }
            }

            if (this.world.isSolidBlockAt(hitX, hitY, hitZ)) {
                if (targetFace == null) {
                    return null;
                }
                return new HitResult(hitX, hitY, hitZ, targetFace);
            } else {
                prevAirX = (int) (targetX < 0 ? targetX + shift : targetX);
                prevAirY = (int) (targetY < 0 ? targetY + shift : targetY);
                prevAirZ = (int) (targetZ < 0 ? targetZ + shift : targetZ);
            }
        }
        return null;
    }

    public static void checkError() {
        int error = glGetError();
        if (error != 0) {
            System.err.println("OpenGL Error: " + error);
        }
    }

    public static void main(String[] args) {
        new Thread(new Minecraft(), "Game Thread").start();
    }
}
