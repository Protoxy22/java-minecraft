package de.labystudio.game.world;

import de.labystudio.game.render.Frustum;
import de.labystudio.game.render.gl.Camera;
import de.labystudio.game.render.gl.Shader;
import de.labystudio.game.render.world.BlockRenderer;
import de.labystudio.game.util.EnumWorldBlockLayer;
import de.labystudio.game.util.TextureManager;
import de.labystudio.game.world.chunk.Chunk;
import de.labystudio.game.world.chunk.ChunkSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class WorldRenderer {

    public static final int RENDER_DISTANCE = 8;

    public final int textureId = TextureManager.loadTexture("/terrain.png", GL_NEAREST);

    private final World world;

    private final BlockRenderer blockRenderer = new BlockRenderer();
    private final Frustum frustum = new Frustum();
    private final List<ChunkSection> chunkSectionUpdateQueue = new ArrayList<>();
    
    private Shader worldShader;
    private Camera camera;
    
    // Fog settings
    private float fogStart;
    private float fogEnd;
    private float fogR = 0.6222222F - 0.05F;
    private float fogG = 0.5F + 0.1F;
    private float fogB = 1.0F;
    private boolean fogEnabled = true;

    public WorldRenderer(World world) {
        this.world = world;

        // Sky color
        glClearColor(0.6222222F - 0.05F, 0.5F + 0.1F, 1.0F, 0.0F);
        glClearDepth(1.0D);

        // Render methods
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        
        // Load shader
        worldShader = new Shader("/shaders/world.vert", "/shaders/world.frag");
    }
    
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setupFog(boolean inWater) {
        if (inWater) {
            fogStart = 0;
            fogEnd = 10.0F;
            fogR = 0.2F;
            fogG = 0.2F;
            fogB = 0.4F;
        } else {
            int viewDistance = WorldRenderer.RENDER_DISTANCE * ChunkSection.SIZE;
            fogStart = viewDistance / 4.0F;
            fogEnd = viewDistance;
            fogR = 0.6222222F - 0.05F;
            fogG = 0.5F + 0.1F;
            fogB = 1.0F;
        }
        fogEnabled = true;
    }

    public void onTick() {

    }

    public void render(int cameraChunkX, int cameraChunkZ, EnumWorldBlockLayer renderLayer) {
        if (camera == null) {
            return;
        }
        
        this.frustum.setMatrices(camera.getProjectionMatrix(), camera.getViewMatrix());
        this.frustum.calculateFrustum();
        
        // Bind shader and set uniforms
        worldShader.bind();
        worldShader.setUniform("uProjection", camera.getProjectionMatrix());
        worldShader.setUniform("uView", camera.getViewMatrix());
        worldShader.setUniform("uModel", camera.getModelMatrix());
        worldShader.setUniform("uTexture", 0);
        worldShader.setUniform("uFogColor", fogR, fogG, fogB, 1.0f);
        worldShader.setUniform("uFogStart", fogStart);
        worldShader.setUniform("uFogEnd", fogEnd);
        worldShader.setUniform("uFogEnabled", fogEnabled ? 1 : 0);
        
        // Bind texture
        glBindTexture(GL_TEXTURE_2D, textureId);

        for (Chunk chunk : this.world.chunks.values()) {
            int distanceX = Math.abs(cameraChunkX - chunk.getX());
            int distanceZ = Math.abs(cameraChunkZ - chunk.getZ());

            // Is in camera view
            if (distanceX < RENDER_DISTANCE && distanceZ < RENDER_DISTANCE && this.frustum.cubeInFrustum(chunk)) {

                // For all chunk sections
                for (ChunkSection chunkSection : chunk.getSections()) {
                    // Render chunk section
                    chunkSection.render(renderLayer);

                    // Queue for rebuild
                    if (chunkSection.isQueuedForRebuild() && !this.chunkSectionUpdateQueue.contains(chunkSection)) {
                        this.chunkSectionUpdateQueue.add(chunkSection);
                    }
                }
            }
        }
        
        worldShader.unbind();

        // Sort update queue, chunk sections that are closer to the camera get a higher priority
        Collections.sort(this.chunkSectionUpdateQueue, (section1, section2) -> {
            int distance1 = (int) (Math.pow(section1.x - cameraChunkX, 2) + Math.pow(section1.z - cameraChunkZ, 2));
            int distance2 = (int) (Math.pow(section2.x - cameraChunkX, 2) + Math.pow(section2.z - cameraChunkZ, 2));
            return Integer.compare(distance1, distance2);
        });

        // Rebuild one chunk per frame
        if (!this.chunkSectionUpdateQueue.isEmpty()) {
            ChunkSection chunkSection = this.chunkSectionUpdateQueue.remove(0);
            if (chunkSection != null) {
                chunkSection.rebuild(this);
            }
        }
    }

    public BlockRenderer getBlockRenderer() {
        return this.blockRenderer;
    }
    
    public void cleanup() {
        if (worldShader != null) {
            worldShader.delete();
        }
    }
}
