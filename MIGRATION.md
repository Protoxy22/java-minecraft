# LWJGL 3.4.0 Migration Guide

## Overview

This document describes the migration from LWJGL 2.9.3 to LWJGL 3.4.0, replacing the fixed-function OpenGL pipeline with modern core-profile rendering using VAO/VBOs and shaders.

## Major Changes

### 1. Dependencies (build.gradle)

**Before (LWJGL 2):**
```gradle
dependencies {
    compile 'org.lwjgl.lwjgl:lwjgl:2.9.3'
    compile 'org.lwjgl.lwjgl:lwjgl_util:2.9.3'
}
```

**After (LWJGL 3.4.0):**
```gradle
dependencies {
    implementation platform("org.lwjgl:lwjgl-bom:3.4.0")
    implementation "org.lwjgl:lwjgl"
    implementation "org.lwjgl:lwjgl-glfw"
    implementation "org.lwjgl:lwjgl-opengl"
    implementation "org.lwjgl:lwjgl-stb"
    implementation "org.joml:joml:1.10.5"
    
    // Natives for all platforms
    runtimeOnly "org.lwjgl:lwjgl::natives-windows"
    runtimeOnly "org.lwjgl:lwjgl::natives-linux"
    runtimeOnly "org.lwjgl:lwjgl::natives-macos"
    // ... (similar for glfw, opengl, stb)
}
```

### 2. Window Management

**Before:** `Display` class from LWJGL 2
**After:** GLFW window management

```java
// Initialize GLFW
glfwInit();
long window = glfwCreateWindow(width, height, title, NULL, NULL);
glfwMakeContextCurrent(window);
GL.createCapabilities();
```

### 3. Input Handling

**Before:** `Keyboard` and `Mouse` static classes
**After:** GLFW callbacks via custom `Input` class

```java
// Set up callbacks
glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
    // Handle keyboard input
});

glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
    // Handle mouse input
});
```

### 4. Rendering Pipeline

#### Old: Fixed-Function Pipeline
```java
GL11.glMatrixMode(GL11.GL_PROJECTION);
GL11.glLoadIdentity();
GLU.gluPerspective(fov, aspect, near, far);
GL11.glMatrixMode(GL11.GL_MODELVIEW);
GL11.glLoadIdentity();
GL11.glTranslatef(x, y, z);
GL11.glRotatef(angle, 1, 0, 0);
```

#### New: Modern Shader-Based Pipeline
```java
// Create projection and view matrices
Matrix4f projection = new Matrix4f().perspective(fov, aspect, near, far);
Matrix4f view = new Matrix4f().identity()
    .rotateX(pitch)
    .rotateY(yaw)
    .translate(-x, -y, -z);

// Upload to shader
shader.bind();
shader.setUniform("uProjection", projection);
shader.setUniform("uView", view);
```

### 5. Mesh Rendering

#### Old: Tessellator + Display Lists
```java
Tessellator tessellator = Tessellator.instance;
tessellator.startDrawingQuads();
tessellator.addVertexWithUV(x, y, z, u, v);
tessellator.draw();

// Display lists for chunks
int listId = GL11.glGenLists(1);
GL11.glNewList(listId, GL11.GL_COMPILE);
// ... render geometry
GL11.glEndList();
GL11.glCallList(listId);
```

#### New: MeshBuilder + VAO/VBO
```java
MeshBuilder builder = MeshBuilder.instance;
builder.begin();
builder.pos(x, y, z);
builder.tex(u, v);
builder.color(r, g, b, a);
builder.vertex();
builder.end();
builder.draw();

// Chunk meshes stored in VBOs
ChunkMesh mesh = new ChunkMesh();
FloatBuffer data = buildMeshData();
mesh.upload(data, vertexCount);
mesh.render();
```

### 6. Texture Loading

**Before:** `ImageIO` + manual pixel conversion
**After:** STB Image library

```java
// Load with STB
ByteBuffer image = STBImage.stbi_load_from_memory(
    imageBuffer, w, h, comp, 4);
glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 
    0, GL_RGBA, GL_UNSIGNED_BYTE, image);
STBImage.stbi_image_free(image);
```

### 7. Shaders

Two shader programs were added:

**World Shader** (`world.vert`, `world.frag`):
- Handles block rendering with fog
- Uniforms: projection, view, model matrices, texture, fog parameters

**GUI Shader** (`gui.vert`, `gui.frag`):
- Handles 2D GUI rendering
- Uniforms: orthographic projection, texture, use texture flag

## New Classes

1. **`Camera`**: Manages projection and view matrices using JOML
2. **`Shader`**: Loads and manages GLSL shaders
3. **`MeshBuilder`**: Modern replacement for Tessellator using VAO/VBO
4. **`ChunkMesh`**: VBO-based chunk storage
5. **`Input`**: GLFW input management

## Updated Classes

1. **`MinecraftWindow`**: Now uses GLFW instead of Display
2. **`Minecraft`**: Updated for GLFW main loop and modern rendering
3. **`WorldRenderer`**: Uses shaders and camera for rendering
4. **`BlockRenderer`**: Builds mesh data into buffers instead of immediate mode
5. **`ChunkSection`**: Stores geometry in VBOs instead of display lists
6. **`Frustum`**: Uses JOML matrices instead of extracting from OpenGL
7. **`Player`**: Uses GLFW key constants
8. **`GuiRenderer`**: Uses modern rendering with shaders
9. **`FontRenderer`**: Uses MeshBuilder instead of Tessellator
10. **`TextureManager`**: Uses STB for image loading

## Building and Running

### Requirements
- Java 11 or higher
- Gradle 6.0 or higher

### Build Commands
```bash
# Build the project
./gradlew build

# Run the game
./gradlew run

# Or use the custom task
./gradlew runGame
```

### Platform-Specific Notes

**Windows:**
- No special configuration needed

**macOS:**
- JVM argument `-XstartOnFirstThread` is automatically added
- Supports both Intel and Apple Silicon (ARM)

**Linux:**
- Supports x86-64, ARM64, and ARM32

## API Mappings

### Display Management
| LWJGL 2 | LWJGL 3 (GLFW) |
|---------|----------------|
| `Display.create()` | `glfwCreateWindow()` |
| `Display.update()` | `glfwSwapBuffers()` + `glfwPollEvents()` |
| `Display.destroy()` | `glfwDestroyWindow()` |
| `Display.isCloseRequested()` | `glfwWindowShouldClose()` |
| `Display.setFullscreen()` | `glfwSetWindowMonitor()` |

### Input
| LWJGL 2 | LWJGL 3 (GLFW) |
|---------|----------------|
| `Keyboard.isKeyDown(key)` | Custom `Input.isKeyDown(GLFW_KEY_*)` |
| `Mouse.isButtonDown(btn)` | Custom `Input.isMouseButtonDown(btn)` |
| `Mouse.getDX()` | Custom `Input.getMouseDeltaX()` |
| `Mouse.setGrabbed(true)` | `glfwSetInputMode(GLFW_CURSOR, GLFW_CURSOR_DISABLED)` |

### OpenGL
| LWJGL 2 (Fixed) | LWJGL 3 (Modern) |
|-----------------|-------------------|
| `glBegin/glEnd` | VAO/VBO + `glDrawArrays` |
| `glMatrixMode/glLoadIdentity` | JOML matrices + uniform uploads |
| `gluPerspective` | `Matrix4f.perspective()` |
| Display lists | VBOs |
| `GL11.GL_QUADS` | `GL11.GL_TRIANGLES` (quads converted to triangles) |

## Known Limitations

1. **Selection Box Rendering**: Currently disabled, needs separate line rendering shader
2. **Fog**: Now implemented in fragment shader instead of fixed-function fog
3. **No Legacy Support**: Requires OpenGL 3.3+ core profile

## Performance Notes

- VBOs provide better performance than display lists on modern hardware
- Chunk meshes are built on-demand and cached in GPU memory
- All geometry is uploaded once and reused until chunk changes
- Modern shader-based rendering allows for more advanced effects

## Troubleshooting

### Build Fails
- Ensure Java 11+ is installed
- Run `./gradlew clean build`

### Black Screen
- Check OpenGL version: Core profile 3.3+ required
- Update graphics drivers

### Crash on macOS
- Ensure `-XstartOnFirstThread` JVM argument is present
- Check console for GLFW/OpenGL errors

### Performance Issues
- Modern OpenGL should be faster than LWJGL 2
- If slower, check if discrete GPU is being used
- Monitor with F3 debug info

## Future Enhancements

Possible improvements for the future:
1. Instanced rendering for particles/entities
2. Compute shaders for world generation
3. PBR materials and lighting
4. Ambient occlusion
5. Water reflections/refractions
6. Shadow mapping
7. Post-processing effects (bloom, SSAO, etc.)
