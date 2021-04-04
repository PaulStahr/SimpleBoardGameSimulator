package gameObjects.functions;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.data.IntegerArrayList;

import java.util.ArrayList;

public class CheckFunctions {
    public static boolean objectOnTableSelectable(ObjectInstance objectInstance){
        return !objectInstance.state.isFixed && !objectInstance.state.inBox && !objectInstance.state.inPrivateArea && objectInstance.state.owner_id == -1;
    }

    public static void selectableObjectsOnTable(ArrayList<ObjectInstance> objectList, ArrayList<ObjectInstance> selectableObjects){
        selectableObjects.clear();
        for (ObjectInstance oi : objectList){
            if (objectOnTableSelectable(oi)){
                selectableObjects.add(oi);
            }
        }
    }

    public static boolean drawObjectOnTable(ObjectInstance objectInstance){
        return !objectInstance.state.inBox && !objectInstance.state.inPrivateArea && objectInstance.state.owner_id == -1;
    }

    public static void drawableObjectsOnTable(GameInstance gameInstance, IntegerArrayList ial, IntegerArrayList DrawableObjects){
        DrawableObjects.clear();
        for (int id : ial){
            ObjectInstance oi = gameInstance.getObjectInstanceById(id);
            if (drawObjectOnTable(oi)){
                DrawableObjects.add(oi.id);
            }
        }
    }

    public static void drawableObjectsOnTable(ArrayList<ObjectInstance> ObjectList, ArrayList<ObjectInstance> DrawableObjects){
        DrawableObjects.clear();
        for (ObjectInstance oi : ObjectList){
            if (drawObjectOnTable(oi)){
                DrawableObjects.add(oi);
            }
        }
    }

    public static boolean isValidNearestObject(ObjectInstance oi, Player player, IntegerArrayList ignoredObjects){
        return !oi.state.isFixed && !oi.state.inBox && (ignoredObjects == null || !ignoredObjects.contains(oi.id)) && !oi.state.inPrivateArea && (oi.state.owner_id == -1 || oi.state.owner_id == player.id);
    }
}
