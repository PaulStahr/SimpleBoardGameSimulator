package io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import gameObjects.definition.GameObjectBook;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.ObjectState;
import util.io.StreamUtil;

public class ObjectStateIO {

    public static void simulateStateFromStreamObject(ObjectInputStream is, ObjectState state) throws IOException
    {
        int skip = 13 * 4 + 2;
        if (state instanceof GameObjectToken.TokenState)
        {
            skip -= StreamUtil.skip(is, 12);
            skip += is.readInt() * 4 + 1;
        }
        else if (state instanceof GameObjectDice.DiceState)  {skip += 4;}
        else if (state instanceof GameObjectFigure.FigureState){skip += 1;}
        long skipped = StreamUtil.skip(is, skip);
        if (skip != skipped)
        {
            throw new IOException("Needed to scip " + skip + " bytes but got " + skipped);
        }
    }


    /**
     * Edit a ObjectState from an XML Element. The Attributes "x", "y"
     * and "r" are needed in @param elem. All others are optional.
     * @param elem the Element with all needed information
     * @param state the ObjectState that shall the updated
     */
    static void editStateFromElement(ObjectState state, Element elem)
    {
        state.rotation        = GameIO.readAttribute(elem, IOString.R, state.rotation);
        state.originalRotation = GameIO.readAttribute(elem, IOString.R, state.originalRotation);
        state.originalRotation = GameIO.readAttribute(elem, IOString.ORIGINAL_ROTATION, state.originalRotation);
        state.scale 			= GameIO.readAttribute(elem, IOString.S, state.scale);
        state.aboveInstanceId 	= GameIO.readAttribute(elem, IOString.ABOVE, state.aboveInstanceId);
        state.belowInstanceId 	= GameIO.readAttribute(elem, IOString.BELOW, state.belowInstanceId);
        state.liesOnId 			= GameIO.readAttribute(elem, IOString.LYING_ON, state.liesOnId);
                                  GameIO.readAttribute(elem, IOString.LYING_ABOVE, state.aboveLyingObectIds);
        state.owner_id 			= GameIO.readAttribute(elem, IOString.OWNER_ID, state.owner_id);
        state.isSelected 		= GameIO.readAttribute(elem, IOString.IS_SELECTED, state.isSelected);
        state.drawValue			= GameIO.readAttribute(elem, IOString.DRAW_VALUE, state.drawValue);
        state.posX 				= GameIO.readAttribute(elem, IOString.X, state.posX);
        state.posY 				= GameIO.readAttribute(elem, IOString.Y, state.posY);
        state.value				= GameIO.readAttribute(elem, IOString.VALUE, state.value);
        state.sortValue         = GameIO.readAttribute(elem, IOString.SORT_VALUE, state.sortValue);
        state.rotationStep		= GameIO.readAttribute(elem, IOString.ROTATION_STEP, state.rotationStep);
        state.isFixed			= GameIO.readAttribute(elem, IOString.IS_FIXED, state.isFixed);
        state.inPrivateArea       = GameIO.readAttribute(elem, IOString.IN_PRIVATE_AREA, state.inPrivateArea);
        if (state instanceof GameObjectToken.TokenState)
        {
            ((GameObjectToken.TokenState)state).side = GameIO.readAttribute(elem, IOString.SIDE, ((GameObjectToken.TokenState)state).side);
        }
        if (state instanceof GameObjectFigure.FigureState && elem.getAttribute(IOString.STANDING) != null)
        {
            ((GameObjectFigure.FigureState)state).standing = Boolean.parseBoolean(elem.getAttributeValue(IOString.STANDING));
        }
        if (state instanceof GameObjectDice.DiceState && elem.getAttributeValue(IOString.SIDE) != null)
        {
            ((GameObjectDice.DiceState)state).side = Integer.parseInt(elem.getAttributeValue(IOString.SIDE));
        }
        if (state instanceof GameObjectBook.BookState && elem.getAttributeValue(IOString.SIDE) != null)
        {
            ((GameObjectBook.BookState)state).side = Integer.parseInt(elem.getAttributeValue(IOString.SIDE));
        }
    }

    public static void editStateFromStreamObject(ObjectInputStream is, ObjectState state) throws IOException
    {
        state.aboveInstanceId = is.readInt();
        state.belowInstanceId = is.readInt();
        state.liesOnId = is.readInt();
        state.aboveLyingObectIds.clear();
        for (int i = is.readInt(); i > 0; --i){
            state.aboveLyingObectIds.add(is.readInt());
        }
        state.inPrivateArea = is.readBoolean();
        state.owner_id = is.readInt();
        state.isSelected = is.readInt();
        state.drawValue = is.readInt();
        state.posX = is.readInt();
        state.posY = is.readInt();
        state.rotation = is.readInt();
        state.scale = is.readInt();
        state.value = is.readInt();
        state.sortValue = is.readInt();
        state.rotationStep = is.readInt();
        state.isFixed = is.readBoolean();
        if (state instanceof GameObjectToken.TokenState)
        {
            ((GameObjectToken.TokenState)state).side = is.readBoolean();
        }
        else if (state instanceof GameObjectDice.DiceState)
        {
            ((GameObjectDice.DiceState)state).side = is.readInt();
        }
        else if (state instanceof GameObjectFigure.FigureState)
        {
            ((GameObjectFigure.FigureState) state).standing = is.readBoolean();
        }
    }

    public static void writeStateToStreamObject(ObjectOutputStream out, ObjectState state) throws IOException
    {
        out.writeInt(state.aboveInstanceId);
        out.writeInt(state.belowInstanceId);
        out.writeInt(state.liesOnId);
        out.writeInt(state.aboveLyingObectIds.size());
        for (int id : state.aboveLyingObectIds){
            out.writeInt(id);
        }
        out.writeBoolean(state.inPrivateArea);
        out.writeInt(state.owner_id);
        out.writeInt(state.isSelected);
        out.writeInt(state.drawValue);
        out.writeInt(state.posX);
        out.writeInt(state.posY);
        out.writeInt(state.rotation);
        out.writeInt(state.scale);
        out.writeInt(state.value);
        out.writeInt(state.sortValue);
        out.writeInt(state.rotationStep);
        out.writeBoolean(state.isFixed);
        if (state instanceof GameObjectToken.TokenState)
        {
            out.writeBoolean(((GameObjectToken.TokenState)state).side);
        }
        else if (state instanceof GameObjectDice.DiceState)
        {
            out.writeInt(((GameObjectDice.DiceState)state).side);
        }
        else if (state instanceof GameObjectFigure.FigureState)
        {
            out.writeBoolean(((GameObjectFigure.FigureState) state).standing);
        }
    }


    /**
     * Write an ObjectState to an XML-Element. All fields of @param state
     * will be added as Attributes to @param elem. If state is a TokenState
     * or a FigureState, it will also write the side of standing attribute respectively.
     * @param state the ObjectState
     * @param elem the Element
     */
    static void writeStateToElement(ObjectState state, Element elem)
    {
        elem.setAttribute(IOString.X, 				Integer.toString(state.posX));
        elem.setAttribute(IOString.Y, 				Integer.toString(state.posY));
        elem.setAttribute(IOString.R, 				Integer.toString(state.rotation));
        elem.setAttribute(IOString.ORIGINAL_ROTATION, 				Integer.toString(state.originalRotation));
        elem.setAttribute(IOString.S, 				Integer.toString(state.scale));
        elem.setAttribute(IOString.OWNER_ID, 		Integer.toString(state.owner_id));
        elem.setAttribute(IOString.IS_SELECTED, 		Integer.toString(state.isSelected));
        elem.setAttribute(IOString.DRAW_VALUE, 		Integer.toString(state.drawValue));
        elem.setAttribute(IOString.ABOVE, 			Integer.toString(state.aboveInstanceId));
        elem.setAttribute(IOString.BELOW,			Integer.toString(state.belowInstanceId));
        elem.setAttribute(IOString.LYING_ON,        Integer.toString(state.liesOnId));
        elem.setAttribute(IOString.LYING_ABOVE,     state.aboveLyingObectIds.toString());
        elem.setAttribute(IOString.VALUE, 			Integer.toString(state.value));
        elem.setAttribute(IOString.SORT_VALUE,		Integer.toString(state.sortValue));
        elem.setAttribute(IOString.ROTATION_STEP, 	Integer.toString(state.rotationStep));
        elem.setAttribute(IOString.IS_FIXED, 		Boolean.toString(state.isFixed));
        elem.setAttribute(IOString.IN_PRIVATE_AREA, Boolean.toString(state.inPrivateArea));
        if (state instanceof GameObjectToken.TokenState) 					{elem.setAttribute(IOString.SIDE, Boolean.toString(((GameObjectToken.TokenState) state).side));}
        if (state instanceof GameObjectFigure.FigureState)	{elem.setAttribute(IOString.STANDING, Boolean.toString(((GameObjectFigure.FigureState) state).standing));}
        if (state instanceof GameObjectDice.DiceState)		{elem.setAttribute(IOString.SIDE, Integer.toString(((GameObjectDice.DiceState)state).side));}
        if (state instanceof GameObjectBook.BookState)		{elem.setAttribute(IOString.SIDE, Integer.toString(((GameObjectBook.BookState)state).side));}
    }

    /**
     * Encodes all fields of @param object to an XML document and puts
     * it into @param os.
     * @param object the ObjectState that shall be encoded
     * @param output the OutputStream the Game will be written to
     */
    public static void writeObjectStateToStreamXml(ObjectState object, OutputStream output) throws IOException
    {
        Document doc = new Document();
        Element elem = new Element(IOString.OBJECT_STATE);
        writeStateToElement(object, elem);
        doc.addContent(elem);
        new XMLOutputter(Format.getPrettyFormat()).output(doc, output);
    }

    /**
     * Edit the ObjectState @param objectState from information encoded in @param input.
     * ATTENTION 1: @param input is expected to contain an XML document
     * that can be used to build a Document via the SAXBuilder.
     * ATTENTION 2: Only the root element will be considered and it has to have the
     * attributes "x", "y" and "r". All other attributes are optional.
     * @param objectState the ObjectState to be edited
     * @param input the InputStream with all update information
     */
    public static void editObjectStateFromStream(ObjectState objectState, InputStream input) throws IOException, JDOMException
    {
        Document doc = new SAXBuilder().build(input);
        Element elem = doc.getRootElement();

        ObjectStateIO.editStateFromElement(objectState, elem);
    }

}
