package gui.game;

import data.controls.ControlCombination;
import data.controls.ControlTypes;
import data.controls.UserControl;
import gui.language.Language;
import gui.language.Words;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class UserControlString {
    private static final UserControl uc = new UserControl();

    public final UserControlString.FuncControl[] boardControls = {
            new UserControlString.FuncControl(Words.move_board, null, false, new ControlCombination(InputEvent.ALT_DOWN_MASK, 0,  -1,0), new ControlCombination(0, -1,  -1,16)),
            new UserControlString.FuncControl(Words.rotate_board, null, false, new ControlCombination(InputEvent.ALT_DOWN_MASK, 2,  -1, 0), new ControlCombination(0, -1,  KeyEvent.VK_UP, 0), new ControlCombination(0, -1,  KeyEvent.VK_DOWN, 0)),
            new UserControlString.FuncControl(Words.zoom, null, false, new ControlCombination(InputEvent.ALT_DOWN_MASK, -1,  -1, 4), new ControlCombination(0, -1,  KeyEvent.VK_PLUS, 0), new ControlCombination(0, -1,  KeyEvent.VK_MINUS, 0)),
            new UserControlString.FuncControl(Words.sit_to_own_seat, ControlTypes.SIT_DOWN_OWN_PLACE, true),
            new UserControlString.FuncControl(Words.sit_down, ControlTypes.SIT_DOWN, false),
            new UserControlString.FuncControl(Words.hide_and_show_hand_area, ControlTypes.HIDE_PRIVATE_AREA, true),
            new UserControlString.FuncControl(Words.hide_show_table, ControlTypes.HIDE_SHOW_TABLE, true),
    };

    public final UserControlString.FuncControl[] objectControls= {
            new UserControlString.FuncControl(Words.select_object, ControlTypes.SELECT_OBJECT, false),
            new UserControlString.FuncControl(Words.select_multiple, null, false, new ControlCombination(InputEvent.CTRL_DOWN_MASK, 0,  -1,0)),
            new UserControlString.FuncControl(Words.move_object, 		ControlTypes.MOVE_OBJECT, false),
            new UserControlString.FuncControl(Words.rotate_object, ControlTypes.ROTATE, true),
            new UserControlString.FuncControl(Words.zoom, null, false, new ControlCombination(InputEvent.ALT_DOWN_MASK, -1,  -1, 4), new ControlCombination(0, -1,  KeyEvent.VK_PLUS, 0), new ControlCombination(0, -1,  KeyEvent.VK_MINUS, 0)),
            new UserControlString.FuncControl(Words.fix_object, ControlTypes.FIX, false),
    };

    public final UserControlString.FuncControl[] cardControls= {
            new UserControlString.FuncControl(Words.move_stack,	 	ControlTypes.MOVE_STACK, false),
            new UserControlString.FuncControl(Words.shuffle_stack, 	ControlTypes.SHUFFLE, true),
            new UserControlString.FuncControl(Words.merge_objects, 	ControlTypes.COLLECT_SELECTED, true),
            new UserControlString.FuncControl(Words.distribute_cards_to_players, ControlTypes.DEAL_OBJECTS, true),
            new UserControlString.FuncControl(Words.take_object_to_hand, ControlTypes.TAKE, true, uc.get(ControlTypes.TAKE,0), new ControlCombination(0, 0,  -1, 1)),
            new UserControlString.FuncControl(Words.take_object_to_hand_face_down, ControlTypes.TAKE_FACE_DOWN, true),
            new UserControlString.FuncControl(Words.play_card_face_up, ControlTypes.PLAY, true),
            new UserControlString.FuncControl(Words.play_card_face_down, ControlTypes.PLAY_FACE_DOWN, true),
            new UserControlString.FuncControl(Words.collect_all_objects_of_a_group,ControlTypes.COLLECT_ALL, false),
            new UserControlString.FuncControl(Words.collect_all_objects_of_a_group_plus_hand, ControlTypes.COLLECT_ALL_WITH_HANDS, false),
            new UserControlString.FuncControl(Words.flip_card, ControlTypes.FLIP, true),
            new UserControlString.FuncControl(Words.get_top_n_card, ControlTypes.GET_TOP_N_CARDS, false),
            new UserControlString.FuncControl(Words.get_bottom_card, ControlTypes.GET_BOTTOM_CARD, false),
    };
    public final UserControlString.FuncControl[] figureControls= {
    };

    public final UserControlString.FuncControl[] diceControls= {
            new UserControlString.FuncControl(Words.roll_dice, ControlTypes.FLIP, true),
            new UserControlString.FuncControl(Words.unfold_dice, ControlTypes.VIEW, true),
    };

    public final UserControlString.FuncControl[] bookControls= {
            new UserControlString.FuncControl(Words.previous_page, ControlTypes.PREVIOUS_PAGE, true),
            new UserControlString.FuncControl(Words.next_page, ControlTypes.NEXT_PAGE, true),
    };

    public final UserControlString.FuncControl[] boxControls= {
            new UserControlString.FuncControl(Words.unpack_box, ControlTypes.UNPACK_BOX, true),
            new UserControlString.FuncControl(Words.pack_box, ControlTypes.PACK_BOX, true),
    };

    public final UserControlString.FuncControl[] privateAreaControls= {
            new UserControlString.FuncControl(Words.hide_and_show_hand_area, ControlTypes.HIDE_PRIVATE_AREA, true),
            new UserControlString.FuncControl(Words.distribute_cards_to_players, ControlTypes.DEAL_OBJECTS, false),
            new UserControlString.FuncControl(Words.take_object_to_hand, ControlTypes.TAKE, false, uc.get(ControlTypes.TAKE,0), new ControlCombination(0, 0,  -1, 1)),
            new UserControlString.FuncControl(Words.take_object_to_hand_face_down, null, false, new ControlCombination(0, 2,  -1,1)),
            new UserControlString.FuncControl(Words.play_card_face_up, null, false, new ControlCombination(0, 1,  -1, 8)),
            new UserControlString.FuncControl(Words.play_card_face_down, null, false, new ControlCombination(0, 2,  -1, 8)),
            new UserControlString.FuncControl(Words.play_active_hand_card, ControlTypes.PLAY,false, uc.get(ControlTypes.PLAY, 0), new ControlCombination(0, 0, -1, 32)),
            new UserControlString.FuncControl(Words.drop_all_hand_cards, ControlTypes.DROP_ALL, false),
            new UserControlString.FuncControl(Words.drop_active_hand_card, ControlTypes.DROP, false),
            new UserControlString.FuncControl(Words.sort_hand_cards, ControlTypes.SORT, true),
            new UserControlString.FuncControl(Words.zoom, null, false, new ControlCombination(0, -1,  -1, 4), new ControlCombination(0, -1,  KeyEvent.VK_PLUS, 0), new ControlCombination(0, -1,  KeyEvent.VK_MINUS, 0)),
    };

    public final UserControlString.FuncControl[] countControls = {
            new UserControlString.FuncControl(Words.count_card_number, ControlTypes.COUNT, true),
            new UserControlString.FuncControl(Words.count_card_value, ControlTypes.COUNT_VALUES, true),
    };

    public static class FuncControl
    {
        public Words word;
        public ControlCombination[] cc;
        public ControlTypes controlTypes;
        public boolean hasAction;

        public FuncControl(Words word, ControlTypes controlTypes, boolean hasAction, ControlCombination... cc){
            this.word = word;
            this.controlTypes = controlTypes;
            if (controlTypes != null){
                this.cc = uc.get(controlTypes);
            }
            else {
                this.cc = cc;
            }
            this.hasAction = hasAction;
        }

        public String toString(Language lang) {
            StringBuilder strB = new StringBuilder();
            if (cc != null) {
                for (int i = 0; i < cc.length; ++i) {
                    if (i != 0) {
                        strB.append(", ");
                    }
                    cc[i].toString(strB, lang);
                }
            }
            return strB.toString();
        }
    }
}
