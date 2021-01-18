package gameObjects.functions;

import java.util.ArrayList;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import util.ArrayTools;

public class CheckingFunctions {
	public static class GameInconsistency{}
	public static class InconsistencyMultistackend 		extends GameInconsistency{public final int first; public final int second; 	public InconsistencyMultistackend(int first,int second) 	{this.first = first; this.second = second;}}
	public static class InconsistencyNotLinkedViceVersa extends GameInconsistency{public final int first; public final int second; 	public InconsistencyNotLinkedViceVersa(int first,int second){this.first = first; this.second = second;}}
	public static class InconsistencyNostackend 		extends GameInconsistency{													public InconsistencyNostackend() 							{}}
	public static class InconsistencyNotInPrivateArea 	extends GameInconsistency{public final int id; 								public InconsistencyNotInPrivateArea(int id) 				{this.id = id;}}
	public static class InconsistencyNotOwnedArea 		extends GameInconsistency{public final int id; 								public InconsistencyNotOwnedArea(int id) 					{this.id = id;}}
	public static class StackEndReached 				extends GameInconsistency{public final int id; 								public StackEndReached(int id) 								{this.id = id;}}

	public static void countIncoming(ArrayList<ObjectInstance> tmp, int incoming[]) {
		for (int i = 0; i < tmp.size(); ++i)
		{
			ObjectInstance current = tmp.get(i);
			if (current.state.aboveInstanceId != -1)
			{
				int aboveIdx = ArrayTools.binarySearch(tmp, current.state.aboveInstanceId, ObjectInstance.OBJECT_TO_ID);
				if (aboveIdx >= 0){++incoming[aboveIdx];}
			}
		}
	}
	
	public static GameInconsistency checkStack(ArrayList<ObjectInstance> tmp, int begin, int end)
	{
		if (begin != end)
		{
			ObjectInstance last = tmp.get(begin);
			if (last.state.belowInstanceId != -1){return new InconsistencyMultistackend(last.id, last.state.belowInstanceId);}
			for (int i = begin + 1; i < end; ++i)
			{
				ObjectInstance current = tmp.get(i);
				if (last.state.aboveInstanceId != current.id || current.state.belowInstanceId != last.id){return new InconsistencyNotLinkedViceVersa(current.id, last.id);}
				last = current;
			}
			return last.state.aboveInstanceId == -1 ? null : new InconsistencyMultistackend(last.id, last.state.aboveInstanceId);
		}
		return null;
	}
	
	public static int packBelongingObjects(int incoming[], int write, int curIdx, ArrayList<ObjectInstance> tmp)
	{
		ObjectInstance current = tmp.get(curIdx);
		while(true){
			incoming[curIdx] = incoming[write];
			incoming[write] = 0;
			tmp.set(curIdx, tmp.get(write));
			tmp.set(write, current);
			++write;
			curIdx = ArrayTools.binarySearch(tmp, current.state.aboveInstanceId, ObjectInstance.OBJECT_TO_ID);
			if (curIdx < 0){return write;}
			current = tmp.get(curIdx);
			--incoming[curIdx];
			if (incoming[curIdx] != 0){return write;}
		}
	}
	
	public static GameInconsistency checkPlayerConsistency(int player_id, ArrayList<ObjectInstance> tmp, GameInstance gi)
	{
		tmp.clear();
		gi.getOwnedPrivateObjects(player_id, true, tmp);
		if (tmp.size() != 0)
		{
			ObjectInstance bottom = null;
			ObjectInstance top = null;
			for (int i = 0; i < tmp.size(); ++i)
			{
				ObjectInstance oi = tmp.get(i);
				if (oi.state.belowInstanceId == -1)
				{
					if (bottom != null){return new InconsistencyMultistackend(bottom.id, oi.id);}
					bottom = oi;
				}
				if (oi.state.aboveInstanceId == -1)
				{
					if (top != null){return new InconsistencyMultistackend(bottom.id, oi.id);}
					top = oi;
				}
			}
			if (bottom == null || top == null){return new InconsistencyNostackend();}
			ObjectInstance current = bottom;
			if (current.owner_id() != player_id || !current.state.inPrivateArea){return new InconsistencyNotInPrivateArea(current.id);}
			for (int i = 1; i < tmp.size(); ++i)
			{
				ObjectInstance next = gi.getObjectInstanceById(current.state.aboveInstanceId);
				if (next == null){return new StackEndReached(current.id);}
				if (next.state.belowInstanceId != current.id) {return new InconsistencyNotLinkedViceVersa(current.id, next.state.belowInstanceId);}
				if (next.owner_id() != player_id) {return new InconsistencyNotOwnedArea(next.id);}
				if (!next.state.inPrivateArea) {return new InconsistencyNotInPrivateArea(next.id);}
				current = next;
			}
			if (current != top){return new InconsistencyMultistackend(current.id, top.id);}
			tmp.clear();
		}
		gi.getOwnedPrivateObjects(player_id, false, tmp);
		tmp.sort(ObjectInstance.ID_COMPARATOR);
		int incoming[] = new int[tmp.size()];
		countIncoming(tmp, incoming);
		int write = 0;
		for (int read = 0; read < incoming.length;)
		{
			int oldWrite = write;
			if (incoming[read] == 0)
			{
				write = packBelongingObjects(incoming, write, read, tmp);
				GameInconsistency inc = checkStack(tmp, oldWrite, write);
				if (inc != null){return inc;}
			}
			read = Math.max(write, read + 1);
		}
		return write == incoming.length ? null : new GameInconsistency();
	}
}
