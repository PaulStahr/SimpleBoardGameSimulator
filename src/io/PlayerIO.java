package io;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import main.Player;
import util.io.StreamUtil;

public class PlayerIO {


    /**
     * Creates a new Player. All needed information will be retrieved
     * from the XML-element @param elem.
     * @param elem the Element with all needed information
     * @return the created GameInstance
     */
    static Player createPlayerFromElement(Element elem)
    {
        Player result = new Player(
                elem.getAttributeValue(StringIO.NAME),
                Integer.parseInt(elem.getAttributeValue(StringIO.ID)),
                new Color(Integer.parseInt(elem.getAttributeValue(StringIO.COLOR))),
                GameIO.readAttribute(elem, StringIO.SEAT_NUM, -1),
                GameIO.readAttribute(elem, StringIO.TRICK_NUM, 0),
                Integer.parseInt(elem.getAttributeValue(StringIO.MOUSE_X)),
                Integer.parseInt(elem.getAttributeValue(StringIO.MOUSE_Y)));
        result.screenWidth = Integer.parseInt(elem.getAttributeValue(StringIO.SCREEN_W));
        result.screenHeight = Integer.parseInt(elem.getAttributeValue(StringIO.SCREEN_H));
        result.visitor = Boolean.parseBoolean(elem.getAttributeValue(StringIO.VISITOR));
        GameIO.editAffineTransformFromElement(elem.getChild(StringIO.AFFINE_TRANSFORM), result.screenToBoardTransformation);
        return result;
    }

    private static Player editPlayerFromElement(Element elem, Player player)
    {
        player.setName(elem.getAttributeValue(StringIO.NAME));
        player.color = new Color(Integer.parseInt(elem.getAttributeValue(StringIO.COLOR)));
        player.seatNum = Integer.parseInt(elem.getAttributeValue(StringIO.SEAT_NUM));
        player.trickNum = Integer.parseInt(elem.getAttributeValue(StringIO.TRICK_NUM));
        player.mouseXPos = Integer.parseInt(elem.getAttributeValue(StringIO.MOUSE_X));
        player.mouseYPos = Integer.parseInt(elem.getAttributeValue(StringIO.MOUSE_Y));
        player.screenWidth = Integer.parseInt(elem.getAttributeValue(StringIO.SCREEN_W));
        player.screenHeight = Integer.parseInt(elem.getAttributeValue(StringIO.SCREEN_H));
        player.visitor = Boolean.parseBoolean(elem.getAttributeValue(StringIO.VISITOR));
        GameIO.editAffineTransformFromElement(elem.getChild(StringIO.AFFINE_TRANSFORM), player.screenToBoardTransformation);
        return player;
    }

    public static void simulateEditPlayerFromObject(ObjectInputStream is) throws IOException, ClassNotFoundException
    {
        is.readObject();
        long toScip = 7 * Integer.SIZE / 8 + 6 * Double.SIZE / 8 + 1;
        long skipped = StreamUtil.skip(is, toScip);
        if (toScip != skipped)
        {
            throw new IOException("Needed to scip " + toScip + " bytes but got " + skipped);
        }
    }

    public static void editPlayerFromStreamObject(ObjectInputStream is, Player player) throws ClassNotFoundException, IOException
    {
        if (player == null)
        {
            simulateEditPlayerFromObject(is);
            throw new NullPointerException();
        }
        player.setName((String)is.readObject());
        //read player color
        int col = is.readInt();
        if (player.color.getRGB() != col) {player.color = new Color(col);}
        player.seatNum = is.readInt();
        player.trickNum = is.readInt();
        //read player mouse position
        player.mouseXPos = is.readInt();
        player.mouseYPos = is.readInt();
        //read player window position
        player.screenToBoardTransformation.setTransform(is.readDouble(), is.readDouble(),is.readDouble(), is.readDouble(),is.readDouble(), is.readDouble());
        player.screenWidth = is.readInt();
        player.screenHeight = is.readInt();
        player.visitor = is.readBoolean();
    }

    public static void writePlayerToStreamObject(ObjectOutputStream out, Player player) throws IOException
    {
        out.writeObject(player.getName());
        //write player color
        out.writeInt(player.color.getRGB());
        out.writeInt(player.seatNum);
        out.writeInt(player.trickNum);
        //write player mouse position
        out.writeInt(player.mouseXPos);
        out.writeInt(player.mouseYPos);
        //write player window position
        out.writeDouble(player.screenToBoardTransformation.getScaleX());
        out.writeDouble(player.screenToBoardTransformation.getShearY());
        out.writeDouble(player.screenToBoardTransformation.getShearX());
        out.writeDouble(player.screenToBoardTransformation.getScaleY());
        out.writeDouble(player.screenToBoardTransformation.getTranslateX());
        out.writeDouble(player.screenToBoardTransformation.getTranslateY());
        out.writeInt(player.screenWidth);
        out.writeInt(player.screenHeight);
        out.writeBoolean(player.visitor);
    }

    public static void editPlayerFromStreamZip(InputStream is, Player gi) throws JDOMException, IOException
    {
        Document doc = new SAXBuilder().build(is);
        Element root = doc.getRootElement();
        editPlayerFromElement(root, gi);
    }

    public static Player readPlayerFromStream(InputStream is) throws JDOMException, IOException
    {
        Document doc = new SAXBuilder().build(is);
        Element root = doc.getRootElement();

        return createPlayerFromElement(root);
    }


    public static void editPlayerFromZip(InputStream inputStream, Player player) throws IOException, JDOMException {
        ZipInputStream stream = new ZipInputStream(inputStream);
        ZipEntry entry;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        while ((entry = stream.getNextEntry()) != null)
        {
            StreamUtil.copy(stream, byteStream);
            if (entry.getName().equals(StringIO.PLAYER_XML))
            {
                editPlayerFromStreamZip(new ByteArrayInputStream(byteStream.toByteArray()), player);
            }
            byteStream.reset();
        }
        stream.close();
    }

    public static Player readPlayerFromZip(InputStream inputStream) throws IOException, JDOMException {
        ZipInputStream stream = new ZipInputStream(inputStream);
        ZipEntry entry;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Player pl = null;
        while ((entry = stream.getNextEntry()) != null)
        {
            StreamUtil.copy(stream, byteStream);
            if (entry.getName().equals(StringIO.PLAYER_XML))
            {
                pl = readPlayerFromStream(new ByteArrayInputStream(byteStream.toByteArray()));
            }
            byteStream.reset();
        }
        stream.close();
        return pl;
    }

    /**
     * Creates a new Element that represents the @param player.
     * @param player the Player that shall be encoded
     * @return the created Element
     */
    static Element createElementFromPlayer(Player player) {
        Element elem = new Element(StringIO.PLAYER);
        elem.setAttribute(StringIO.NAME, player.getName());
        elem.setAttribute(StringIO.ID, Integer.toString(player.id));
        elem.setAttribute(StringIO.COLOR, Integer.toString(player.color.getRGB()));
        elem.setAttribute(StringIO.SEAT_NUM, Integer.toString(player.seatNum));
        elem.setAttribute(StringIO.TRICK_NUM, Integer.toString(player.trickNum));
        elem.setAttribute(StringIO.MOUSE_X, Integer.toString(player.mouseXPos));
        elem.setAttribute(StringIO.MOUSE_Y, Integer.toString(player.mouseYPos));
        elem.setAttribute(StringIO.SCREEN_W, Integer.toString(player.screenWidth));
        elem.setAttribute(StringIO.SCREEN_H, Integer.toString(player.screenHeight));
        elem.setAttribute(StringIO.VISITOR, Boolean.toString(player.visitor));
        elem.addContent(GameIO.createElementFromAffineTransform(player.screenToBoardTransformation));
        return elem;
    }

    public static void writePlayerToStream(Player player, OutputStream output) throws IOException
    {
        Document doc = new Document();
        Element elem = createElementFromPlayer(player);
        doc.addContent(elem);
        new XMLOutputter(Format.getPrettyFormat()).output(doc, output);
    }

}
