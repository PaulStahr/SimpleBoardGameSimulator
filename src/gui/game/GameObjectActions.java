package gui.game;

import data.controls.ControlTypes;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.functions.ObjectFunctions;
import gameObjects.functions.PlayerFunctions;
import gameObjects.instance.ObjectInstance;
import util.data.IntegerArrayList;

import java.awt.geom.Point2D;

public class GameObjectActions {
    public static void RunAction(ControlTypes controlTypes, GamePanel gamePanel, IntegerArrayList ial){
        int count = 0;
        IntegerArrayList ial2 = new IntegerArrayList();
        switch (controlTypes){
            case MOVE_BOARD:
                break;
            case SHUFFLE:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ObjectFunctions.getObjectRepresentatives(gamePanel.gameInstance, ial)) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.shuffleStack(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi);
                    gamePanel.playAudio(GamePanel.AudioClip.shuffle);
                    ObjectFunctions.deselectObject(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi.id, gamePanel.hoveredObject);
                    ObjectFunctions.selectObject(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), ObjectFunctions.getStackTop(gamePanel.gameInstance, oi).id);
                }
                break;
            case FLIP:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ial) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.flipTokenObject(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi);
                    ObjectFunctions.rollTheDice(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi);
                }
                if (gamePanel.hoveredObject != null && ObjectFunctions.getObjectSelector(gamePanel.gameInstance, gamePanel.hoveredObject) != gamePanel.getPlayerId()) {
                    ObjectFunctions.flipTokenObject(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject);
                    ObjectFunctions.rollTheDice(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject);
                }
                break;
            case FLIP_STACK:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ObjectFunctions.getObjectRepresentatives(gamePanel.gameInstance, ial)) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.flipTokenStack(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), ObjectFunctions.getStackTop(gamePanel.gameInstance, oi), gamePanel.hoveredObject);
                }
                break;
            case DROP:
                ObjectFunctions.dropObject(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject);
                break;
            case DROP_ALL:
                ObjectFunctions.dropObjects(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject);
                break;
            case COUNT:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ial) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    if (oi.go instanceof GameObjectToken) {
                        count += ObjectFunctions.countStack(gamePanel.gameInstance, oi);
                    } else if (oi.go instanceof GameObjectDice) {
                        count += 1;
                    } else if (oi.go instanceof GameObjectFigure) {
                        count += 1;
                    }
                }
                gamePanel.getPlayer().actionString = "Object Number: " + count;
                break;
            case COUNT_VALUES:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ial) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    count += ObjectFunctions.countStackValues(gamePanel.gameInstance, oi);
                }
                gamePanel.getPlayer().actionString = "Value: " + count;
                break;
            case SIT_DOWN:
                for (int i = 0; i < gamePanel.table.playerShapes.size(); ++i) {
                    if (gamePanel.table.playerShapes.get(i).contains(gamePanel.mouseScreenX, gamePanel.mouseScreenY)) {
                        gamePanel.sitDown(gamePanel.getPlayer(), i);
                        break;
                    }
                }
                break;
            case SIT_DOWN_OWN_PLACE:
                gamePanel.sitDown(gamePanel.getPlayer(), PlayerFunctions.GetTablePlayerPosition(gamePanel.getPlayer()));
                break;
            case FIX:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ial) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.fixObject(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi);
                }
                break;
            case ROTATE:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ObjectFunctions.getObjectRepresentatives(gamePanel.gameInstance, ial)) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.rotateStep(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi, ial);
                }
                gamePanel.repaint();
                break;
            case HIDE_PRIVATE_AREA:
                if (gamePanel.privateArea.zooming == 0.1) {
                    gamePanel.privateArea.zooming = gamePanel.privateArea.savedZooming;
                    gamePanel.mouseInPrivateArea = ObjectFunctions.isInPrivateArea(gamePanel.privateArea, gamePanel.getMouseBoardPos().getXI(), gamePanel.getMouseBoardPos().getYI());
                } else {
                    gamePanel.privateArea.savedZooming = gamePanel.privateArea.zooming;
                    gamePanel.privateArea.zooming = 0.1;
                    gamePanel.mouseInPrivateArea = false;
                }
                gamePanel.updateGameTransform();
                gamePanel.repaint();
                break;
            case COLLECT_SELECTED:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                ial2.clear();
                ial2.set(ObjectFunctions.getObjectRepresentatives(gamePanel.gameInstance, ial, true));
                for (int idx = 0; idx < ial2.size() - 1; ++idx) {
                    int topId = ial2.getI(idx);
                    int bottomId = ial2.getI(idx + 1);
                    ObjectInstance top = ObjectFunctions.getStackTop(gamePanel.gameInstance, gamePanel.gameInstance.getObjectInstanceById(topId));
                    ObjectInstance bottom = ObjectFunctions.getStackBottom(gamePanel.gameInstance, gamePanel.gameInstance.getObjectInstanceById(bottomId));
                    ObjectFunctions.mergeStacks(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), top, bottom);
                }
                break;
            case COLLECT_ALL:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ial) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.stackAllObjectsOfGroup(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi, gamePanel.hoveredObject, false);
                }
                break;
            case COLLECT_ALL_WITH_HANDS:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ial) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.stackAllObjectsOfGroup(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi, gamePanel.hoveredObject, true);
                }
                break;
            case DISSOLVE_STACK:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ial) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.removeStackRelations(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi);
                }
                break;
            case TAKE:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ObjectFunctions.getObjectRepresentatives(gamePanel.gameInstance, ial)) {
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    double offset;
                    if (gamePanel.table != null){
                        offset = gamePanel.table.getTableOffset(gamePanel.getPlayer(), oi);
                        ObjectFunctions.takeObjects(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.table.getTableCenter(new Point2D.Double()), offset,  oi, gamePanel.hoveredObject);
                    }
                }
                ObjectFunctions.deselectAllSelected(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), ial, gamePanel.hoveredObject);
                break;
            case DEAL_OBJECTS:
                ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                for (int oId : ObjectFunctions.getObjectRepresentatives(gamePanel.gameInstance, ial)) {
                    IntegerArrayList stackList = new IntegerArrayList();
                    ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                    ObjectFunctions.getStack(gamePanel.gameInstance, oi, stackList);
                    ObjectFunctions.removeStackRelations(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi);
                    for (int id : stackList) {
                        ial2.add(id);
                    }
                }
                ObjectFunctions.giveObjects(gamePanel, gamePanel.gameInstance, gamePanel.table.getTableCenter(new Point2D.Double()), gamePanel.table.getTableOffset(gamePanel.getPlayer(), gamePanel.gameInstance.getObjectInstanceById(ial.get(0))), ial2, gamePanel.objectInstanceList, gamePanel.hoveredObject);
                break;
            case SORT:
                if (gamePanel.hoveredObject != null){
                    ObjectFunctions.sortHandCardsByValue(gamePanel, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.table.getTableCenter(new Point2D.Double()), gamePanel.table.getTableOffset(gamePanel.getPlayer(), gamePanel.hoveredObject), ial, gamePanel.objectInstanceList, gamePanel.hoveredObject, false);
                }
                break;
            case VIEW_COLLECT_STACK:
                break;
            case GET_BOTTOM_CARD:
                break;
            case PLAY:
                if (gamePanel.hoveredObject != null) {
                    ObjectFunctions.playObject(gamePanel, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject, gamePanel.hoveredObject);
                    gamePanel.playAudio(GamePanel.AudioClip.drop);
                }
                break;
            case OPEN_HELP:
                break;
            case DESELECT_ALL:
                ObjectFunctions.deselectAll(gamePanel.id, gamePanel.gameInstance);
                break;
            case VIEW:
                if (!gamePanel.mouseInPrivateArea) {
                    ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                    for (int oId : ObjectFunctions.getObjectRepresentatives(gamePanel.gameInstance, ial)) {
                        ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                        if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gamePanel.gameInstance, oi), ObjectFunctions.getStackBottom(gamePanel.gameInstance, oi))) {
                            ObjectFunctions.displayStack(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi, (int) (oi.getWidth(gamePanel.getPlayerId()) * gamePanel.gameInstance.cardOverlap));
                        } else {
                            if (ial.size() == 1) {
                                ObjectInstance selectedObject = gamePanel.gameInstance.getObjectInstanceById(ial.get(0));
                                ObjectFunctions.collectStack(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), selectedObject);
                            } else {
                                ObjectFunctions.collectStack(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi);
                            }
                        }
                    }
                    // unfold dice
                    for (int oId : ial) {
                        ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(oId);
                        if (oi != null && oi.go instanceof GameObjectDice) {
                            GameObjectDice.DiceState state = (GameObjectDice.DiceState) oi.state;
                            state.unfold = !state.unfold;
                        }
                    }
                    if (gamePanel.hoveredObject != null && gamePanel.getPlayerId() != ObjectFunctions.getObjectSelector(gamePanel.gameInstance, gamePanel.hoveredObject) && gamePanel.hoveredObject.go instanceof GameObjectDice) {
                        GameObjectDice.DiceState state = (GameObjectDice.DiceState) gamePanel.hoveredObject.state;
                        state.unfold = !state.unfold;
                    }

                } else {
                    ObjectFunctions.getOwnedStack(gamePanel.gameInstance, gamePanel.getPlayer(), ial);
                    if (ial.size() > 0) {
                        ObjectInstance oi = gamePanel.gameInstance.getObjectInstanceById(ial.getI(0));
                        if (ObjectFunctions.haveSamePositions(ObjectFunctions.getStackTop(gamePanel.gameInstance, oi), ObjectFunctions.getStackBottom(gamePanel.gameInstance, oi))) {
                            ObjectFunctions.displayStack(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi, (int) (oi.getWidth(gamePanel.getPlayerId()) * gamePanel.gameInstance.cardOverlap));
                        } else {
                            ObjectFunctions.getSelectedObjects(gamePanel.gameInstance, gamePanel.getPlayer(), ial2);
                            if (ial2.size() == 1) {
                                ObjectInstance selectedObject = gamePanel.gameInstance.getObjectInstanceById(ial2.get(0));
                                ObjectFunctions.collectStack(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), selectedObject);
                            } else {
                                ObjectFunctions.collectStack(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), oi);
                            }
                        }
                    }
                }
                break;
            case UNPACK_BOX:
                ObjectFunctions.unpackBox(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject);
                break;
            case PACK_BOX:
                ObjectFunctions.packBox(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject);
                break;
            case HIDE_SHOW_TABLE:
                gamePanel.isTableVisible = !gamePanel.isTableVisible;
                break;
            case TAKE_TRICK:
                double offset;
                Point2D tableCenter = new Point2D.Double();
                if (gamePanel.table != null){
                    offset = gamePanel.table.getTableOffset(gamePanel.getPlayer(), gamePanel.hoveredObject);
                    gamePanel.table.getTableCenter(tableCenter);
                    ObjectFunctions.takeTrick(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.table.stackerShape, tableCenter, offset, gamePanel.getBoardToScreenTransform(), gamePanel.hoveredObject, ial, gamePanel.hoveredObject);
                }
                break;
            case MOVE_OBJECT:
                break;
            case PLAY_FACE_UP:
                break;
            case GET_TOP_N_CARDS:
                break;
            case MOVE_STACK:
                break;
            case SELECT_OBJECT:
                break;
            case PREVIOUS_PAGE:
                ObjectFunctions.previousBookPage(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject);
                break;
            case NEXT_PAGE:
                ObjectFunctions.nextBookPage(gamePanel.id, gamePanel.gameInstance, gamePanel.getPlayer(), gamePanel.hoveredObject);
                break;
        }
    }
}
