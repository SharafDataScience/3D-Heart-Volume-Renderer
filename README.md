# 3D Heart Volume Renderer

This project visualizes a 3D heart-shaped object using Java by evaluating an implicit equation across a volumetric grid and rendering a 2D projection using ray casting.

---

## Features

- Generates a 3D heart shape using a mathematical function
- Uses ray casting to generate a 2D isosurface image
- Outputs a `.tiff` image file
- Accepts grid and image size via command-line arguments
- Basic surface shading using gradients

---

## How It Works

### 1. Volume Generation

A 3D grid of voxels is allocated and filled using a mathematical heart equation.

```java
this.data = new int[gridSize][gridSize][gridSize];
