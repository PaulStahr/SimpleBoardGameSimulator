package gameObjects.functions;

import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.GamePanel;
import main.Player;

public class PlayerFunctions {
    public static Player GetPlayerFromTablePlace(GameInstance gameInstance, int tablePlace){
        int playerIndex = 0;
        for (int j = 0; j < gameInstance.getPlayerNumber(); ++j)
        {
            if (tablePlace < gameInstance.getPlayerNumber()) {
                if (gameInstance.getPlayerByIndex(j).id < gameInstance.getPlayerByIndex(tablePlace).id) {
                    ++playerIndex;
                }
            }
        }
        Player player = gameInstance.getPlayerByIndex(playerIndex);
        return player;
    }

    public static int GetTablePlayerPosition(GameInstance gameInstance, Player player){
        return gameInstance.playerSeatList.indexOf(player.id);
    }

    public static double GetCurrentPlayerRotation(GamePanel gamePanel, GameInstance gameInstance, Player player){
        int playerPosition = GetTablePlayerPosition(gameInstance, player);
        return 360. / gamePanel.table.playerShapes.size() * playerPosition;
    }
}
