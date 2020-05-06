/*******************************************************************************
 * Copyright (c) 2019 Paul Stahr
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package gui;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.DataHandler;
import data.JFrameLookAndFeelUtil;
import data.ProgrammData;
import util.JFrameUtils;
import util.io.StreamUtil;
import util.jframe.JComponentSingletonInstantiator;

/** 
* Dieses Fenster zeigt die Version und Autoren des Programmes an
* @author  Paul Stahr
* @version 12.03.2012
*/

public class Credits extends JFrame
{
	private static final Logger logger = LoggerFactory.getLogger(Credits.class);

	private static final long serialVersionUID = 6303233656713618087L;
	private static final JComponentSingletonInstantiator<JFrame> instantiator = new JComponentSingletonInstantiator<JFrame>(Credits.class);

    
    public static final ActionListener getOpenWindowListener()
    {
    	return instantiator;
    }
    
    public Credits(){
    	super("\u00DCber");
    	final JLabel text = new JLabel();
        setLayout(JFrameUtils.SINGLE_COLUMN_LAYOUT);
        try {
			InputStream stream = DataHandler.getResourceAsStream("credits.txt");
			text.setText(StreamUtil.readStreamToString(stream)
					.replace("$progversion", ProgrammData.getVersion())
					.replace("$progname", ProgrammData.name));
			stream.close();
		} catch (IOException e) {
			logger.error("Can't read licence", e);
		}
        add(text);
        pack();
        setResizable(false);      
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JFrameLookAndFeelUtil.addToUpdateTree(this);
    }
}
