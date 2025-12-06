package de.labystudio.game.render.gl;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    
    private final Matrix4f projectionMatrix;
    private final Matrix4f viewMatrix;
    private final Matrix4f modelMatrix;
    
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;
    
    private final Vector3f position;
    private float pitch;
    private float yaw;
    
    public Camera(float fov, float aspectRatio, float nearPlane, float farPlane) {
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.modelMatrix = new Matrix4f();
        
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
        
        this.position = new Vector3f();
        this.pitch = 0;
        this.yaw = 0;
        
        updateProjectionMatrix();
    }
    
    public void updateProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective((float) Math.toRadians(fov), aspectRatio, nearPlane, farPlane);
    }
    
    public void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.rotateX((float) Math.toRadians(pitch));
        viewMatrix.rotateY((float) Math.toRadians(yaw));
        viewMatrix.translate(-position.x, -position.y, -position.z);
    }
    
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }
    
    public void setRotation(float pitch, float yaw) {
        this.pitch = pitch;
        this.yaw = yaw;
    }
    
    public void setFov(float fov) {
        this.fov = fov;
        updateProjectionMatrix();
    }
    
    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        updateProjectionMatrix();
    }
    
    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
        updateProjectionMatrix();
    }
    
    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }
    
    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }
    
    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }
    
    public Vector3f getPosition() {
        return position;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public float getYaw() {
        return yaw;
    }
}
