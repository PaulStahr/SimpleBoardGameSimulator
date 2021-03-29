package gameObjects.action;

import java.io.Serializable;
import java.util.List;

public class AtomicAction extends GameAction implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -6145028605800162853L;
    private transient GameAction actions[];

    public AtomicAction(int source, List<GameAction> ga)
    {
        super(source);
        actions = new GameAction[ga.size()];
        for (int i = 0; i < actions.length; ++i)
        {
            actions[i] = ga.get(i);
        }
    }

    public void set(GameAction actions[]){this.actions = actions;}

    public GameAction get(int i) {return actions[i];}

    public int size() {return actions.length;}
}
