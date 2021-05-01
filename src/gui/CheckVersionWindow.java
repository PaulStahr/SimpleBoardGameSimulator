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

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

public class CheckVersionWindow extends JFrame implements Runnable, LanguageChangeListener, ActionListener
{
    private static final Logger logger = LoggerFactory.getLogger(CheckVersionWindow.class);
    private static final long serialVersionUID = -6479080661365866948L;
    private static final JComponentSingletonInstantiator<CheckVersionWindow> instantiator = new JComponentSingletonInstantiator<CheckVersionWindow>(CheckVersionWindow.class);
    private final JLabel labelOwnVersion        = new JLabel();
    private final JLabel versionOwnVersion      = new JLabel(ProgrammData.getVersion());
    private final JLabel labelCurrentMasterVersion     = new JLabel();
    private final JLabel versionCurrentMasterVersion   = new JLabel();
    private final JLabel labelCurrentDevVersion     = new JLabel();
    private final JLabel versionCurrentDevVersion   = new JLabel();
    private final JEditorPane editorPaneChangelog= new JEditorPane();
    private final JScrollPane scrollPaneChangelog= new JScrollPane(editorPaneChangelog);
    private final JButton buttonDownloadMaster        = new JButton("Herunterladen");
    private final JButton buttonDownloadDev        = new JButton("Herunterladen");
    boolean isDownloading = false;
    private static LanguageHandler lh;

    public static void setLanguageHandler(LanguageHandler lh){CheckVersionWindow.lh = lh;}

    @Override
    public void run (){
        try {
        final List<ProgrammData.Version> versionListMaster = ProgrammData.getRemoteVersionList(ProgrammData.Branch.MASTER);
        JFrameUtils.runByDispatcher(new Runnable() {
            @Override
            public void run() {
                ProgrammData.Version currentMasterVersion = ProgrammData.getHighestVerion(versionListMaster);
                versionCurrentMasterVersion.setText(currentMasterVersion.code);
                logger.info("Downloaded online version list");
            }
        });
        }catch(Exception e) {logger.error("Couldn't fetch version information", e);}
        try {
        final List<ProgrammData.Version> versionListDev = ProgrammData.getRemoteVersionList(ProgrammData.Branch.DEV);
        JFrameUtils.runByDispatcher(new Runnable() {
            @Override
            public void run() {
                ProgrammData.Version currentDevVersion = ProgrammData.getHighestVerion(versionListDev);
                editorPaneChangelog.setText(convertToHtml(versionListDev));
                versionCurrentDevVersion.setText(currentDevVersion.code);
                logger.info("Downloaded online version list");
            }
        });
        }catch(Exception e) {logger.error("Couldn't fetch version information", e);}
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
                        .addComponent(labelCurrentMasterVersion)
                        .addComponent(labelCurrentDevVersion)
                    ).addGap(5).addGroup(layout.createParallelGroup()
                        .addComponent(versionOwnVersion)
                        .addComponent(versionCurrentMasterVersion)
                        .addComponent(versionCurrentDevVersion)
                    ).addGap(5).addGroup(layout.createParallelGroup()
                        .addComponent(buttonDownloadDev)
                        .addComponent(buttonDownloadMaster))
                ).addComponent(scrollPaneChangelog)
            ).addGap(5)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGap(5).addGroup(layout.createParallelGroup()
                .addComponent(labelOwnVersion, Alignment.CENTER)
                .addComponent(versionOwnVersion, Alignment.CENTER)
            ).addGap(5).addGroup(layout.createParallelGroup()
                .addComponent(labelCurrentMasterVersion, Alignment.CENTER)
                .addComponent(versionCurrentMasterVersion, Alignment.CENTER)
                .addComponent(buttonDownloadMaster, Alignment.CENTER)
            ).addGap(5).addGroup(layout.createParallelGroup()
                .addComponent(labelCurrentDevVersion, Alignment.CENTER)
                .addComponent(versionCurrentDevVersion, Alignment.CENTER)                
                .addComponent(buttonDownloadDev, Alignment.CENTER)
            ).addGap(5).addComponent(scrollPaneChangelog, 0, 300, 10000).addGap(5)
        );

        editorPaneChangelog.setContentType("text/html"); 
        editorPaneChangelog.setEditable(false);

        buttonDownloadDev.addActionListener(this);
        buttonDownloadMaster.addActionListener(this);

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
        labelCurrentMasterVersion.setText(language.getString(Words.current_version));
        labelCurrentDevVersion.setText(language.getString(Words.current_developement_version));
        versionCurrentMasterVersion.setText(language.getString(Words.loading));
    }

    public static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                logger.error("Couldn't open browser", e);
            }
        }
        return false;
    }

    public static boolean openWebpage(URL url) {
        try {
            return openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            logger.error("Couldn't parse uril",e);
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Object source = arg0.getSource();
        if (source == buttonDownloadMaster)
        {
            try {
                openWebpage(new URL("https://github.com/PaulStahr/SimpleBoardGameSimulator"));
            } catch (MalformedURLException e) {
               logger.error("Couldn't parse url", e);
            }
        }
        else if (source == buttonDownloadDev)
        {
            try {
                openWebpage(new URL("https://github.com/PaulStahr/SimpleBoardGameSimulator/tree/dev"));
            } catch (MalformedURLException e) {
                logger.error("Couldn't parse url", e);
            }
        }
    }
}
