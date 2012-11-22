package com.dzer6.ga3

import java.awt.*
import java.awt.image.*
import javax.imageio.ImageIO
import java.io.File
import java.io.IOException

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ImageTool {
  
    private static final Logger log = LoggerFactory.getLogger(ImageTool.class)
   
    public static byte[] getPNGBytes(byte[] dataBytes) {
        int X = 200
        int Y = 1
        BufferedImage I = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB)
        int x = 0
        WritableRaster wr = I.raster
        for (int i = 0; i < dataBytes.length; i+=3) {
            int[] color = new int[3]
            color[0] = i < dataBytes.length ? (int) dataBytes[i] : 0
            color[1] = i + 1 < dataBytes.length ? (int) dataBytes[i + 1] : 0
            color[2] = i + 2 < dataBytes.length ? (int) dataBytes[i + 2] : 0
      
            wr.setPixel(x++, 0, color)
        }
                    
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
    
        try {
            ImageIO.write(I, "png", bos)
        } catch (IOException e) {
            log.error("Unable to write image.", e)
        }
    
        return bos.toByteArray()
    }
}