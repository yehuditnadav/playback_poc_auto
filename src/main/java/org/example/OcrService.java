package org.example;

import com.google.common.collect.Iterables;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.util.LoadLibs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OcrService {
    public static Word getWord(String imgFileName, String word) throws Exception {
        File imageFile = new File(imgFileName);
        File tmpFolder = LoadLibs.extractTessResources("win32-x86-64");
        System.setProperty("java.library.path", tmpFolder.getPath());

        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage("heb+eng");
        tesseract.setOcrEngineMode(1);
        tesseract.setTessVariable("tessedit_write_images", "true");

        Path dataDirectory = Paths.get(System.getProperty("user.dir"), "tess4j_data");
//        Path dataDirectory = Paths.get(ClassLoader.getSystemResource("tess4j_data").toURI());
        tesseract.setDatapath(dataDirectory.toString());

        BufferedImage image = toBufferedImage(ImageIO.read(imageFile));

        Word liveWord = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD).stream().filter(w -> w.getText().replace("?", "").equals(word)).findFirst().orElse(null);
//        if (liveWord == null) {
            String result = tesseract.doOCR(imageFile);
            System.out.println(result);
//        }
        return liveWord;
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(image, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static Word getLastWord(String imgFileName, String word) throws Exception {
        File imageFile = new File(imgFileName);
        File tmpFolder = LoadLibs.extractTessResources("win32-x86-64");
        System.setProperty("java.library.path", tmpFolder.getPath());

        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage("heb+eng");
        tesseract.setOcrEngineMode(1);

        Path dataDirectory = Paths.get(ClassLoader.getSystemResource("tess4j_data").toURI());
        tesseract.setDatapath(dataDirectory.toString());

        BufferedImage image = ImageIO.read(imageFile);
        Word liveWord = Iterables.getLast(() -> tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD).stream().
                filter(w -> w.getText().equals(word)).iterator());

        return liveWord;
    }
}
