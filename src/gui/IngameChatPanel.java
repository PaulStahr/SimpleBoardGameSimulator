package gui;

import gameObjects.GameAction;
import gameObjects.UsertextMessageAction;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import main.Player;

public class IngameChatPanel implements GameChangeListener{
	int id = (int)System.nanoTime();
	GameInstance game;
	Player player;
	public IngameChatPanel(GameInstance game, Player player)
	{
		this.game = game;
		game.changeListener.add(this);
		this.player = player;
	}
	
	private void send()
	{
		game.update(new UsertextMessageAction(id, player.id , "Here we can send Hello World"));
	}

	@Override
	public void changeUpdate(GameAction action) {
		if (action instanceof UsertextMessageAction)
		{
			/*And here we get our message back*/
		}
	}
	
	
}
