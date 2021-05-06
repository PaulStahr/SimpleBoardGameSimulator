package data.controls;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class UserControl {
    private final ControlCombination[][] controlCombinations = new ControlCombination[ControlTypes.values().length][];

    public UserControl() {
        controlCombinations[ControlTypes.MOVE_BOARD.ordinal()]      = new ControlCombination[] {new ControlCombination(InputEvent.ALT_DOWN_MASK, 0,  -1,0), new ControlCombination(0, -1,  -1,16)};
        controlCombinations[ControlTypes.SHUFFLE.ordinal()]         = new ControlCombination[] {new ControlCombination(0, -1, KeyEvent.VK_S, 0)};
        controlCombinations[ControlTypes.FLIP.ordinal()]            = new ControlCombination[] {new ControlCombination(0, -1,  KeyEvent.VK_F, 0)};
        controlCombinations[ControlTypes.FLIP_STACK.ordinal()]      = new ControlCombination[] {new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_F, 0)};
        controlCombinations[ControlTypes.DROP.ordinal()]            = new ControlCombination[] {new ControlCombination(0, -1, KeyEvent.VK_D, 0)};
        controlCombinations[ControlTypes.DROP_ALL.ordinal()]        = new ControlCombination[] {new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1, KeyEvent.VK_D, 0)};
        controlCombinations[ControlTypes.COUNT.ordinal()]           = new ControlCombination[] {new ControlCombination(0, -1,  KeyEvent.VK_C, 0)};
        controlCombinations[ControlTypes.COUNT_VALUES.ordinal()]    = new ControlCombination[] {new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_C, 0)};
        controlCombinations[ControlTypes.SIT_DOWN.ordinal()]        = new ControlCombination[] {new ControlCombination(0, -1,  KeyEvent.VK_SPACE, 0)};
        controlCombinations[ControlTypes.FIX.ordinal()]             = new ControlCombination[] {new ControlCombination(InputEvent.ALT_DOWN_MASK, -1, KeyEvent.VK_F, 0)};
        controlCombinations[ControlTypes.ROTATE.ordinal()]          = new ControlCombination[] {new ControlCombination(0, -1,  KeyEvent.VK_R, 0)};
        controlCombinations[ControlTypes.HIDE_PRIVATE_AREA.ordinal()] = new ControlCombination[] {new ControlCombination(0, -1,  KeyEvent.VK_H, 0)};
        controlCombinations[ControlTypes.COLLECT_SELECTED.ordinal()]= new ControlCombination[] {new ControlCombination(0, -1, KeyEvent.VK_M, 0)};
        controlCombinations[ControlTypes.COLLECT_ALL.ordinal()]     = new ControlCombination[] {new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_M, 0)};
        controlCombinations[ControlTypes.COLLECT_ALL_WITH_HANDS.ordinal()] = new ControlCombination[] {new ControlCombination(InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK, -1, KeyEvent.VK_M, 0)};
        controlCombinations[ControlTypes.DISSOLVE_STACK.ordinal()]  = new ControlCombination[] {new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  KeyEvent.VK_R, 0)};
        controlCombinations[ControlTypes.TABLE_VISIBLE.ordinal()]   = new ControlCombination[] {new ControlCombination(InputEvent.ALT_DOWN_MASK, -1,  KeyEvent.VK_T, 0)};
        controlCombinations[ControlTypes.TAKE.ordinal()]            = new ControlCombination[] {new ControlCombination(0, -1, KeyEvent.VK_T, 0)};
        controlCombinations[ControlTypes.GIVE.ordinal()]            = new ControlCombination[] {new ControlCombination(0, -1, KeyEvent.VK_G, 0)};
        controlCombinations[ControlTypes.SORT.ordinal()]            = new ControlCombination[] {new ControlCombination(0, -1, KeyEvent.VK_S, 0)};
        controlCombinations[ControlTypes.VIEW_COLLECT_STACK.ordinal()] = new ControlCombination[] {new ControlCombination(0, -1,  KeyEvent.VK_V, 0)};
        controlCombinations[ControlTypes.GET_BOTTOM_CARD.ordinal()] = new ControlCombination[] {new ControlCombination(InputEvent.SHIFT_DOWN_MASK, -1,  -1, 2)};
        controlCombinations[ControlTypes.PLAY.ordinal()]            = new ControlCombination[] {new ControlCombination(0, -1, KeyEvent.VK_P, 0)};
    }

    public boolean check(ControlTypes ct, int keyCode, int keyModifier)
    {
        for (ControlCombination cc : controlCombinations[ct.ordinal()])
        {
            if (cc.keyCode == keyCode && cc.keyModifier == keyModifier)
            {
                return true;
            }
        }
        return false;
    }

    public ControlCombination get(ControlTypes ct, int i) {
        return controlCombinations[ct.ordinal()][i];
    }

    public ControlCombination[] get(ControlTypes ct) {
        return controlCombinations[ct.ordinal()];
    }
}
