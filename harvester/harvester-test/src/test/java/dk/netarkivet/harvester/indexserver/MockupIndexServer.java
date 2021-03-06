/*
 * #%L
 * Netarchivesuite - harvester - test
 * %%
 * Copyright (C) 2005 - 2017 The Royal Danish Library, 
 *             the National Library of France and the Austrian National Library.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package dk.netarkivet.harvester.indexserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;
import javax.jms.MessageListener;

import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.distribute.Channels;
import dk.netarkivet.common.distribute.JMSConnection;
import dk.netarkivet.common.distribute.JMSConnectionFactory;
import dk.netarkivet.common.distribute.RemoteFile;
import dk.netarkivet.common.distribute.RemoteFileFactory;
import dk.netarkivet.common.distribute.indexserver.RequestType;
import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.harvester.indexserver.distribute.IndexRequestMessage;
import dk.netarkivet.testutils.preconfigured.TestConfigurationIF;

/**
 * A fake IndexServer that gives one or more files back as specified in its constructor.
 */
public class MockupIndexServer implements TestConfigurationIF, MessageListener {
    private File resultFile;
    private boolean responseOK = true;
    private List<IndexRequestMessage> msgs = new ArrayList<IndexRequestMessage>();
    private String origDir;

    /**
     * Create a new MockupIndexServer that serves back the given file or directory of files.
     *
     * @param resultFile Files that this IndexServer should return upon request. The file (or files if resultFile is a
     * directory) should be gzipped, as they will be ungzipped in the receiving end.
     */
    public MockupIndexServer(File resultFile) {
        this.resultFile = resultFile;
    }

    public void setUp() {
        setResponseSuccessfull(true);
        origDir = Settings.get(CommonSettings.CACHE_DIR);
        Settings.set(CommonSettings.CACHE_DIR, Settings.get(CommonSettings.DIR_COMMONTEMPDIR));
        JMSConnectionFactory.getInstance().setListener(Channels.getTheIndexServer(), this);
    }

    public void tearDown() {
        JMSConnectionFactory.getInstance().removeListener(Channels.getTheIndexServer(), this);
        Settings.set(CommonSettings.CACHE_DIR, origDir);
    }

    public void setResponseSuccessfull(boolean isOk) {
        responseOK = isOk;
    }

    public void resetMsgList() {
        msgs.clear();
    }

    public List<IndexRequestMessage> getMsgList() {
        return msgs;
    }

    public void onMessage(Message message) {
        IndexRequestMessage irm = (IndexRequestMessage) JMSConnection.unpack(message);
        msgs.add(irm);
        irm.setFoundJobs(irm.getRequestedJobs());
        if (irm.getRequestType() == RequestType.CDX) {
            RemoteFile resultFile = RemoteFileFactory.getInstance(this.resultFile, true, false, true);
            irm.setResultFile(resultFile);
        } else {
            List<RemoteFile> resultFiles = new ArrayList<RemoteFile>();
            File[] files = resultFile.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        resultFiles.add(RemoteFileFactory.getInstance(f, true, false, true));
                    }
                }
            }
            irm.setResultFiles(resultFiles);
        }
        if (!responseOK) {
            irm.setNotOk("Test setting");
        }
        JMSConnectionFactory.getInstance().reply(irm);
    }
}
