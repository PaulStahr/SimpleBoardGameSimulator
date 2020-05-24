package gameObjects.functions;

import java.util.ArrayList;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import util.ArrayTools;

public class CheckingFunctions {

	public static void countIncoming(ArrayList<ObjectInstance> tmp, int incoming[]) {
		for (int i = 0; i < tmp.size(); ++i)
		{
			ObjectInstance current = tmp.get(i);
			if (current.state.aboveInstanceId != -1)
			{
				int aboveIdx = ArrayTools.binarySearch(tmp, current.state.aboveInstanceId, ObjectInstance.OBJECT_TO_ID);
				if (aboveIdx >= 0)
				{
					++incoming[aboveIdx];
				}
			}
		}
	}
	
	public static boolean checkStack(ArrayList<ObjectInstance> tmp, int begin, int end)
	{
		if (begin != end)
		{
			ObjectInstance last = tmp.get(begin);
			if (last.state.belowInstanceId != -1)
			{
				return false;
			}
			for (int i = begin + 1; i < end; ++i)
			{
				ObjectInstance current = tmp.get(i);
				if (last.state.aboveInstanceId != current.id || current.state.belowInstanceId != last.id)
				{
					return false;
				}
				last = current;
			}
			return last.state.aboveInstanceId == -1;
		}
		return true;
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
			if (curIdx < 0)
			{
				return write;
			}
			current = tmp.get(curIdx);
			--incoming[curIdx];
			if (incoming[curIdx] != 0)
			{
				return write;
			}
		}
	}
	
	public static boolean checkPlayerConsistency(int player_id, ArrayList<ObjectInstance> tmp, GameInstance gi)
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
					if (bottom != null)
					{
						return false;
					}
					bottom = oi;
				}
				if (oi.state.aboveInstanceId == -1)
				{
					if (top != null)
					{
						return false;
					}
					top = oi;
				}
			}
			if (bottom == null || top == null)
			{
				return false;
			}
			ObjectInstance current = bottom;
			if (current.owner_id() != player_id || !current.state.inPrivateArea)
			{
				return false;
			}
			for (int i = 1; i < tmp.size(); ++i)
			{
				ObjectInstance next = gi.getObjectInstanceById(current.state.aboveInstanceId);
				if (next == null || next.state.belowInstanceId != current.id || next.owner_id() != player_id || !next.state.inPrivateArea)
				{
					return false;
				}
				current = next;
			}
			if (current != top)
			{
				return false;
			}
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
				if (!CheckingFunctions.checkStack(tmp, oldWrite, write))
				{
					return false;
				}
			}
			read = Math.max(write, read + 1);
		}
		return write == incoming.length;
	}
	


}
