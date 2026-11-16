# Java Image Processor

A desktop image processing application written in Java using Swing and AWT (`BufferedImage`, `Graphics2D`). The project demonstrates raster image manipulation, convolution kernels, lookup tables, affine transformations, and state history management.

## Features

- **File Operations:** Open standard image formats via `JFileChooser` and export processed images to PNG (`ImageIO`) with automatic `.png` file extension appending.
- **Convolution Filters (`ConvolveOp`):**
  - **Blur:** 3x3 box blur smoothing filter.
  - **Sharpen:** High-pass sharpening kernel.
  - **Edge Detection:** Directional edge detection filter.
- **Color & Tone Adjustments:**
  - **Brighten:** Linear pixel intensity scaling (`RescaleOp`).
  - **Negative:** Color inversion using a 256-entry `ByteLookupTable` (`LookupOp`).
- **Geometric Transformations:** 90-degree clockwise image rotation using `AffineTransform` with bicubic interpolation.
- **Undo History:** Multi-step state restoration backed by a `Stack<BufferedImage>` with deep raster copying.

## Technologies Used

- **Java** (JDK 17+)
- **Java Swing & AWT** (`JFrame`, `JMenuBar`, `JFileChooser`, `BufferedImage`, `Graphics2D`, `ConvolveOp`, `LookupOp`, `RescaleOp`, `AffineTransform`)

## How to Run

1. Clone the repository:
   ```bash
   git clone [https://github.com/OnlyBart123/ImageProcessing.git](https://github.com/OnlyBart123/ImageProcessing.git)
   ```
2. Navigate to the `src` directory and compile the source file:
   ```bash
   javac ImageProcessingFrame.java
   ```
3. Run the application:
   ```bash
   java ImageProcessingFrame
   ```