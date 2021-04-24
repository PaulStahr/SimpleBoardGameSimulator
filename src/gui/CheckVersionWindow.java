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

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.DataHandler;
import data.JFrameLookAndFeelUtil;
import data.ProgrammData;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;
import util.JFrameUtils;
import util.jframe.JComponentSingletonInstantiator;

/** 
* @author  Paul Stahr
* @version 04.02.2012
*/

public class CheckVersionWindow extends JFrame implements Runnable, LanguageChangeListener
{
    private static final Logger logger = LoggerFactory.getLogger(CheckVersionWindow.class);
    private static final long serialVersionUID = -6479080661365866948L;
    private static final JComponentSingletonInstantiator<CheckVersionWindow> instantiator = new JComponentSingletonInstantiator<CheckVersionWindow>(CheckVersionWindow.class);
    private final JLabel labelOwnVersion        = new JLabel();
    private final JLabel versionOwnVersion      = new JLabel(ProgrammData.getVersion());
    private final JLabel labelCurrentVersion     = new JLabel();
    private final JLabel versionCurrentVersion   = new JLabel();
    private final JEditorPane editorPaneChangelog= new JEditorPane();
    private final JScrollPane scrollPaneChangelog= new JScrollPane(editorPaneChangelog);
    private final JButton buttonDownload        = new JButton("Herunterladen");
    boolean isDownloading = false;
    private static LanguageHandler lh;

    public static void setLanguageHandler(LanguageHandler lh){CheckVersionWindow.lh = lh;}

    @Override
    public void run (){
        //try {
            final List<ProgrammData.Version> versionList = ProgrammData.getRemoteVersionList();
            //final boolean isNewer = chLog != nuTll && ProgrammData.isNewer(chLog.version);
            //versionActualVersion.setText(isNewer ? chLog.version.concat(" (aktueller)") : chLog.version);
            JFrameUtils.runByDispatcher(new Runnable() {
                @Override
                public void run() {
                    long maxVersion = Integer.MIN_VALUE;
                    String code = "";
                    for (int i = 0; i < versionList.size(); ++i)
                    {
                        ProgrammData.Version v = versionList.get(i);
                        if(ProgrammData.getLongOfVersion(v.code) > maxVersion)
                        {
                            code = v.code;
                            maxVersion = ProgrammData.getLongOfVersion(code);
                        }
                    }
                    editorPaneChangelog.setText(convertToHtml(versionList));
                    versionCurrentVersion.setText(code);
                    logger.info("Downloaded online version list");
                }
            });
        //} catch (InterruptedException e) {
          //  logger.error("Unexpcted interrupt");
        //}
        isDownloading = false;
    }
    
    private String convertToHtml(List<ProgrammData.Version> version)
    {
        StringBuilder strB = new StringBuilder();
        strB.append("<html><body><table border=\"1\">");
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
        for (int i = 0; i < version.size(); ++i) {
            ProgrammData.Version v = version.get(i);
            strB.append("<tr><td bgcolor=\"gray\">").append(v.code).append("<tab id=t1> ").append(format.format(v.date)).append("<tab id=t2> ").append(v.title).append("</td></tr>");
            strB.append("<tr><td colspan=\"2\"><ul>");
            for (int j = 0; j < v.notices.size(); ++j)
            {
                strB.append("<li>").append(v.notices.get(j)).append("</li>");
            }
            strB.append("</ul></td></tr>");        
        }
        return strB.append("</table></body></html>").toString();
    }

    public static final ActionListener getOpenWindowListener(){return instantiator;}

    public static final synchronized CheckVersionWindow getInstance(){return instantiator.get();}

    public CheckVersionWindow(){
        languageChanged(lh.getCurrentLanguage());
        final GroupLayout layout = new GroupLayout(getContentPane());
        setLayout(layout);

        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGap(5).addGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(labelOwnVersion)
                        .addComponent(labelCurrentVersion)
                    ).addGap(5).addGroup(layout.createParallelGroup()
                        .addComponent(versionOwnVersion)
                        .addComponent(versionCurrentVersion)
                    ).addGap(5).addComponent(buttonDownload)
                ).addComponent(scrollPaneChangelog)
            ).addGap(5)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGap(5).addGroup(layout.createParallelGroup()
                .addComponent(labelOwnVersion, Alignment.CENTER)
                .addComponent(versionOwnVersion, Alignment.CENTER)
            ).addGap(5).addGroup(layout.createParallelGroup()
                .addComponent(labelCurrentVersion, Alignment.CENTER)
                .addComponent(versionCurrentVersion, Alignment.CENTER)
                .addComponent(buttonDownload, Alignment.CENTER)
            ).addGap(5).addComponent(scrollPaneChangelog, 0, 300, 10000).addGap(5)
        );

        editorPaneChangelog.setContentType("text/html"); 
        editorPaneChangelog.setEditable(false);

        setTitle("Version");
        setResizable(true);      

        //setIconImage(ProgramIcons.webIcon.getImage());
        setMinimumSize(new Dimension(500, 200));
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JFrameLookAndFeelUtil.addToUpdateTree(this);
        List<ProgrammData.Version> versionList = ProgrammData.getLocalVersionList();
        editorPaneChangelog.setText(convertToHtml(versionList));
        lh.addLanguageChangeListener(this);
    }

    @Override
    public void setVisible(boolean vis){
        if (vis && !isDownloading){
            isDownloading = true;
            DataHandler.tp.run(this, "Download Info");
        }
        super.setVisible(vis);
    }

    @Override
    public void languageChanged(Language language) {
        labelOwnVersion.setText(language.getString(Words.your_version));
        labelCurrentVersion.setText(language.getString(Words.current_version));
        versionCurrentVersion.setText(language.getString(Words.loading));
    }
}
