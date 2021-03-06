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
package dk.netarkivet.viewerproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.netarkivet.common.arcrepository.TestArcRepositoryClient;
import dk.netarkivet.common.distribute.JMSConnectionMockupMQ;
import dk.netarkivet.common.distribute.indexserver.Index;
import dk.netarkivet.common.distribute.indexserver.JobIndexCache;
import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.testutils.StringAsserts;

/**
 * Tests of DelegatingController class.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DelegatingControllerTester {
    private TestMissingURIRecorder mur;
    private TestCDXCache cc;
    private TestARCArchiveAccess aaa;

    @Before
    public void setUp() {
        JMSConnectionMockupMQ.useJMSConnectionMockupMQ();
        mur = new TestMissingURIRecorder();
        cc = new TestCDXCache();
        aaa = new TestARCArchiveAccess();
    }

    @After
    public void tearDown() {
        JMSConnectionMockupMQ.getInstance().cleanup();
    }

    /**
     * Tests constructor. Only thing really testable is that ArgumentNotValid is thrown on null arguments.
     */
    @Test
    public void testController() {
        try {
            new DelegatingController(null, cc, aaa);
            fail("Should have thrown ArgumentNotValid");
        } catch (ArgumentNotValid e) {
            // expected
        }
        try {
            new DelegatingController(mur, null, aaa);
            fail("Should have thrown ArgumentNotValid");
        } catch (ArgumentNotValid e) {
            // expected
        }
        try {
            new DelegatingController(mur, cc, null);
            fail("Should have thrown ArgumentNotValid");
        } catch (ArgumentNotValid e) {
            // expected
        }
        new DelegatingController(mur, cc, aaa);
        // Should not throw ANV
    }

    /**
     * Tests start. This is simply a delegating method, so just tests the mur's start is called, and nothing else.
     *
     * @throws Exception
     */
    @Test
    public void testStart() throws Exception {
        Controller c = new DelegatingController(mur, cc, aaa);
        c.startRecordingURIs();
        assertEquals("The mur's start method should be called once", 1, mur.startCounter);
        assertEquals("No more methods should be called", 1, mur.totalCounter);
        assertEquals("No other classes methods should be called", 0, cc.totalCounter);
        assertEquals("No other classes methods should be called", 0, aaa.totalCounter);
    }

    /**
     * Tests stop. This is simply a delegating method, so just tests the mur's stop is called, and nothing else.
     *
     * @throws Exception
     */
    @Test
    public void testStop() throws Exception {
        Controller c = new DelegatingController(mur, cc, aaa);
        c.stopRecordingURIs();
        assertEquals("The mur's stop method should be called once", 1, mur.stopCounter);
        assertEquals("No more methods should be called", 1, mur.totalCounter);
        assertEquals("No other classes methods should be called", 0, cc.totalCounter);
        assertEquals("No other classes methods should be called", 0, aaa.totalCounter);
    }

    /**
     * Tests clear. This is simply a delegating method, so just tests the mur's clear is called, and nothing else.
     *
     * @throws Exception
     */
    @Test
    public void testClear() throws Exception {
        Controller c = new DelegatingController(mur, cc, aaa);
        c.clearRecordedURIs();
        assertEquals("The mur's clear method should be called once", 1, mur.clearCounter);
        assertEquals("No more methods should be called", 1, mur.totalCounter);
        assertEquals("No other classes methods should be called", 0, cc.totalCounter);
        assertEquals("No other classes methods should be called", 0, aaa.totalCounter);
    }

    /**
     * Tests getRecordedURIs. This is simply a delegating method, so just tests the mur's getRecordedURI is called, and
     * nothing else, and also that what is returned is exactly what the mur returns.
     *
     * @throws Exception
     */
    @Test
    public void testGetRecordedURIs() throws Exception {
        Controller c = new DelegatingController(mur, cc, aaa);
        Set<URI> uris = c.getRecordedURIs();
        assertEquals("The mur's getRecordedURIs method should be called once", 1, mur.getRecordedURICounter);
        assertEquals("No more methods should be called", 1, mur.totalCounter);
        assertEquals("No other classes methods should be called", 0, cc.totalCounter);
        assertEquals("No other classes methods should be called", 0, aaa.totalCounter);
        assertEquals("Should have received 1 uris as result", 1, uris.size());
        assertEquals("The uri should be the right result", new URI("http://foo.bar"), uris.iterator().next());
    }

    /**
     * Tests changeIndex. This is really a mediator between cdxcache and arc archive access. Tests that argument not
     * valid is not checked on null argument (this should be checked by the wrapped class), and otherwise that what
     * happens is exactly that CDXCache.getJobIndex is called, and the result given to ARCArchiveAccess.setIndex.
     */
    @Test
    public void testChangeIndex() {
        Controller c = new DelegatingController(mur, cc, aaa);
        try {
            c.changeIndex(null, "label");
            assertEquals("The cc's getJobsIndex method should be called once", 1, cc.getJobIndexCount);
            assertEquals("The aaa's setIndex method should be called once", 1, aaa.setIndexCount);
            assertEquals("No mur methods should be called", 0, mur.totalCounter);
            assertEquals("Should have received null in CDX cache", null, cc.getJobIndexArgument);
            assertEquals("Should have received the cc return value in aaa", new File("/return/data"),
                    aaa.setIndexArgument);
        } catch (ArgumentNotValid e) {
            fail("Should NOT throw ArgumentNotValid on null argument." + "This is just a mediator.");
        }
        Set<Long> jobs = new HashSet<Long>();
        try {
            c.changeIndex(jobs, null);
            assertEquals("The cc's getJobsIndex method should be called once", 2, cc.getJobIndexCount);
            assertEquals("The aaa' setIndex method should be called once", 2, aaa.setIndexCount);
            assertEquals("No mur methods should be called", 0, mur.totalCounter);
            assertEquals("Should have received empty list in CDX cache", jobs, cc.getJobIndexArgument);
            assertEquals("Should have received the cc return value in aaa", new File("/return/data"),
                    aaa.setIndexArgument);
        } catch (ArgumentNotValid e) {
            fail("Should NOT throw ArgumentNotValid on null argument." + "This is just a mediator.");
        }
        try {
            c.changeIndex(jobs, "label");
            assertEquals("The cc's getJobsIndex method should be called once", 3, cc.getJobIndexCount);
            assertEquals("The aaa' setIndex method should be called once", 3, aaa.setIndexCount);
            assertEquals("No mur methods should be called", 0, mur.totalCounter);
            assertEquals("Should have received the empty list in CDX cache", jobs, cc.getJobIndexArgument);
            assertEquals("Should have received the cc return value in aaa", new File("/return/data"),
                    aaa.setIndexArgument);
        } catch (ArgumentNotValid e) {
            fail("Should NOT throw ArgumentNotValid on empty list argument." + "This is just a mediator.");
        }
        jobs.add(1234L);
        c.changeIndex(jobs, "label");
        assertEquals("The cc's getJobsIndex method should be called once", 4, cc.getJobIndexCount);
        assertEquals("The aaa' setIndex method should be called once", 4, aaa.setIndexCount);
        assertEquals("No mur methods should be called", 0, mur.totalCounter);
        assertEquals("Should have received the list in CDX cache", jobs, cc.getJobIndexArgument);
        assertEquals("Should have received the cc return value in aaa", new File("/return/data"), aaa.setIndexArgument);
        StringAsserts.assertStringContains("Should contain label and job " + "number in status", "label",
                c.getStatus(Locale.ENGLISH));
        StringAsserts.assertStringContains("Should contain label and job " + "number in status", "1234",
                c.getStatus(Locale.ENGLISH));
    }

    public static class TestMissingURIRecorder extends MissingURIRecorder {
        int totalCounter = 0;
        int startCounter = 0;
        int stopCounter = 0;
        int clearCounter = 0;
        int getRecordedURICounter = 0;

        public void startRecordingURIs() {
            totalCounter++;
            startCounter++;
        }

        public void stopRecordingURIs() {
            totalCounter++;
            stopCounter++;
        }

        public void clearRecordedURIs() {
            totalCounter++;
            clearCounter++;
        }

        public Set<URI> getRecordedURIs() {
            totalCounter++;
            getRecordedURICounter++;
            HashSet<URI> uris = new HashSet<URI>();
            try {
                uris.add(new URI("http://foo.bar"));
            } catch (URISyntaxException e) {
                throw new RuntimeException("Illegal URI not possible");
            }
            return uris;
        }
    }

    public static class TestCDXCache implements JobIndexCache {
        int totalCounter = 0;
        int getJobIndexCount = 0;
        Set<Long> getJobIndexArgument;

        public Index<Set<Long>> getIndex(Set<Long> jobIDs) {
            totalCounter++;
            getJobIndexCount++;
            getJobIndexArgument = jobIDs;
            return new Index(new File("/return/data"), jobIDs);
        }

        @Override
        public void requestIndex(Set<Long> jobSet, Long harvestId) {
            // To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class TestARCArchiveAccess extends ARCArchiveAccess {
        int totalCounter = 0;
        int setIndexCount = 0;
        File setIndexArgument = null;

        public TestARCArchiveAccess() {
            super(new TestARCRepositoryClient());
        }

        public void setIndex(File index) {
            totalCounter++;
            setIndexCount++;
            setIndexArgument = index;
        }
    }

    public static class TestARCRepositoryClient extends TestArcRepositoryClient {
        protected TestARCRepositoryClient() {
            super(TestInfo.WORKING_DIR);
            close();
        }
    }
}
