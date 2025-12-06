package de.labystudio.game.render.world;

import de.labystudio.game.util.BoundingBox;
import de.labystudio.game.util.EnumBlockFace;
import de.labystudio.game.world.block.Block;

import java.nio.FloatBuffer;

public class BlockRenderer {

    public static final boolean CLASSIC_LIGHTNING = false;

    /**
     * Build mesh data for a block into the provided buffer
     * @return number of vertices added
     */
    public int buildBlockMesh(IWorldAccess world, Block block, int x, int y, int z, FloatBuffer buffer) {
        BoundingBox boundingBox = block.getBoundingBox(world, x, y, z);
        int vertexCount = 0;

        // Render faces
        for (EnumBlockFace face : EnumBlockFace.values()) {
            if (block.shouldRenderFace(world, x, y, z, face)) {
                vertexCount += buildFaceMesh(world, block, boundingBox, face, x, y, z, buffer);
            }
        }
        
        return vertexCount;
    }

    private int buildFaceMesh(IWorldAccess world, Block block, BoundingBox boundingBox, EnumBlockFace face, int x, int y, int z, FloatBuffer buffer) {

        // Vertex mappings
        double minX = x + boundingBox.minX;
        double minY = y + boundingBox.minY;
        double minZ = z + boundingBox.minZ;
        double maxX = x + boundingBox.maxX;
        double maxY = y + boundingBox.maxY;
        double maxZ = z + boundingBox.maxZ;

        // UV Mapping
        int textureIndex = block.getTextureForFace(face);
        float minU = (textureIndex % 16) / 16.0F;
        float maxU = minU + (16 / 256F);
        float minV = (float) (textureIndex / 16);
        float maxV = minV + (16 / 256F);

        // Build quad as two triangles (6 vertices)
        if (face == EnumBlockFace.BOTTOM) {
            addBlockCorner(world, face, minX, minY, maxZ, minU, maxV, buffer);
            addBlockCorner(world, face, minX, minY, minZ, minU, minV, buffer);
            addBlockCorner(world, face, maxX, minY, minZ, maxU, minV, buffer);
            
            addBlockCorner(world, face, minX, minY, maxZ, minU, maxV, buffer);
            addBlockCorner(world, face, maxX, minY, minZ, maxU, minV, buffer);
            addBlockCorner(world, face, maxX, minY, maxZ, maxU, maxV, buffer);
        } else if (face == EnumBlockFace.TOP) {
            addBlockCorner(world, face, maxX, maxY, maxZ, maxU, maxV, buffer);
            addBlockCorner(world, face, maxX, maxY, minZ, maxU, minV, buffer);
            addBlockCorner(world, face, minX, maxY, minZ, minU, minV, buffer);
            
            addBlockCorner(world, face, maxX, maxY, maxZ, maxU, maxV, buffer);
            addBlockCorner(world, face, minX, maxY, minZ, minU, minV, buffer);
            addBlockCorner(world, face, minX, maxY, maxZ, minU, maxV, buffer);
        } else if (face == EnumBlockFace.EAST) {
            addBlockCorner(world, face, minX, maxY, minZ, maxU, minV, buffer);
            addBlockCorner(world, face, maxX, maxY, minZ, minU, minV, buffer);
            addBlockCorner(world, face, maxX, minY, minZ, minU, maxV, buffer);
            
            addBlockCorner(world, face, minX, maxY, minZ, maxU, minV, buffer);
            addBlockCorner(world, face, maxX, minY, minZ, minU, maxV, buffer);
            addBlockCorner(world, face, minX, minY, minZ, maxU, maxV, buffer);
        } else if (face == EnumBlockFace.WEST) {
            addBlockCorner(world, face, minX, maxY, maxZ, minU, minV, buffer);
            addBlockCorner(world, face, minX, minY, maxZ, minU, maxV, buffer);
            addBlockCorner(world, face, maxX, minY, maxZ, maxU, maxV, buffer);
            
            addBlockCorner(world, face, minX, maxY, maxZ, minU, minV, buffer);
            addBlockCorner(world, face, maxX, minY, maxZ, maxU, maxV, buffer);
            addBlockCorner(world, face, maxX, maxY, maxZ, maxU, minV, buffer);
        } else if (face == EnumBlockFace.NORTH) {
            addBlockCorner(world, face, minX, maxY, maxZ, maxU, minV, buffer);
            addBlockCorner(world, face, minX, maxY, minZ, minU, minV, buffer);
            addBlockCorner(world, face, minX, minY, minZ, minU, maxV, buffer);
            
            addBlockCorner(world, face, minX, maxY, maxZ, maxU, minV, buffer);
            addBlockCorner(world, face, minX, minY, minZ, minU, maxV, buffer);
            addBlockCorner(world, face, minX, minY, maxZ, maxU, maxV, buffer);
        } else if (face == EnumBlockFace.SOUTH) {
            addBlockCorner(world, face, maxX, minY, maxZ, minU, maxV, buffer);
            addBlockCorner(world, face, maxX, minY, minZ, maxU, maxV, buffer);
            addBlockCorner(world, face, maxX, maxY, minZ, maxU, minV, buffer);
            
            addBlockCorner(world, face, maxX, minY, maxZ, minU, maxV, buffer);
            addBlockCorner(world, face, maxX, maxY, minZ, maxU, minV, buffer);
            addBlockCorner(world, face, maxX, maxY, maxZ, minU, minV, buffer);
        }
        
        return 6; // 6 vertices per face (2 triangles)
    }


    private void addBlockCorner(IWorldAccess world, EnumBlockFace face, double x, double y, double z, float u, float v, FloatBuffer buffer) {
        // Calculate color
        float r, g, b, a;
        
        if (CLASSIC_LIGHTNING) {
            float brightness = 0.9F / 15.0F * world.getLightAt((int) x + face.x, (int) y + face.y, (int) z + face.z) + 0.1F;
            float color = brightness * face.getShading();
            r = g = b = color;
            a = 1.0f;
        } else {
            // Smooth lightning
            int lightLevelAtThisCorner = getAverageLightLevelAt(world, (int) x, (int) y, (int) z);
            float brightness = 0.9F / 15.0F * lightLevelAtThisCorner + 0.1F;
            float color = brightness * face.getShading();
            r = g = b = color;
            a = 1.0f;
        }
        
        // Add vertex: position (3) + texcoord (2) + color (4)
        buffer.put((float) x).put((float) y).put((float) z);
        buffer.put(u).put(v);
        buffer.put(r).put(g).put(b).put(a);
    }

    private int getAverageLightLevelAt(IWorldAccess world, int x, int y, int z) {
        int totalLightLevel = 0;
        int totalBlocks = 0;

        // For all blocks around this corner
        for (int offsetX = -1; offsetX <= 0; offsetX++) {
            for (int offsetY = -1; offsetY <= 0; offsetY++) {
                for (int offsetZ = -1; offsetZ <= 0; offsetZ++) {
                    short typeId = world.getBlockAt(x + offsetX, y + offsetY, z + offsetZ);

                    // Does it contain air?
                    if (typeId == 0 || Block.getById(typeId).isTransparent()) {

                        // Sum up the light levels
                        totalLightLevel += world.getLightAt(x + offsetX, y + offsetY, z + offsetZ);
                        totalBlocks++;
                    }
                }
            }
        }

        // Calculate the average light level of all surrounding blocks
        return totalBlocks == 0 ? 0 : totalLightLevel / totalBlocks;
    }
}
