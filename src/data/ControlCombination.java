package data;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import gui.Language;
import gui.Words;

public class ControlCombination
{
	final int keyModifier;
	final int mouse;
	//final char key;
	final int additional;
	final int keyCode;
	
	public ControlCombination(int keyModifier, int mouse, int keyCode, int additional)
	{
		this.keyModifier = keyModifier;
		this.mouse = mouse;
		this.additional = additional;
		this.keyCode = keyCode;
	}
	
	StringBuilder appendPlus(StringBuilder strB)
	{
		if (strB.length() != 0)
		{
			strB.append(' ').append('+').append(' ');
		}
		return strB;
	}
	
	public String toString(Language lang)
	{
		return toString(new StringBuilder(), lang).toString();
	}

	public StringBuilder toString(StringBuilder strB, Language lang) {
		if ((keyModifier & InputEvent.SHIFT_DOWN_MASK) != 0)
		{
			appendPlus(strB).append(lang.getString(Words.shift));
		}
		if ((keyModifier & InputEvent.CTRL_DOWN_MASK) != 0)
		{
			appendPlus(strB).append(lang.getString(Words.ctrl));
		}
		if ((keyModifier & InputEvent.ALT_DOWN_MASK) != 0)
		{
			appendPlus(strB).append(lang.getString(Words.alt));
		}

		if (keyCode != -1){
			appendPlus(strB).append(KeyEvent.getKeyText(keyCode));
		}
		if (mouse != -1)
		{
			switch(mouse)
			{
				case 0: appendPlus(strB).append(lang.getString(Words.left_click));break;
				case 1: appendPlus(strB).append(lang.getString(Words.middle_click));break;
				case 2: appendPlus(strB).append(lang.getString(Words.right_click));break;
				default: throw new IllegalArgumentException("MouseFunction not known");
			}
		}
		if ((additional & 1) != 0)
		{
			appendPlus(strB).append(lang.getString(Words.drag));
		}
		if ((additional & 2) != 0)
		{
			appendPlus(strB).append(lang.getString(Words.grab));
		}
		if ((additional & 4) != 0)
		{
			appendPlus(strB).append(lang.getString(Words.rotate_wheel));			
		}
		if ((additional & 8) != 0) {
			appendPlus(strB).append(lang.getString(Words.drop_on_board));
		}
		if ((additional & 16) != 0) {
			appendPlus(strB).append(lang.getString(Words.arrow_keys));
		}
		if ((additional & 32) != 0) {
			appendPlus(strB).append(lang.getString(Words.double_click));
		}

		return strB;
	}
}
