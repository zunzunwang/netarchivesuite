/*
 * #%L
 * Netarchivesuite - common - test
 * %%
 * Copyright (C) 2005 - 2014 The Royal Danish Library, the Danish State and University Library,
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
 package dk.netarkivet.common.distribute;

 import java.util.Calendar;

import org.junit.Ignore;

import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.common.utils.TimeUtils;

/**
 * Testclass for testing the exceptionhandling in JMSConnection.
 */
@SuppressWarnings({ "unused", "serial"})
@Ignore("Not present in TestSuite")
public class AlternateJMSConnectionTester {

    public void errorcodesTest() {
        Settings.set(JMSConnectionSunMQ.JMS_BROKER_PORT, "7677");
        JMSConnection con = JMSConnectionFactory.getInstance();
        NetarkivetMessage msg;
        int msgNr = 0;
        while (msgNr < 50) {
             msg = new TestMessage(Channels.getError(), Channels.getTheRepos(), "testID" + msgNr);
             System.out.println("Sending message " +  msgNr);
             con.send(msg);
             System.out.println("Message " +  msgNr +  " now sent");
             TimeUtils.exponentialBackoffSleep(1, Calendar.MINUTE);
             msgNr++;
        }
        con.cleanup();
    }

	private static class TestMessage extends NetarkivetMessage {
        private String testID;

        public TestMessage(ChannelID to, ChannelID replyTo, String testID) {
            super(to, replyTo);
            this.testID = testID;
        }

        public String getTestID() {
            return testID;
        }
    }
}
