package gameObjects.functions;

import gameObjects.instance.GameInstance;
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

    public static int GetTablePlayerPosition(Player player){
        return player.seatNum;
    }

    public static double GetCurrentPlayerRotation(GameInstance gameInstance, Player player){
        int playerPosition = GetTablePlayerPosition(player);
        if (playerPosition == -1){
            return 0;
        }
        return 360. / gameInstance.getPlayerList(false).size() * playerPosition;
    }
}
