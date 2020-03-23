package gameObjects.functions;

import gameObjects.GameObjectInstanceEditAction;
import gameObjects.definition.GameObjectCard;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gui.GamePanel;
import main.Player;
import util.data.IntegerArrayList;

import java.util.Collections;

public class CardStackFunctions {

    public static ObjectInstance getStackTop(GameInstance gameInstance, ObjectInstance objectInstance)
    {
        ObjectInstance currentTop = objectInstance;
        while (currentTop.state.aboveInstanceId != -1){
            currentTop = gameInstance.objects.get(currentTop.state.aboveInstanceId);
            if (objectInstance == currentTop)
            {
                throw new RuntimeException();
            }
        }
        return currentTop;

    }


    public static ObjectInstance getStackBottom(GameInstance gameInstance, ObjectInstance objectInstance){
        ObjectInstance currentBottom = objectInstance;
        while (currentBottom.state.belowInstanceId != -1){
            currentBottom = gameInstance.objects.get(currentBottom.state.belowInstanceId);
            if (objectInstance == currentBottom)
            {
                throw new RuntimeException();
            }
        }
        return currentBottom;
    }


    public static void shuffleStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance){
        /*Shuffle objects on a stack*/
        ObjectInstance topObject = getStackTop(gameInstance, objectInstance);
        IntegerArrayList objectStack = new IntegerArrayList();
        objectStack.add(topObject.id);
        ObjectInstance currentObjectInstance = topObject;
        while (currentObjectInstance.state.belowInstanceId != -1)
        {
            objectStack.add(currentObjectInstance.state.belowInstanceId);
            currentObjectInstance =  gameInstance.objects.get(currentObjectInstance.state.belowInstanceId);
        }
        Collections.shuffle(objectStack);
        for (int i = 0; i < objectStack.size(); i++)
        {
            ObjectInstance currentObject = gameInstance.objects.get(objectStack.get(i));
            if (i==0 && i<objectStack.size()-1){
                currentObject.state.belowInstanceId = -1;
                currentObject.state.aboveInstanceId = gameInstance.objects.get(objectStack.get(i+1)).id;
            }
            else if(i==objectStack.size()-1){
                currentObject.state.belowInstanceId = gameInstance.objects.get(objectStack.get(i-1)).id;
                currentObject.state.aboveInstanceId = -1;
            }
            else{
                currentObject.state.belowInstanceId = gameInstance.objects.get(objectStack.get(i-1)).id;
                currentObject.state.aboveInstanceId = gameInstance.objects.get(objectStack.get(i+1)).id;
            }
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject));
        }

    }

    public static void flipObject(int gamePanelId, GameInstance gameInstance, Player player,ObjectInstance objectInstance)
    {
        ((GameObjectCard.CardState)objectInstance.state).side = !((GameObjectCard.CardState)objectInstance.state).side;
        gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, objectInstance));
    }

    public static void flipStack(int gamePanelId, GameInstance gameInstance, Player player, ObjectInstance objectInstance){
        /*Flip the whole stack*/
        ObjectInstance topObject = getStackTop(gameInstance, objectInstance);
        IntegerArrayList objectStack = new IntegerArrayList();
        objectStack.add(topObject.id);
        ObjectInstance currentObjectInstance = topObject;
        flipObject(gamePanelId, gameInstance, player, currentObjectInstance);
        while (currentObjectInstance.state.belowInstanceId != -1)
        {
            objectStack.add(currentObjectInstance.state.belowInstanceId);
            currentObjectInstance =  gameInstance.objects.get(currentObjectInstance.state.belowInstanceId);
            flipObject(gamePanelId, gameInstance, player, currentObjectInstance);
        }

        for (int i = objectStack.size() - 1; i >=0 ; i--)
        {
            ObjectInstance currentObject = gameInstance.objects.get(objectStack.get(i));
            int aboveId = currentObject.state.aboveInstanceId;
            currentObject.state.aboveInstanceId = currentObject.state.belowInstanceId;
            currentObject.state.belowInstanceId = aboveId;
            gameInstance.update(new GameObjectInstanceEditAction(gamePanelId, player, currentObject));
        }
    }

    public static int countStack(GameInstance gameInstance, ObjectInstance objectInstance){
        /*Flip the whole stack*/
        int counter = 1;
        ObjectInstance topObject = getStackTop(gameInstance, objectInstance);
        ObjectInstance currentObjectInstance = topObject;
        while (currentObjectInstance.state.belowInstanceId != -1)
        {
            counter += 1;
            currentObjectInstance =  gameInstance.objects.get(currentObjectInstance.state.belowInstanceId);
        }
        return counter;
    }

}
