package de.labystudio.game.render.gui;

import de.labystudio.game.MinecraftWindow;
import de.labystudio.game.render.gl.MeshBuilder;
import de.labystudio.game.render.gl.Shader;
import de.labystudio.game.util.TextureManager;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class GuiRenderer {

    private int textureId;
    private final int zLevel = 0;

    private int scaleFactor;
    private int width;
    private int height;
    
    private Shader guiShader;
    private Matrix4f projectionMatrix;

    public void loadTextures() {
        this.textureId = TextureManager.loadTexture("/icons.png", GL_NEAREST);
        this.guiShader = new Shader("/shaders/gui.vert", "/shaders/gui.frag");
        this.projectionMatrix = new Matrix4f();
    }

    public void init(MinecraftWindow gameWindow) {
        this.width = gameWindow.displayWidth;
        this.height = gameWindow.displayHeight;
        for (this.scaleFactor = 1; this.width / (this.scaleFactor + 1) >= 320 && this.height / (this.scaleFactor + 1) >= 240; this.scaleFactor++) {
        }
        this.width = this.width / this.scaleFactor;
        this.height = this.height / this.scaleFactor;
        
        // Update orthographic projection
        updateProjection();
    }
    
    private void updateProjection() {
        projectionMatrix.identity();
        projectionMatrix.ortho(0, width, height, 0, -1, 1);
    }

    public void setupCamera() {
        // Update projection in case window was resized
        updateProjection();
    }

    public void renderCrosshair() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE_MINUS_DST_COLOR, GL_ONE_MINUS_SRC_COLOR);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, this.textureId);
        
        // Use gui shader
        guiShader.bind();
        guiShader.setUniform("uProjection", projectionMatrix);
        guiShader.setUniform("uTexture", 0);
        guiShader.setUniform("uUseTexture", 1);
        
        this.drawTexturedModalRect(this.width / 2 - 7, this.height / 2 - 7, 0, 0, 16, 16);
        
        guiShader.unbind();

        glDisable(GL_BLEND);
    }

    public void drawTexturedModalRect(int left, int top, int offsetX, int offsetY, int width, int height) {
        MeshBuilder builder = MeshBuilder.instance;
        builder.begin();
        this.drawTexturedModalRect(builder, left, top, offsetX, offsetY, width, height, width, height, 256, 256);
        builder.end();
        builder.draw();
    }

    public void drawTexturedModalRect(MeshBuilder builder, int x, int y,
                                      int u, int v, int uWidth, int vHeight,
                                      int width, int height,
                                      float bitMapWidth, float bitmapHeight) {

        float factorX = 1.0F / bitMapWidth;
        float factorY = 1.0F / bitmapHeight;
        
        builder.color(1, 1, 1, 1);

        // Triangle 1
        builder.pos(x, y + height, 0);
        builder.tex(u * factorX, (v + vHeight) * factorY);
        builder.vertex();
        
        builder.pos(x + width, y + height, 0);
        builder.tex((u + uWidth) * factorX, (v + vHeight) * factorY);
        builder.vertex();
        
        builder.pos(x + width, y, 0);
        builder.tex((u + uWidth) * factorX, v * factorY);
        builder.vertex();
        
        // Triangle 2
        builder.pos(x, y + height, 0);
        builder.tex(u * factorX, (v + vHeight) * factorY);
        builder.vertex();
        
        builder.pos(x + width, y, 0);
        builder.tex((u + uWidth) * factorX, v * factorY);
        builder.vertex();
        
        builder.pos(x, y, 0);
        builder.tex(u * factorX, v * factorY);
        builder.vertex();
    }
    
    public void cleanup() {
        if (guiShader != null) {
            guiShader.delete();
        }
    }
}
