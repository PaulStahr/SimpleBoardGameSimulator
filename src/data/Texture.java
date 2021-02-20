package data;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.io.StreamUtil;

public class Texture {
    private static final Logger logger = LoggerFactory.getLogger(Texture.class);
    private byte data[];
    BufferedImage img;
    public String suffix;
    
    public Texture(InputStream in, String suffix) throws IOException {
        this.data = StreamUtil.toByteArray(in);
        this.suffix = suffix;
    }
    
    public Texture(byte[] data, String suffix) {
        this.data = data;
        img = null;
    }
    public Texture(BufferedImage img, String suffix) {
        this.img = img;
        this.suffix = suffix;
    }

    public byte[] getData() throws IOException {
        if (data == null)
        {
            synchronized(this)
            {
                if (data == null)
                {
                    ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                    ImageIO.write(img, suffix, tmp);
                    data = tmp.toByteArray();
                }
            }
        }
        return data;
    }
    
    public BufferedImage getImage() throws IOException {
        if (img == null)
        {
            synchronized(this)
            {
                if (img == null)
                {
                    img = ImageIO.read(new ByteArrayInputStream(data));    
                }
            }
        }
        return img;
    }

    public BufferedImage getImageNoExc() {
        try {
            return getImage();
        } catch (IOException e) {
            logger.error("Couldn't read image");
            return null;
        }
    }
    
    public void writeTo(OutputStream out) throws IOException{out.write(getData());}
}
