# Minecraft Recode in Java - LWJGL 3.4.0

This is a sandbox that provides all basic features of Minecraft using **modern OpenGL with LWJGL 3.4.0**.<br>
The main purpose of this project is to understand the render and physics engine of Minecraft.<br>
It is a fork of the [first version](https://github.com/thecodeofnotch/rd-131655) of Minecraft, migrated to LWJGL 3.

This project was originally created by [LabyStudio](https://github.com/LabyStudio/java-minecraft) and ported to javascript: [js-minecraft](https://github.com/LabyStudio/js-minecraft).

## Recent Changes: LWJGL 3.4.0 Migration

This version has been fully migrated from LWJGL 2 to LWJGL 3.4.0 with the following improvements:

- ✅ **GLFW Window Management**: Replaced Display with GLFW
- ✅ **Modern OpenGL**: VAO/VBO rendering with shaders (Core Profile 3.3+)
- ✅ **Shader-Based Rendering**: Custom GLSL shaders for world and GUI
- ✅ **JOML Matrices**: Mathematical operations using JOML library
- ✅ **STB Image Loading**: Native image loading via STB
- ✅ **Multi-Platform Support**: Windows, macOS (Intel & ARM), and Linux (x86-64, ARM64, ARM32)

See [MIGRATION.md](MIGRATION.md) for detailed migration notes.

## Feature Overview

- Block rendering with modern OpenGL shaders
- Block collision and physics
- Player movement
    - Walking
    - Sprinting
    - Sneaking
    - Flying
    - Swimming
- Dynamic and smooth lighting
- Anvil world loading/saving
- Perlin world generation
- Frustum culling
- Shader-based fog system
- Underwater effects
- HUD rendering with custom GUI shader
    - Cross-hair
    - Font rendering
- Dynamic FOV

![Ingame](.artwork/ingame.png)

## Requirements

- **Java 11 or higher**
- **OpenGL 3.3+ compatible graphics card**
- **Gradle 6.0+** (included via wrapper)

## Building and Running

### Quick Start

```bash
# Clone the repository
git clone https://github.com/Protoxy22/java-minecraft.git
cd java-minecraft

# Build the project
./gradlew build

# Run the game
./gradlew run
```

### Alternative Run Methods

```bash
# Using the custom runGame task
./gradlew runGame

# Or manually with Java (after build)
cd run
java -cp ../build/libs/java-minecraft-1.0-SNAPSHOT.jar de.labystudio.game.Minecraft
```

### Platform-Specific Notes

**Windows:**
```bash
gradlew.bat build
gradlew.bat run
```

**macOS:**
- The project automatically adds the `-XstartOnFirstThread` JVM argument required for macOS
- Supports both Intel and Apple Silicon Macs

**Linux:**
- Ensure you have OpenGL 3.3+ drivers installed
- For Wayland users: may need to set `GLFW_PLATFORM=x11`

## Controls

### Movement
```
W or ↑     : Forward
S or ↓     : Backwards
A or ←     : Left
D or →     : Right
Space      : Jump
Double Space : Toggle flying
Q          : Sneaking
Shift      : Sprinting
```

### Interaction
```
Left Click   : Destroy block
Right Click  : Place block
Middle Click : Pick block
```

### System
```
R          : Return to spawn
ESC        : Toggle game focus / Pause
F11        : Toggle fullscreen
Enter      : Save world
```

## Project Structure

```
src/main/
├── java/de/labystudio/game/
│   ├── Minecraft.java              # Main game loop (GLFW)
│   ├── MinecraftWindow.java        # Window management
│   ├── input/
│   │   └── Input.java              # GLFW input handling
│   ├── render/
│   │   ├── gl/
│   │   │   ├── Camera.java         # Camera with JOML matrices
│   │   │   ├── Shader.java         # Shader management
│   │   │   ├── MeshBuilder.java    # VAO/VBO mesh builder
│   │   │   └── ChunkMesh.java      # VBO-based chunk storage
│   │   ├── world/
│   │   │   └── BlockRenderer.java  # Block mesh generation
│   │   └── gui/
│   │       ├── GuiRenderer.java    # GUI with shaders
│   │       └── FontRenderer.java   # Text rendering
│   ├── world/
│   │   ├── World.java              # World management
│   │   ├── WorldRenderer.java      # World rendering with shaders
│   │   └── chunk/
│   │       ├── Chunk.java          # Chunk container
│   │       └── ChunkSection.java   # 16x16x16 chunk section
│   └── player/
│       └── Player.java             # Player entity
└── resources/
    ├── shaders/
    │   ├── world.vert              # World vertex shader
    │   ├── world.frag              # World fragment shader
    │   ├── gui.vert                # GUI vertex shader
    │   └── gui.frag                # GUI fragment shader
    ├── terrain.png                 # Block textures
    ├── icons.png                   # UI elements
    └── font.png                    # Font texture
```

## Technical Details

### Rendering Pipeline

1. **Modern OpenGL Core Profile 3.3+**
   - No fixed-function pipeline
   - All rendering via shaders
   - VAO/VBO for geometry
   - Uniform matrices for transformations

2. **Chunk System**
   - World divided into 16x16x16 sections
   - Each section has separate meshes for solid and transparent blocks
   - Frustum culling for efficient rendering
   - Only visible faces are meshed

3. **Shaders**
   - World shader: Handles 3D geometry, lighting, and fog
   - GUI shader: Handles 2D overlays and text

### Dependencies

- **LWJGL 3.4.0**: OpenGL, GLFW, STB bindings
- **JOML 1.10.5**: Mathematics library for matrices and vectors
- **OpenNBT 1.3**: NBT serialization for world saves

## Known Issues

- Selection box rendering temporarily disabled (needs line rendering shader)
- Mouse over block calculation can be imprecise in some cases
- No light updates during initial world generation

## Planned Features

- Complete selection box rendering
- Infinite world generation (generate chunks on-demand)
- Multiplayer support
- Entity rendering
- Loading screen
- Advanced shader effects (shadows, ambient occlusion)

## Performance

The LWJGL 3 version generally provides better performance than the LWJGL 2 version due to:
- Modern VBO-based rendering
- Efficient GPU memory usage
- Optimized chunk meshing
- Hardware-accelerated shaders

Expected FPS: 200-500+ on modern hardware (depending on view distance and chunk complexity)

## Troubleshooting

### Game Won't Start
- Verify Java 11+ is installed: `java -version`
- Ensure OpenGL 3.3+ support: update graphics drivers
- Check console for error messages

### Low FPS
- Reduce view distance (modify `RENDER_DISTANCE` in `WorldRenderer.java`)
- Ensure discrete GPU is being used (check graphics control panel)
- Update graphics drivers

### Black Screen
- OpenGL 3.3+ required - update drivers
- On Linux: ensure proper OpenGL drivers are installed

### macOS Issues
- If crash on startup, ensure `-XstartOnFirstThread` is in JVM args
- May need to add `NSHighResolutionCapable` to Info.plist for Retina displays

## Development

### Building from Source
```bash
./gradlew clean build
```

### Running Tests
```bash
./gradlew test
```

### Generate IDE Project Files
```bash
# IntelliJ IDEA
./gradlew idea

# Eclipse
./gradlew eclipse
```

## Credits

- Original concept by Notch
- Java implementation by [LabyStudio](https://github.com/LabyStudio)
- LWJGL 3.4.0 migration by contributors

## License

This project is for educational purposes only.

**NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG.**

## Links

- [Original LWJGL 2 Version](https://github.com/LabyStudio/java-minecraft)
- [JavaScript Port](https://github.com/LabyStudio/js-minecraft)
- [LWJGL](https://www.lwjgl.org/)
- [JOML](https://github.com/JOML-CI/JOML)
