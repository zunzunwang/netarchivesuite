/*
 * #%L
 * Netarchivesuite - deploy - test
 * %%
 * Copyright (C) 2005 - 2017 The Royal Danish Library, 
 *       the National Library of France and the Austrian National Library.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueBrowser;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import dk.netarkivet.common.CommonSettings;
import dk.netarkivet.common.utils.RememberNotifications;
import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.testutils.LogbackRecorder;
import dk.netarkivet.testutils.preconfigured.PreventSystemExit;
import dk.netarkivet.testutils.preconfigured.ReloadSettings;

/**
 * Tests JMSConnection, the class that handles all JMS operations for Netarkivet. Currently disabled as this requires a
 * jms broker to be running. Consider moving this class to the integration test phase and perhaps with an embedded broker?
 */
@Ignore
public class IntegrityTestSuite {
    /**
     * We need two arbitrary (but different) queues for testing send and reply.
     */
    private static final ChannelID sendQ = Channels.getAnyBa();

    private static final ChannelID replyQ = Channels.getTheCR();

    private static final ChannelID sendTopic = Channels.getAllBa();

    private static final int WAIT_MS = 3000;

    /**
     * Used in methods testNListenersToTopic and testMoreThanThreeListenersToQueue, should be set to > 3:
     */
    private static final int NO_OF_LISTENERS = 4;

    private static final PreventSystemExit pes = new PreventSystemExit();

    private JMSConnection conn;

    ReloadSettings rs = new ReloadSettings();
    private LogbackRecorder logbackRecorder;

    @Before
    public void setUp() {
        rs.setUp();
        Settings.set(CommonSettings.JMS_BROKER_CLASS, JMSConnectionSunMQ.class.getName());
        /* Do not send notification by email. Print them to STDOUT. */
        Settings.set(CommonSettings.NOTIFICATIONS_CLASS, RememberNotifications.class.getName());
        JMSConnectionFactory.getInstance().cleanup();
        conn = JMSConnectionFactory.getInstance();
        pes.setUp();
        logbackRecorder = LogbackRecorder.startRecorder();
    }

    @After
    public void tearDown() {
        logbackRecorder.stopRecorder();
        Channels.reset();
        JMSConnectionFactory.getInstance().cleanup();
        pes.tearDown();
        rs.tearDown();
    }

    /**
     * Verify that we can remove a given MessageListener from a given Queue. Note that this method does not test that a
     * MessageListener can removed from a Topic - at the moment we have no need for that. If the need arises, a test
     * case should be written for Topics as well.
     */
    @Test
    public void testRemoveListener() {
        TestMessage testMsg = new TestMessage(sendQ, replyQ);
        TestMessageListener listener1 = new TestMessageListener(testMsg);
        TestMessageListener listener2 = new TestMessageListener(testMsg);
        TestMessageListener listener3 = new TestMessageListener(testMsg);
        conn.setListener(sendQ, listener1);
        conn.setListener(sendQ, listener2);

        synchronized (this) {
            conn.send(testMsg);
            try {
                wait(WAIT_MS);
            } catch (InterruptedException e) {}
        }

        // The test is ok if exactly one of the listeners is ok.
        boolean ok = listener1.getOk() ? !listener2.getOk() : listener2.getOk();
        assertTrue("Expected test message '" + testMsg.toString() + "'\nto be received by exactly one listener within "
                + WAIT_MS + " milliseconds.", ok);

        // Removing listener1 - test that a message will only be received by
        // listener2:
        // This also tests fix of bug 235:
        conn.removeListener(sendQ, listener1);
        listener1.resetOkState();
        listener2.resetOkState();

        synchronized (this) {
            conn.send(testMsg);
            try {
                wait(WAIT_MS);
            } catch (InterruptedException e) {}
        }

        assertTrue("Expected test message " + testMsg.toString() + "\nshould be received by listener2 only",
                !listener1.getOk() && listener2.getOk());

        // Now removing listener2 and setting a third listener
        // Test that a message is neither received by listener1 nor listener2:
        conn.removeListener(sendQ, listener2);
        conn.setListener(sendQ, listener3);
        listener1.resetOkState();
        listener2.resetOkState();
        listener3.resetOkState();

        synchronized (this) {
            conn.send(testMsg);
            try {
                wait(WAIT_MS);
            } catch (InterruptedException e) {}
        }

        assertTrue("Expected test message " + testMsg.toString() + "\nshould not be received by neither listener1 "
                + "nor listener2", !listener1.getOk() && !listener2.getOk());

        conn.setListener(sendQ, listener1);

        // This should be quite fast, the message is already waiting.
        synchronized (this) {
            try {
                if (!listener1.getOk()) {
                    wait(WAIT_MS);
                }
            } catch (InterruptedException e) {}
        }

        // The test is ok if exactly one of the listeners is ok.
        boolean okAfterRemovalAndReset = listener1.getOk() ? !listener3.getOk() : listener3.getOk();
        assertTrue("Expected test message " + testMsg.toString() + "\nto be received by either listener1 or listener3 "
                + "after reading it", okAfterRemovalAndReset);

    }

    /**
     * Verify that a sent message is only delivered to one listener (this is point-to-point semantics).
     * <p>
     * This is an integrity test because: It tests that JMS behaves as expected.
     */
    @Test
    public void testTwoListenersSend() {
        TestMessage testMsg = new TestMessage(sendQ, replyQ);
        TestMessageListener listener1 = new TestMessageListener(testMsg);
        TestMessageListener listener2 = new TestMessageListener(testMsg);
        conn.setListener(sendQ, listener1);
        conn.setListener(sendQ, listener2);
        synchronized (this) {
            conn.send(testMsg);
            try {
                wait(WAIT_MS);
            } catch (InterruptedException e) {}
        }

        // The test is ok if exactly one of the listeners is ok.
        int listeners = 0;
        if (listener1.getOk()) {
            listeners++;
        }
        if (listener2.getOk()) {
            listeners++;
        }
        assertEquals("Expected test message: (" + testMsg.toString()
                + ") to be received by exactly one listener within " + WAIT_MS + " milliseconds.", 1, listeners);
    }

    /**
     * Test that we can subscribe more than three (3) listeners to a queue and that exactly one receives the message
     * <p>
     * This is an integrity test because: We are testing that JMS itself behaves correctly.
     * <p>
     * This is used for testing the Platform Ed. Enterprise License feature of queue Requires a running broker with
     * Platform Ed. Enterprise License (e.g. trial license: /opt/imq/bin/imqbrokerd -license try)
     */
    @Test
    public void testMoreThanThreeListenersToQueue() {
        TestMessage testMsg = new TestMessage(sendQ, replyQ);
        List<MessageListener> listeners = new ArrayList<MessageListener>(NO_OF_LISTENERS);

        for (int i = 0; i < NO_OF_LISTENERS; i++) {
            TestMessageListener aListener = new TestMessageListener(testMsg);
            listeners.add(aListener);
            conn.setListener(sendQ, aListener);
        }

        synchronized (this) {
            conn.send(testMsg);
            // Listen for two notifies in case two messages are received
            for (int i = 0; i < 2; i++) {
                try {
                    wait(WAIT_MS);
                } catch (InterruptedException e) {}
            }
        }

        int oks = 0;
        for (int i = 0; i < NO_OF_LISTENERS; i++) {
            if (((TestMessageListener) listeners.get(i)).getOk()) {
                ++oks;
            }
        }

        assertTrue("Expected test message " + testMsg.toString() + "to be received by exactly 1 of the "
                + NO_OF_LISTENERS + " listeners within " + WAIT_MS + " milliseconds. Received " + oks, (oks == 1));
    }

    /**
     * Verify that a sent message arrives unchanged to a listener.
     */
    @Test
    public void testListenAndSend() {
        TestMessage testMsg = new TestMessage(sendQ, replyQ);
        TestMessageListener listener = new TestMessageListener(testMsg);
        conn.setListener(sendQ, listener);
        synchronized (this) {
            conn.send(testMsg);
            try {
                wait(WAIT_MS);
            } catch (InterruptedException e) {}
        }
        assertTrue("Expected test message >" + testMsg.toString() + "< to have arrived on queue " + replyQ + " within "
                + WAIT_MS + " milliseconds.", listener.getOk());
    }

    /**
     * Verify that a replied message on a queue arrives unchanged to a listener.
     */
    @Test
    public void testListenAndReply() {
        TestMessage testMsg = new TestMessage(sendQ, replyQ);
        conn.send(testMsg);
        TestMessageListener listener = new TestMessageListener(testMsg);
        conn.setListener(replyQ, listener);
        synchronized (this) {
            conn.reply(testMsg);
            try {
                wait(WAIT_MS);
            } catch (InterruptedException e) {}
        }
        assertTrue("Expected test message " + testMsg.toString() + "to have arrived on queue " + replyQ + " within "
                + WAIT_MS + " milliseconds", listener.getOk());
    }

    /**
     * Test that we can subscribe more than one listener to a topic and that they all receive the message.
     */
    @Test
    public void testNListenersToTopic() {
        TestMessage testMsg = new TestMessage(sendTopic, replyQ);
        List<MessageListener> listeners = new ArrayList<MessageListener>(NO_OF_LISTENERS);

        for (int i = 0; i < NO_OF_LISTENERS; i++) {
            TestMessageListener aListener = new TestMessageListener(testMsg);
            listeners.add(aListener);
            conn.setListener(sendTopic, aListener);
        }

        synchronized (this) {
            conn.send(testMsg);
            for (int i = 0; i < NO_OF_LISTENERS; i++) {
                try {
                    wait(WAIT_MS);
                } catch (InterruptedException e) {}
                boolean all_ok = true;
                for (int j = 0; j < NO_OF_LISTENERS; j++) {
                    if (((TestMessageListener) listeners.get(j)).getOk()) {
                        all_ok = false;
                    }
                }
                if (all_ok) {
                    break;
                }
            }
        }

        List<MessageListener> oks = new ArrayList<MessageListener>(NO_OF_LISTENERS);
        for (int i = 0; i < NO_OF_LISTENERS; i++) {
            if (((TestMessageListener) listeners.get(i)).getOk()) {
                oks.add(listeners.get(i));
            }
        }

        assertEquals("Expected test message " + testMsg.toString() + "to be received by exactly " + NO_OF_LISTENERS
                + " listeners within " + WAIT_MS + " milliseconds, but got " + oks, NO_OF_LISTENERS, oks.size());
    }

    /**
     * Tests that no messages are generated twice.
     */
    @Test
    public void testMsgIds() throws Exception {
        conn.setListener(Channels.getAnyBa(), new TestMessageListener(new TestMessage(Channels.getAnyBa(), sendQ)));
        Set<String> set = new HashSet<String>();

        for (int i = 0; i < 100; i++) {
            NetarkivetMessage msg = new TestMessage(Channels.getAnyBa(), sendQ);
            conn.send(msg);
            assertTrue("No msg ID must be there twice", set.add(msg.getID()));
        }
        conn = JMSConnectionFactory.getInstance();
        for (int i = 0; i < 100; i++) {
            NetarkivetMessage msg = new TestMessage(Channels.getAnyBa(), sendQ);
            conn.send(msg);
            assertTrue("No msg ID must be there twice", set.add(msg.getID()));
        }
        // To test messages are unique between processes, run the unittest by
        // two
        // JVMs simultanously (increase the number of messages generated or
        // insert delay to have time for starting two processes).
        // Then compare the logs:
        //
        // $ grep Generated netarkivtesta.log | cut -f 3- > a
        // $ grep Generated netarkivtestb.log | cut -f 3- > b
        // $ cat a b | sort | uniq -d
        //
        // This should produce no output unless two message IDs are equal.
    }

    /**
     * Tries to generate the mysterious NullPointerException of bug 220.
     */
    @Test
    public void testProvokeNullPointer() throws Exception {
        LogbackRecorder logbackRecorder = LogbackRecorder.startRecorder();
        Settings.set(CommonSettings.REMOTE_FILE_CLASS, FTPRemoteFile.class.getName());
        File testFile1 = new File("tests/dk/netarkivet/common/distribute/data/originals/arc_record0.txt");
        int tries = 100;
        for (int i = 0; i < tries; i++) {
            RemoteFile rf = RemoteFileFactory.getInstance(testFile1, true, false, true);
            rf.cleanup();
        }
        logbackRecorder.assertLogNotContains("A NullPointerException was thrown!", "NullPointerException");
    }

    /**
     * Tries to send a message to a Queue. - Makes a TestMessageConsumer, that listens to the ArcRepository Queue. -
     * Sends a message to that queue. - Verifies, that this message is sent and received un-modified.
     *
     * @throws Exception On failures
     */
    @Test
    public void testQueueSendMessage() throws Exception {
        TestMessageConsumer mc = new TestMessageConsumer();
        conn.setListener(Channels.getTheRepos(), mc);

        NetarkivetMessage nMsg = new TestMessage(Channels.getTheRepos(), Channels.getError(), "testQueueSendMessage");
        synchronized (mc) {
            conn.send(nMsg);
            mc.wait();
        }
        assertEquals("Arcrepos queue MessageConsumer should have received message.", nMsg.toString(),
                mc.nMsg.toString());
    }

    /**
     * Sets up 3 message consumers, all listening on the same channel. Then sends a message on that channel. Verify,
     * that the message is received by all three consumers.
     */
    @Test
    public void testTopicSendMessage() throws Exception {
        TestMessageConsumer mc1 = new TestMessageConsumer();
        TestMessageConsumer mc2 = new TestMessageConsumer();
        TestMessageConsumer mc3 = new TestMessageConsumer();
        conn.setListener(Channels.getAllBa(), mc1);
        conn.setListener(Channels.getAllBa(), mc2);
        conn.setListener(Channels.getAllBa(), mc3);

        NetarkivetMessage nMsg = new TestMessage(Channels.getAllBa(), Channels.getError(), "testTopicSendMessage");
        conn.send(nMsg);
        synchronized (mc1) {
            if (mc1.nMsg == null) {
                mc1.wait();
            }
        }
        synchronized (mc2) {
            if (mc2.nMsg == null) {
                mc2.wait();
            }
        }
        synchronized (mc3) {
            if (mc3.nMsg == null) {
                mc3.wait();
            }
        }

        assertEquals("Arcrepos queue MessageConsumer should have received message.", nMsg.toString(),
                mc1.nMsg.toString());
        assertEquals("Arcrepos queue MessageConsumer should have received message.", nMsg.toString(),
                mc2.nMsg.toString());
        assertEquals("Arcrepos queue MessageConsumer should have received message.", nMsg.toString(),
                mc3.nMsg.toString());
    }

    /**
     * Checks that the QueueBrowser created by the <code>JMSConnectionMQ</code> class work correctly.
     *
     * @throws JMSException
     * @throws InterruptedException
     * @see JMSConnection#createQueueBrowser(ChannelID)
     */
    @Test
    public void testQueueBrowsing() throws JMSException, InterruptedException {
        QueueBrowser queueBrowser = conn.createQueueBrowser(Channels.getTheRepos());
        TestMessageConsumer mc = new TestMessageConsumer();
        conn.setListener(Channels.getTheRepos(), mc);

        assertTrue("Empty queue had size > 0", queueBrowser.getEnumeration().hasMoreElements() == false);

        NetarkivetMessage nMsg = new TestMessage(Channels.getTheRepos(), Channels.getError(), "testQueueSendMessage");

        synchronized (mc) {
            conn.send(nMsg);
            assertTrue("Queue didn't have any messages after dispatching job", queueBrowser.getEnumeration()
                    .hasMoreElements() == true);
            mc.wait();
        }

        assertEquals("Arcrepos queue MessageConsumer should have received message.", nMsg.toString(),
                mc.nMsg.toString());

        assertTrue("Queue not empty after consumation of message",
                queueBrowser.getEnumeration().hasMoreElements() == false);
    }

    private class TestMessageListener implements MessageListener {
        private NetarkivetMessage expected;
        private boolean ok;

        public TestMessageListener(TestMessage tm) {
            expected = tm;
            ok = false;
        }

        public synchronized boolean getOk() {
            return ok;
        }

        public void resetOkState() {
            ok = false;
        }

        public void onMessage(Message msg) {
            synchronized (IntegrityTestSuite.this) {
                NetarkivetMessage nMsg = JMSConnection.unpack(msg);
                ok = nMsg.equals(expected);
                IntegrityTestSuite.this.notifyAll();
            }
        }
    }

    /**
     * A simple subclass of NetarkivetMessage to be used for test purposes only. The only added functionality is that
     * toString() outputs a representation of the "entire visible state" of the message.
     */
    @SuppressWarnings({"unused", "serial"})
    private static class TestMessage extends NetarkivetMessage {
        String testID;

        public TestMessage(ChannelID sendQ, ChannelID recQ) {
            super(sendQ, recQ);
        }

        public TestMessage(ChannelID to, ChannelID replyTo, String testID) {
            super(to, replyTo);
            this.testID = testID;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((testID == null) ? 0 : testID.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TestMessage other = (TestMessage) obj;
            if (testID == null) {
                if (other.testID != null) {
                    return false;
                }
            } else if (!testID.equals(other.testID)) {
                return false;
            }
            return true;
        }

        public String toString() {
            return super.toString() + "(" + getTo().toString() + "," + getReplyTo().toString() + ")" + ":" + isOk()
                    + (isOk() ? "" : getErrMsg());
        }

        public String getTestID() {
            return testID;
        }
    }

    public static final class TestMessageConsumer implements MessageListener {

        public NetarkivetMessage nMsg;

        JMSConnection con;

        public TestMessageConsumer() {
            con = JMSConnectionFactory.getInstance();
        }

        public void onMessage(Message msg) {
            synchronized (this) {
                nMsg = JMSConnection.unpack(msg);
                this.notifyAll();
            }
        }
    }
}
