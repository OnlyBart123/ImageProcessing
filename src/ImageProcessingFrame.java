import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;

public class ImageProcessingFrame extends JFrame
{
    private static final int DEFAULT_WIDTH = 700;
    private static final int DEFAULT_HEIGHT = 700;
    private BufferedImage image;
    private BufferedImage originalImage;
    private Stack<BufferedImage> undoStack;

    public ImageProcessingFrame()
    {
        setTitle("ImageProcessing");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        undoStack = new Stack<>();

        add(new JComponent()
        {
            public void paintComponent(Graphics g)
            {
                if (image != null) g.drawImage(image, 0, 0, null);
            }
        });

        var fileMenu = new JMenu("File");
        var openItem = new JMenuItem("Open");
        openItem.addActionListener(event -> openFile());
        fileMenu.add(openItem);

        var saveItem = new JMenuItem("Save");
        saveItem.addActionListener(event -> saveFile());
        fileMenu.add(saveItem);

        var exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(event -> System.exit(0));
        fileMenu.add(exitItem);

        var editMenu = new JMenu("Edit");

        var undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(event -> undo());
        editMenu.add(undoItem);

        var blurItem = new JMenuItem("Blur");
        blurItem.addActionListener(event ->
        {
            float weight = 1.0f / 9.0f;
            float[] elements = new float[9];
            for (int i = 0; i < 9; i++)
                elements[i] = weight;
            applyTransformation(new ConvolveOp(new Kernel(3, 3, elements)));
        });
        editMenu.add(blurItem);

        var sharpenItem = new JMenuItem("Sharpen");
        sharpenItem.addActionListener(event ->
        {
            float[] elements = { 0.0f, -1.0f, 0.0f, -1.0f, 5.f, -1.0f, 0.0f, -1.0f, 0.0f };
            applyTransformation(new ConvolveOp(new Kernel(3, 3, elements)));
        });
        editMenu.add(sharpenItem);

        var brightenItem = new JMenuItem("Brighten");
        brightenItem.addActionListener(event ->
        {
            float a = 1.1f;
            float b = 20.0f;
            var op = new RescaleOp(a, b, null);
            applyTransformation(op);
        });
        editMenu.add(brightenItem);

        var edgeDetectItem = new JMenuItem("Edge detect");
        edgeDetectItem.addActionListener(event ->
        {
            float[] elements = { -1.0f, 0.0f, 1.0f, -2.0f, 0.0f, 2.0f, -1.0f, 0.0f, 1.0f };
            applyTransformation(new ConvolveOp(new Kernel(3, 3, elements)));
        });
        editMenu.add(edgeDetectItem);

        var negativeItem = new JMenuItem("Negative");
        negativeItem.addActionListener(event ->
        {
            byte[] negative = new byte[256];
            for (int i = 0; i < 256; i++) {
                negative[i] = (byte) (255 - i);
            }
            var table = new ByteLookupTable(0, negative);
            var op = new LookupOp(table, null);
            applyTransformation(op);
        });
        editMenu.add(negativeItem);

        var rotateItem = new JMenuItem("Rotate");
        rotateItem.addActionListener(event ->
        {
            if (image == null) return;

            int w = image.getWidth();
            int h = image.getHeight();


            int type = image.getType() == 0 ? BufferedImage.TYPE_INT_RGB : image.getType();
            BufferedImage rotatedImage = new BufferedImage(h, w, type);

            Graphics2D g2d = rotatedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);


            AffineTransform at = new AffineTransform();
            at.translate(h / 2.0, w / 2.0);
            at.rotate(Math.toRadians(90));
            at.translate(-w / 2.0, -h / 2.0);

            g2d.drawImage(image, at, null);
            g2d.dispose();


            undoStack.push(deepCopy(image));
            image = rotatedImage;
            repaint();
            getEditMenu().getItem(0).setEnabled(true);
        });
        editMenu.add(rotateItem);

        var menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        setJMenuBar(menuBar);
    }

    public void openFile() {
        var chooser = new JFileChooser(".");
        chooser.setCurrentDirectory(new File("."));
        String[] extensions = ImageIO.getReaderFileSuffixes();
        chooser.setFileFilter(new FileNameExtensionFilter("Image files", extensions));
        int r = chooser.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        try {
            originalImage = ImageIO.read(chooser.getSelectedFile());
            image = deepCopy(originalImage);
            repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    public void saveFile() {
        var chooser = new JFileChooser(".");
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Save Image");
        int r = chooser.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        try {
            File outputFile = chooser.getSelectedFile();
            if (!outputFile.getName().contains(".")) {
                outputFile = new File(outputFile.getAbsolutePath() + ".png");
            }
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private void applyTransformation(BufferedImageOp op) {
        if (image == null) return;
        undoStack.push(deepCopy(image));
        image = op.filter(image, null);
        repaint();
        getEditMenu().getItem(0).setEnabled(true);
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            image = undoStack.pop();
            repaint();
        }
        if (undoStack.isEmpty()) {
            getEditMenu().getItem(0).setEnabled(false);
        }
    }

    private JMenu getEditMenu() {
        JMenuBar menuBar = getJMenuBar();
        if (menuBar != null) {
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                JMenu menu = menuBar.getMenu(i);
                if (menu.getText().equals("Edit")) {
                    return menu;
                }
            }
        }
        return null;
    }

    private void filter(BufferedImageOp op) {
        if (image == null) return;
        image = op.filter(image, null);
        repaint();
    }

    private void convolve(float[] elements) {
        var kernel = new Kernel(3, 3, elements);
        var op = new ConvolveOp(kernel);
        filter(op);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new ImageProcessingFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}