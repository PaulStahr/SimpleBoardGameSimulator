package gameObjects.functions;

import java.util.ArrayList;

import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import util.ArrayTools;

public class CheckingFunctions {
	public static class GameInconsistency{}
    public static class InconsistencyMultistackBottom   extends GameInconsistency{public final int first; public final int second;  public InconsistencyMultistackBottom(int first,int second)     {this.first = first; this.second = second;}}
    public static class InconsistencyMultistackTop      extends GameInconsistency{public final int first; public final int second;  public InconsistencyMultistackTop(int first,int second)     {this.first = first; this.second = second;}}
	public static class InconsistencyNotLinkedViceVersa extends GameInconsistency{public final int first; public final int second; 	public InconsistencyNotLinkedViceVersa(int first,int second){this.first = first; this.second = second;}}
    public static class InconsistencyNostackBottom      extends GameInconsistency{                                                  public InconsistencyNostackBottom()                      {}}
    public static class InconsistencyNostackTop         extends GameInconsistency{                                                  public InconsistencyNostackTop()                         {}}
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
		if (begin == end){return null;}
		ObjectInstance last = tmp.get(begin);
		if (last.state.belowInstanceId != -1){return new InconsistencyMultistackBottom(last.id, last.state.belowInstanceId);}
		for (int i = begin + 1; i < end; ++i)
		{
			ObjectInstance current = tmp.get(i);
			if (last.state.aboveInstanceId != current.id || current.state.belowInstanceId != last.id){return new InconsistencyNotLinkedViceVersa(current.id, last.id);}
			last = current;
		}
		return last.state.aboveInstanceId == -1 ? null : new InconsistencyMultistackTop(last.id, last.state.aboveInstanceId);
	}
	
	public static void packBelongingObjects(int incoming[], int nextIdx, ArrayList<ObjectInstance> sorted, ArrayList<ObjectInstance> output)
	{
		while(incoming[nextIdx] == 0){
			ObjectInstance current = sorted.get(nextIdx);
			incoming[nextIdx] = -1;
			output.add(current);
			int aboveId = current.state.aboveInstanceId;
			if (aboveId == -1){return;}
			nextIdx = ArrayTools.binarySearch(sorted, current.state.aboveInstanceId, ObjectInstance.OBJECT_TO_ID);
			if (nextIdx < 0){return;}
			--incoming[nextIdx];
		}
	}
	
	public static GameInconsistency checkChainingCorrectness(ArrayList<ObjectInstance> objects)
	{
		for (int i = 0; i < objects.size(); ++i)
		{
			ObjectInstance current = objects.get(i);
			if (current.state.aboveInstanceId != -1)
			{
				int index = ArrayTools.binarySearch(objects, current.state.aboveInstanceId, ObjectInstance.OBJECT_TO_ID);
				if (index < 0){return new GameInconsistency();}
				if (objects.get(index).state.belowInstanceId != current.id) {return new InconsistencyNotLinkedViceVersa(objects.get(index).id, current.id);}
			}
			if (current.state.belowInstanceId != -1)
			{
				int index = ArrayTools.binarySearch(objects, current.state.belowInstanceId, ObjectInstance.OBJECT_TO_ID);
				if (index < 0){return new GameInconsistency();}
				if (objects.get(index).state.aboveInstanceId != current.id) {return new InconsistencyNotLinkedViceVersa(objects.get(index).id, current.id);}

			}
		}
		return null;
	}
	
	public static GameInconsistency checkPlayerConsistency(int player_id, ArrayList<ObjectInstance> sorted, ArrayList<ObjectInstance> output, GameInstance gi)
	{
		sorted.clear();
		gi.getOwnedPrivateObjects(player_id, true, sorted);
		if (sorted.size() != 0)
		{
			ObjectInstance bottom = null;
			ObjectInstance top = null;
			for (int i = 0; i < sorted.size(); ++i)
			{
				ObjectInstance oi = sorted.get(i);
				if (oi.state.belowInstanceId == -1)
				{
					if (bottom != null){return new InconsistencyMultistackBottom(bottom.id, oi.id);}
					bottom = oi;
				}
				if (oi.state.aboveInstanceId == -1)
				{
					if (top != null){return new InconsistencyMultistackTop(bottom.id, oi.id);}
					top = oi;
				}
			}
			if (bottom == null)  {return new InconsistencyNostackBottom();}
			if (top == null)     {return new InconsistencyNostackTop();}
			ObjectInstance current = bottom;
			if (current.owner_id() != player_id || !current.state.inPrivateArea){return new InconsistencyNotInPrivateArea(current.id);}
			for (int i = 1; i < sorted.size(); ++i)
			{
				ObjectInstance next = gi.getObjectInstanceById(current.state.aboveInstanceId);
				if (next == null){return new StackEndReached(current.id);}
				if (next.state.belowInstanceId != current.id) {return new InconsistencyNotLinkedViceVersa(current.id, next.state.belowInstanceId);}
				if (next.owner_id() != player_id) {return new InconsistencyNotOwnedArea(next.id);}
				if (!next.state.inPrivateArea) {return new InconsistencyNotInPrivateArea(next.id);}
				current = next;
			}
			if (current != top){return new InconsistencyMultistackTop(current.id, top.id);}
			sorted.clear();
		}
		gi.getOwnedPrivateObjects(player_id, false, sorted);
		sorted.sort(ObjectInstance.ID_COMPARATOR);
		GameInconsistency inconsistency = checkChainingCorrectness(sorted);
		if (inconsistency != null) {return inconsistency;}
		return null;
	}
}
