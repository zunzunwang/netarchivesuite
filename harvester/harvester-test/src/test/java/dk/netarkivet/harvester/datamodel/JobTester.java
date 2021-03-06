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
package dk.netarkivet.harvester.datamodel;


/**
 * Test class for the Job class.
 */
@SuppressWarnings({"unchecked"})
public class JobTester extends DataModelTestCase {

//    private HarvestChannel highChan;
//    private HarvestChannel lowChan;
//
//    @Before
//    public void setUp() throws Exception {
//        super.setUp();
//        Settings.set(CommonSettings.NOTIFICATIONS_CLASS, RememberNotifications.class.getName());
//        Settings.set(HarvesterSettings.SPLIT_BY_OBJECTLIMIT, "false");
//        highChan = new HarvestChannel("FOCUSED", false, true, "");
//        lowChan = new HarvestChannel("SNAPSHOT", true, true, "");
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        super.tearDown();
//    }
//
//    /**
//     * Tests that seedlists have ascii versions added.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testDanskTegn() throws Exception {
//        DomainDAO dao = DomainDAO.getInstance();
//        Domain poelse = Domain.getDefaultDomain("pølse.dk");
//        dao.create(poelse);
//        DomainConfiguration dc = poelse.getDefaultConfiguration();
//        Job job = Job.createJob(1234L, highChan, dc, 0);
//        String seedList = job.getSeedListAsString();
//        Pattern p = Pattern.compile("^http://www\\.xn--plse-gra\\.dk$", Pattern.MULTILINE);
//        Matcher m = p.matcher(seedList);
//        assertTrue("Should have added ascii-ized seed to seedlist " + seedList, m.find());
//        poelse = dao.read("pølse.dk");
//        final String extraUrls = "http://www.pølse.dk/enfil\n" + "http://www.pølse.dk/enpølse\n"
//                + "http://www.uden.dk/enpølse\n" + "http://www.pølse.dk:8080/port\n" + "http://www.pølse.dk:8090\n"
//                + "www.pølse.dk:8091\n" + "www.pølse.dk/andenfil";
//        final SeedList withUrlList = new SeedList("withurl", extraUrls);
//        poelse.addSeedList(withUrlList);
//        dc = poelse.getDefaultConfiguration();
//        dc.addSeedList(poelse, withUrlList);
//        dao.update(poelse);
//        job = Job.createJob(12342L, highChan, dc, 0);
//        seedList = job.getSeedListAsString();
//        for (String s : new String[] {"http://www.xn--plse-gra.dk/enfil", "http://www.xn--plse-gra.dk/enpølse",
//                "http://www.uden.dk/enpølse", "http://www.xn--plse-gra.dk:8080/port",
//                "http://www.xn--plse-gra.dk:8090", "www.xn--plse-gra.dk:8091", "www.xn--plse-gra.dk/andenfil"}) {
//            assertTrue("Should contain URL '" + s + "' in '" + seedList + "'", seedList.contains(s));
//        }
//        // LogUtils.flushLogs(Job.class.getName());
//        // FileAsserts.assertFileNotContains(
//        // "No warnings should be generated. The logfile is: "
//        // + FileUtils.readFile(TestInfo.LOG_FILE),
//        // TestInfo.LOG_FILE, "WARNING");
//    }
//
//    @Category(SlowTest.class)
//    @Test
//    public void testAddConfigurationMinCountObjects() {
//        // Note: The configurations have these expectations:
//        // 500, 1400, 2400, 4000
//        DomainConfiguration dc1 = TestInfo.createConfig("kb.dk", "fuld_dybde", 112);
//        DomainConfiguration dc2 = TestInfo.createConfig("netarkivet.dk", "fuld_dybde", 1112);
//        DomainConfiguration dc3 = TestInfo.createConfig("statsbiblioteket.dk", "fuld_dybde", 2223);
//        // System.out.println("generatorclass: " + Settings.get(HarvesterSettings.JOBGEN_CLASS));
//        // This line is needed for this test to pass if this test is run as part of the whole suite
//        // If the default JOBGEN_CLASS is changed, this will not work.
//        dk.netarkivet.harvester.scheduler.jobgen.DefaultJobGenerator.reset();
//        Settings.set(HarvesterSettings.JOBS_MAX_RELATIVE_SIZE_DIFFERENCE, "3");
//        Settings.set(HarvesterSettings.JOBS_MIN_ABSOLUTE_SIZE_DIFFERENCE, "0");
//        Job job = Job.createJob(42L, highChan, dc1, 0);
//        // should be okay, relative size difference below 3
//        job.addConfiguration(dc2);
//        // should be okay, relative size difference now above 3!
//        try {
//            job.addConfiguration(dc3);
//            fail("Should fail on too great relative size difference");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//    }
//
//    /**
//     * Verify that a Job can be created and the correct data retrieved.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testSetAndGet() throws IOException {
//        DomainConfiguration dc = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, dc, 0);
//
//        assertNotNull("A valid job definition expected", job);
//
//        // verify that data can be retrieved from the job again
//        assertEquals("Value from CTOR expected", TestInfo.HARVESTID, job.getOrigHarvestDefinitionID());
//        Set<String> seeds = new HashSet<String>();
//        for (Iterator<SeedList> i = dc.getSeedLists(); i.hasNext();) {
//            SeedList list = (SeedList) i.next();
//            seeds.addAll(list.getSeeds());
//        }
//
//        Set<String> seedsFromJob = new HashSet<String>();
//        BufferedReader reader = new BufferedReader(new StringReader(job.getSeedListAsString()));
//        String s;
//        while ((s = reader.readLine()) != null) {
//            seedsFromJob.add(s);
//        }
//        assertEquals("Value from CTOR expected", seeds, seedsFromJob);
//        assertEquals("Value from CTOR expected", dc.getOrderXmlName(), job.getOrderXMLName());
//
//        Date d1 = new Date();
//        job.setActualStart(d1);
//
//        Date d2 = new Date();
//        job.setActualStop(d2);
//
//        assertEquals("Date value set expected", d1, job.getActualStart());
//        assertEquals("Date value set expected", d2, job.getActualStop());
//    }
//
//    @Category(SlowTest.class)
//    @Test
//    public void testGetAndSetStatus() {
//        DomainConfiguration dc = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, dc, 0);
//        assertEquals("Status of a new Job - expected", JobStatus.NEW, job.getStatus());
//
//        job.setStatus(JobStatus.SUBMITTED);
//        assertEquals("Status value set - expected", JobStatus.SUBMITTED, job.getStatus());
//    }
//
//    /**
//     * Tests that it is only permitted to change job status as follows: new -> submitted -> started -> done -> failed.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testOrderSetStatus() {
//        DomainConfiguration dc = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, dc, 0);
//
//        // Test valid order of status changes:
//        for (int i = JobStatus.NEW.ordinal(); i <= JobStatus.FAILED.ordinal(); i++) {
//            job.setStatus(JobStatus.values()[i]);
//            assertEquals("Status of job set - expected", i, job.getStatus().ordinal());
//        }
//
//        // Set status to STATUS_FAILED, to make sure job has right status for the following test:
//        job.setStatus(JobStatus.FAILED);
//
//        // Test invalid order of status changes:
//        for (int i = JobStatus.DONE.ordinal(); i >= JobStatus.NEW.ordinal(); i--) {
//            try {
//                job.setStatus(JobStatus.values()[i]);
//                fail("Failed to throw ArgumentNotValid exception on trying to set status to " + i
//                        + " on a job with STATUS_FAILED");
//            } catch (ArgumentNotValid e) {
//                // expected
//            }
//        }
//    }
//
//    /**
//     * Tests that we cannot cause a job status to decrease in ordinal value from DONE to NEW
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testIllegalJumpInStatus() {
//        DomainConfiguration dc = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, dc, 0);
//        job.setStatus(JobStatus.DONE);
//        try {
//            job.setStatus(JobStatus.NEW);
//            fail("Changed job from DONE to NEW");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//    }
//
//    /**
//     * Tests the single special case where job status can be reduced in ordinal value from FAILED_REJECTED to FAILED.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testLegalJumpInStatus() {
//        DomainConfiguration dc = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, dc, 0);
//        job.setStatus(JobStatus.FAILED_REJECTED);
//        job.setStatus(JobStatus.FAILED);
//    }
//
//    /**
//     * Test if a configuration is checked with respect to expected number of objects, and that the domain the domain in
//     * this configuration is not already in job.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testCanAcceptByteLimit() {
//
//        Domain defaultDomain = TestInfo.getDefaultDomain();
//        DomainConfiguration dc = TestInfo.getDefaultConfig(defaultDomain);
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, dc, 0);
//        DefaultJobGenerator jobGen = new DefaultJobGenerator();
//        try {
//            jobGen.canAccept(job, null);
//            fail("Failed to throw ArgumentNotValid on null argument");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        assertFalse("Job should not accept configuration asscoiated with domain " + defaultDomain,
//                jobGen.canAccept(job, TestInfo.getConfigurationNotDefault(defaultDomain)));
//
//        // Test with a config associated with another domain:
//        DomainConfiguration anotherConfig = TestInfo.getConfigurationNotDefault(TestInfo.getDomainNotDefault());
//        assertTrue("Job should accept configuration associated with domain " + anotherConfig.getDomainName(),
//                jobGen.canAccept(job, anotherConfig));
//
//        // Test split according to byte limits
//
//        // Make a job with limit of 2000000 defined by harvest definition
//        dc.setMaxBytes(5000000);
//        job = Job.createSnapShotJob(TestInfo.HARVESTID, highChan, dc, -1L, 2000000,
//                Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//
//        anotherConfig.setMaxBytes(2000000);
//        assertTrue("Should accept config with same limit", jobGen.canAccept(job, anotherConfig));
//
//        anotherConfig.setMaxBytes(1000000);
//        assertFalse("Should NOT accept config with lower limit", jobGen.canAccept(job, anotherConfig));
//
//        anotherConfig.setMaxBytes(3000000);
//        assertTrue("Should accept config with higher limit", jobGen.canAccept(job, anotherConfig));
//
//        // Make a job with limit of 2000000 defined by harvest definition
//        dc.setMaxBytes(2000000);
//        job = Job.createSnapShotJob(TestInfo.HARVESTID, highChan, dc, -1L, 5000000,
//                Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//
//        anotherConfig.setMaxBytes(2000000);
//        assertTrue("Should accept config with same limit", jobGen.canAccept(job, anotherConfig));
//
//        anotherConfig.setMaxBytes(1000000);
//        assertFalse("Should NOT accept config with lower limit", jobGen.canAccept(job, anotherConfig));
//
//        anotherConfig.setMaxBytes(3000000);
//        assertFalse("Should NOT accept config with higher limit", jobGen.canAccept(job, anotherConfig));
//
//        // TODO: Should also be tested that expected size associated with this configuration
//        // is with limits (minCountObjects, maxCountObjects). This should be a separate
//        // test case and should have been done in Iteration 4.
//    }
//
//    @Category(SlowTest.class)
//    @Test
//    public void testAddConfiguration() {
//        DomainConfiguration defaultConfig = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, defaultConfig, 0);
//
//        try {
//            job.addConfiguration(null);
//            fail("Failed to throw ArgumentNotValid on null argument");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        try {
//            job.addConfiguration(defaultConfig);
//            fail("Failed to throw ArgumentNotValid on trying to add identical configuration "
//                    + "(associated with same domain");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        DomainConfiguration anotherConfig = TestInfo.getConfigurationNotDefault(TestInfo.getDomainNotDefault());
//        job.addConfiguration(anotherConfig);
//
//        Map<String, String> domainConfigMap = job.getDomainConfigurationMap();
//        assertTrue("Length of DomainConfigurationMap should be 2", domainConfigMap.size() == 2);
//
//        // Another test: The implicit call of testAddConfiguration in the
//        // constructor should accept ONE configurations, even if it exceeds the
//        // usual limits
//
//        Domain d = Domain.getDefaultDomain("kaarefc.dk");
//        HarvestInfo hi = new HarvestInfo(Long.valueOf(1L), d.getName(), d.getDefaultConfiguration().getName(),
//                new Date(), 10000L, 10000L, StopReason.DOWNLOAD_COMPLETE);
//        d.getHistory().addHarvestInfo(hi);
//        DomainDAO.getInstance().create(d);
//
//        job = Job.createSnapShotJob(TestInfo.HARVESTID, highChan, d.getDefaultConfiguration(), 1L, -1L,
//                Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//        assertEquals("First configuration should be accepted", 1, job.getCountDomains());
//
//    }
//
//    /**
//     * Tests that job status fields are defined and are distinct
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testStatusFields() {
//        assertTrue("Error implementing status codes", JobStatus.NEW != JobStatus.STARTED);
//        assertTrue("Error implementing status codes", JobStatus.NEW != JobStatus.SUBMITTED);
//        assertTrue("Error implementing status codes", JobStatus.NEW != JobStatus.DONE);
//        assertTrue("Error implementing status codes", JobStatus.NEW != JobStatus.FAILED);
//        JobStatus s = JobStatus.NEW;
//        assertEquals("Error implementing status code names for NEW", s, JobStatus.valueOf(s.name()));
//        assertEquals("Error implementing status ordinal for NEW", s, JobStatus.fromOrdinal(s.ordinal()));
//        s = JobStatus.SUBMITTED;
//        assertEquals("Error implementing status code names for " + s.name(), s, JobStatus.valueOf(s.name()));
//        assertEquals("Error implementing status ordinal for " + s.name(), s, JobStatus.fromOrdinal(s.ordinal()));
//        s = JobStatus.STARTED;
//        assertEquals("Error implementing status code names for " + s.name(), s, JobStatus.valueOf(s.name()));
//        assertEquals("Error implementing status ordinal for " + s.name(), s, JobStatus.fromOrdinal(s.ordinal()));
//        s = JobStatus.DONE;
//        assertEquals("Error implementing status code names for " + s.name(), s, JobStatus.valueOf(s.name()));
//        assertEquals("Error implementing status ordinal for " + s.name(), s, JobStatus.fromOrdinal(s.ordinal()));
//        s = JobStatus.FAILED;
//        assertEquals("Error implementing status code names for " + s.name(), s, JobStatus.valueOf(s.name()));
//        assertEquals("Error implementing status ordinal for " + s.name(), s, JobStatus.fromOrdinal(s.ordinal()));
//        s = JobStatus.RESUBMITTED;
//        assertEquals("Error implementing status code names for " + s.name(), s, JobStatus.valueOf(s.name()));
//        assertEquals("Error implementing status ordinal for " + s.name(), s, JobStatus.fromOrdinal(s.ordinal()));
//    }
//
//    /**
//     * Check handling of invalid arguments.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testInvalidArgs() {
//        // HarvestID
//        try {
//            Job.createJob(null, highChan, DomainDAO.getInstance().read("netarkivet.dk").getDefaultConfiguration(), 0);
//            fail("Argument invalid");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        try {
//            Job.createJob(Long.valueOf(-1L), highChan, DomainDAO.getInstance().read("netarkivet.dk")
//                    .getDefaultConfiguration(), 0);
//            fail("Argument invalid");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        // DomainConfiguration
//        try {
//            Job.createJob(TestInfo.HARVESTID, highChan, null, 0);
//            fail("Argument invalid");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        // startdate
//        try {
//            Job job = Job.createJob(Long.valueOf(0L), highChan, DomainDAO.getInstance().read("netarkivet.dk")
//                    .getDefaultConfiguration(), 0);
//            job.setActualStart(null);
//            fail("Argument invalid");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        // channel
//        try {
//            Job job = Job.createJob(Long.valueOf(0L), null, DomainDAO.getInstance().read("netarkivet.dk")
//                    .getDefaultConfiguration(), 0);
//            job.setActualStart(new Date());
//            fail("Argument invalid");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        // enddate
//        try {
//            Job job = Job.createJob(Long.valueOf(0L), highChan, DomainDAO.getInstance().read("netarkivet.dk")
//                    .getDefaultConfiguration(), 0);
//            job.setActualStop(null);
//            fail("Argument invalid");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        // startdate < enddate
//        RememberNotifications.resetSingleton();
//        try {
//            Job job = Job.createJob(Long.valueOf(0L), highChan, DomainDAO.getInstance().read("netarkivet.dk")
//                    .getDefaultConfiguration(), 0);
//
//            Date d1 = new Date(0);
//            Date d2 = new Date(1000);
//            job.setActualStart(d2);
//            assertTrue("No notifications should have been emitted", RememberNotifications.getInstance().message == null);
//            job.setActualStop(d1);
//            assertTrue("Argument invalid start later than stop should send an notication",
//                    RememberNotifications.getInstance().message.length() > 0);
//        } catch (ArgumentNotValid e) {
//            fail("Argument invalid start later than stop should not throw an exception:" + e);
//        }
//
//        try {
//            Job job = Job.createJob(Long.valueOf(0), highChan, DomainDAO.getInstance().read("netarkivet.dk")
//                    .getDefaultConfiguration(), 0);
//            Date d1 = new Date(0);
//            Date d2 = new Date(1000);
//            job.setActualStop(d1);
//            RememberNotifications.resetSingleton();
//            job.setActualStart(d2);
//            assertTrue("Argument invalid start later than stop should send an notification",
//                    RememberNotifications.getInstance().message.length() > 0);
//        } catch (ArgumentNotValid e) {
//            fail("Argument invalid start later than stop should not throw an exception:" + e);
//        }
//    }
//
//    /**
//     * Tests that normal jobs have high priority and snapshot jobs have low priority.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testPriority() {
//        Domain d = Domain.getDefaultDomain("testdomain.dk");
//        DomainDAO.getInstance().create(d);
//        Job job0 = Job.createJob(Long.valueOf(1L), highChan, d.getDefaultConfiguration(), 0);
//        assertEquals("Job should have channel " + "FOCUSED", "FOCUSED", job0.getChannel());
//        Job job1 = Job.createSnapShotJob(Long.valueOf(1), lowChan, d.getDefaultConfiguration(), 2000L, -1L,
//                Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//        assertEquals("Job should have channel " + lowChan.getName(), lowChan.getName(), job1.getChannel());
//    }
//
//    @Category(SlowTest.class)
//    @Test
//    public void testCreateSnapShotJob() {
//
//        DomainConfiguration defaultConfig = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//
//        try {
//            Job.createSnapShotJob(TestInfo.HARVESTID, lowChan, defaultConfig, -42L, -1L,
//                    Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//            fail("Should not accept a negative value for max objects per domain");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        // Try to set an object limit higher than the configuration
//        // Should fail (capped to domain config limit)
//
//        Job job1 = Job.createSnapShotJob(TestInfo.HARVESTID, lowChan, defaultConfig, TestInfo.MAX_OBJECTS_PER_DOMAIN,
//                -1L, Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//        assertEquals("forceMaxObjectsPerDomain higher than config limit", defaultConfig.getMaxObjects(),
//                job1.getForceMaxObjectsPerDomain());
//
//        // Try to set a limit lower than domain config limit, should work
//        long jobLimit = defaultConfig.getMaxObjects() - 1;
//        Job job2 = Job.createSnapShotJob(TestInfo.HARVESTID, lowChan, defaultConfig, jobLimit, -1L, 0L, 0);
//        assertEquals("Failed to set forceMaxObjectsPerDomain", jobLimit, job2.getForceMaxObjectsPerDomain());
//
//    }
//
//    /**
//     * Tests that it is possible to set the maximum number of objects to be retrieved per domain i.e. that the order.xml
//     * that is used as base for this Job is edited accordingly.
//     *
//     * @throws DocumentException Thrown if SAXReader() has problems parsing the order.xml file.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testForceMaxObjectsPerDomain() throws DocumentException {
//
//        DomainConfiguration defaultConfig = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//
//        try {
//            Job.createSnapShotJob(TestInfo.HARVESTID, lowChan, defaultConfig, -42L, -1L,
//                    Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//            fail("Should not accept a negative value for max objects per domain");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        try {
//            Job.createSnapShotJob(TestInfo.HARVESTID, lowChan, defaultConfig, -1L, -42L,
//                    Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//            fail("Should not accept a negative value for max bytes per domain");
//        } catch (ArgumentNotValid e) {
//            // expected
//        }
//
//        // test capping of forceMaxObjectsPerDomain:
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, defaultConfig, 0);
//        assertEquals("forceMaxObjectsPerDomain not capped to domain config", defaultConfig.getMaxObjects(),
//                job.getForceMaxObjectsPerDomain());
//
//        job = Job.createSnapShotJob(TestInfo.HARVESTID, lowChan, defaultConfig, TestInfo.MAX_OBJECTS_PER_DOMAIN, -1L,
//                Constants.DEFAULT_MAX_JOB_RUNNING_TIME, 0);
//
//        // test getForceMaxObjectsPerDomain():
//        assertEquals("Value set in setForceMaxObjectsPerDomain not capped", defaultConfig.getMaxObjects(),
//                job.getForceMaxObjectsPerDomain());
//
//        // check if updated in the Document object that the Job object holds:
//        // xpath-expression that selects the appropriate node in order.xml:
//        // FIXME Only appropiate for Heritrix 1.14.4 template
//        /* final String xpath = "/crawl-order/controller/map[@name='pre-fetch-processors']"
//                + "/newObject[@name='QuotaEnforcer']" + "/long[@name='group-max-fetch-successes']";
//
//        Document orderXML = job.getOrderXMLdoc();
//        Node groupMaxFetchSuccessNode = orderXML.selectSingleNode(xpath);
//
//        long maxObjectsXML = Long.parseLong(groupMaxFetchSuccessNode.getText());
//        assertEquals("The order.xml Document should have been updated", defaultConfig.getMaxObjects(), maxObjectsXML);
//        */
//
//    }
//
//    @Category(SlowTest.class)
//    @Test
//    public void testSerializability() throws IOException, ClassNotFoundException {
//        // make a job:
//        Job job = Job.createJob(Long.valueOf(42), highChan, TestInfo.getDefaultConfig(TestInfo.getDefaultDomain()), 0);
//
//        // Write and read
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream ous = new ObjectOutputStream(baos);
//        ous.writeObject(job);
//        ous.close();
//        baos.close();
//
//        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
//
//        Job job2 = (Job) ois.readObject();
//
//        // Finally, compare their visible states:
//
//        // This requires, that class Job needs to override methods hashCode, and equals.
//        // assertTrue("Job2 should equal job1", job.equals(job2));
//
//        assertEquals(
//                "After serialization the states differed:\n"
//                        + TestFileUtils.getDifferences(relevantState(job), relevantState(job2)), relevantState(job),
//                relevantState(job2));
//
//        assertTrue(job.getSortedSeedList().equals(job2.getSortedSeedList()));
//
//        // Call a method that logs - this will fail if the transient logger
//        // has not been reinitialised
//        job2.setSeedList("www.netarkivet.dk");
//    }
//
//    /**
//     * Tests that crawlertraps are correctly merged with the order.xml. Crawlertraps are expressed using
//     * MatchesListRegExpDecideRule each listing the crawlertraps specified for a single domain, e.g. <newObject
//     * name="dr.dk" class="org.archive.crawler.deciderules.MatchesListRegExpDecideRule"> <string
//     * name="decision">REJECT</string> <string name="list-logic">OR</string> <stringList name='regexp-list'>
//     * <string>xyz.*</string> <string>.*[a-z]+</string> </stringList> </newObject>
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testAddConfigurationUpdatesOrderXml() {
//        // Make a configuration with no crawlertraps
//        Domain domain = TestInfo.getDefaultDomain();
//        DomainConfiguration dc1 = domain.getDefaultConfiguration();
//        boolean strictMode = true;
//        domain.setCrawlerTraps(Collections.<String>emptyList(), strictMode);
//        String domain1name = dc1.getDomainName();
//
//        // Make a configuration with two crawlertraps
//        Domain domain2 = TestInfo.getDomainNotDefault();
//        DomainConfiguration dc2 = domain2.getDefaultConfiguration();
//        List<String> traps = new ArrayList<String>();
//
//        String crawlerTrap1 = "xyz.*";
//        String crawlerTrap2 = ".*[a-z]+";
//        traps.add(crawlerTrap1);
//        traps.add(crawlerTrap2);
//        domain2.setCrawlerTraps(traps, strictMode);
//        dc2.setCrawlertraps(traps);
//        String domain2name = dc2.getDomainName();
//
//        // Make a job with the two configurations
//        Job j = Job.createJob(Long.valueOf(42L), highChan, dc1, 0);
//        j.addConfiguration(dc2);
//
//        // Check that there are no crawlertraps for the first domain and exactly
//        // the right two in the second domain
//
//        String domain1CrawlerTrapsXpath = "/crawl-order/controller/newObject[@name='scope']/newObject[@class='"
//                + DecideRuleSequence.class.getName() + "']/map[@name='rules']/" + "/newObject[@name='" + domain1name
//                + "'][@class='" + MatchesListRegExpDecideRule.class.getName() + "']";
//
//        String domain2CrawlerTrapsXpath = "/crawl-order/controller/newObject[@name='scope']/newObject[@class='"
//                + DecideRuleSequence.class.getName() + "']/map[@name='rules']/" + "/newObject[@name='" + domain2name
//                + "'][@class='" + MatchesListRegExpDecideRule.class.getName() + "']";
//
//        //FIXME the current test only appropriate for H1 templates
//        /*
//        j.getOrderXMLdoc().normalize();
//        List<Node> nodes = j.getOrderXMLdoc().selectNodes(domain1CrawlerTrapsXpath);
//        assertFalse("There shouldn't be any crawler traps for domain '" + domain1name + "'", nodes.size() != 0);
//        nodes = j.getOrderXMLdoc().selectNodes(domain2CrawlerTrapsXpath);
//        assertTrue("There should be crawler traps for domain '" + domain2name + "'", nodes.size() == 1);
//        Node regexpListDeciderule = nodes.get(0);
//        // TODO add checks for
//        // <string name="decision">REJECT</string>
//        // <string name="list-logic">OR</string>
//
//        nodes = regexpListDeciderule.selectNodes("stringList[@name='regexp-list']/string");
//
//        Set<String> regexpFound = new HashSet<String>();
//
//        for (Node n : nodes) {
//            String regexp = n.getText();
//            regexpFound.add(regexp);
//        }
//
//        int found = regexpFound.size();
//        assertTrue("Must only contain two regexp, but found " + found + "(" + regexpFound.toArray().toString() + ")",
//                found == 2);
//        assertTrue("Must contain regexp '" + crawlerTrap1 + "'.", regexpFound.contains(crawlerTrap1));
//        assertTrue("Must contain regexp '" + crawlerTrap2 + "'.", regexpFound.contains(crawlerTrap2));
//        */
//
//    }
//
//    private String relevantState(Job job) {
//        return "Job:" + "\nJob ID: " + job.getJobID() + "\nHarvest ID: " + job.getOrigHarvestDefinitionID()
//                + "\nOrder XML name: " + job.getOrderXMLName() + "\nOrder XML contents: "
//             // FIXME  + job.getOrderXMLdoc().asXML() + "\nSetting XML files" + job.getSettingsXMLfiles()
//                + "\nSetting XML docs" + job.getSettingsXMLdocs() + "\nStatus: " + job.getStatus().toString()
//                + "\nEdition" + job.getEdition() + "\nDomain->Configuration map" + job.getDomainConfigurationMap()
//                + "\nExpected maxObjects: " + job.getMaxObjectsPerDomain()
//                + "\nForced maxObjects: "
//                + job.getForceMaxObjectsPerDomain()
//                + "\nExpected maxBytes: "
//                + job.getMaxBytesPerDomain()
//                // Checked outside this method, because order of seeds is random
//                // + "\nSeedlist: " + job.getSeedListAsString()
//                + "\nSubmitted tid: " + job.getSubmittedDate() + "\nCreation tid: " + job.getCreationDate()
//                + "\nActual Start: " + job.getActualStart() + "\nActual Stop: " + job.getActualStop() + "\nChannel: "
//                + job.getChannel();
//    }
//
//    /**
//     * Test fields are set correctly, especially harvestNum (after bug 544) and max-bytes since that now uses limit from
//     * configuration if present.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testCreateJob() {
//        DomainConfiguration dc = TestInfo.getNetarkivetConfiguration();
//        dc.setMaxBytes(-1);
//        final int harvestNum = 4;
//        Job j = new Job(42L, dc, highChan, -1L, -1L, Constants.DEFAULT_MAX_JOB_RUNNING_TIME, harvestNum);
//        assertEquals("Job should have harvest num set", harvestNum, j.getHarvestNum());
//        assertEquals("Job should have harvest id set", Long.valueOf(42L), j.getOrigHarvestDefinitionID());
//        assertEquals("Job should have right prio", "FOCUSED", j.getChannel());
//        assertEquals("Job should have configuration object limit", dc.getMaxObjects(), j.getMaxObjectsPerDomain());
//        assertEquals("Job should have no byte limit (neither config nor hd sets it)", -1, j.getMaxBytesPerDomain());
//        assertEquals("Job should currently have one config", 1, j.getCountDomains());
//        assertEquals("Should be the right one", dc.getName(), j.getDomainConfigurationMap().values().iterator().next());
//
//        dc.setMaxBytes(1000000);
//        j = new Job(42L, dc, highChan, -1L, -1L, Constants.DEFAULT_MAX_JOB_RUNNING_TIME, harvestNum);
//        assertEquals("Job should have byte limit (config sets it)", 1000000, j.getMaxBytesPerDomain());
//
//        j = new Job(42L, dc, highChan, -1L, 500000L, Constants.DEFAULT_MAX_JOB_RUNNING_TIME, harvestNum);
//        assertEquals("Job should have byte limit (hd sets it)", 500000, j.getMaxBytesPerDomain());
//
//        dc.setMaxBytes(500000);
//        j = new Job(42L, dc, highChan, -1L, 1000000L, Constants.DEFAULT_MAX_JOB_RUNNING_TIME, harvestNum);
//        assertEquals("Job should have byte limit (hd sets it)", 500000, j.getMaxBytesPerDomain());
//
//    }
//
//    @Category(SlowTest.class)
//    @Test
//    public void testMaxBytes() throws Exception {
//        Method m = Job.class.getDeclaredMethod("setMaxBytesPerDomain", Long.TYPE);
//        m.setAccessible(true);
//        DomainDAO ddao = DomainDAO.getInstance();
//        Domain d = ddao.read(TestInfo.EXISTINGDOMAINNAME);
//        DomainConfiguration cfg = d.getDefaultConfiguration();
//        Job j = Job.createJob(42L, highChan, cfg, 2);
//        assertEquals("Should have no max bytes limit after creation", 500000000, j.getMaxBytesPerDomain());
//        final long maxBytes = 222 * 1024 * 1024;
//        m.invoke(j, maxBytes);
//        assertEquals("Should have set number of bytes after setting", maxBytes, j.getMaxBytesPerDomain());
//
//        JobDAO jdao = JobDAO.getInstance();
//        jdao.create(j);
//        Job j2 = jdao.read(j.getJobID());
//        assertEquals("Should have set number of bytes after reading", maxBytes, j2.getMaxBytesPerDomain());
//        final long maxBytes2 = 333 * 1024 * 1024;
//        try {
//            m.invoke(j2, maxBytes2);
//        } catch (InvocationTargetException e) {
//            if (!(e.getCause() instanceof IllegalState)) {
//                fail("Should only throw IllegalState");
//            }
//            // Expected
//        }
//        Job j4 = jdao.read(jdao.getAllJobIds().next());
//        assertEquals("Old job should have no max bytes setting", -1L, j4.getMaxBytesPerDomain());
//        // Also check snapshot jobs.
//
//    }
//
//    @Category(SlowTest.class)
//    @Test
//    public void testEditOrderXML_maxBytesPerDomain() throws Exception {
//        // Check that order.xml for the job is updated after calling setMaxBytesPerDomain()
//        // analogous with what is done in testForceMaxObjectsPerDomain()
//        // Should be able to find the value maxBytes2 (333 * 1024 * 1024)
//        // in the group-max-success-kb node.
//        // xpath-expression that selects the appropriate node in order.xml:
//        DomainDAO ddao = DomainDAO.getInstance();
//        Domain d = ddao.read(TestInfo.EXISTINGDOMAINNAME);
//        DomainConfiguration cfg = d.getDefaultConfiguration();
//        Job j = Job.createJob(42L, highChan, cfg, 2);
//
//        
//        //FIXME test only appropiate for Heritrix 1 template
//        /*
//        Document orderXML = j.getOrderXMLdoc();
//        final String xpath = "/crawl-order/controller/map[@name='pre-fetch-processors']"
//                + "/newObject[@name='QuotaEnforcer']" + "/long[@name='group-max-success-kb']";
//        Node groupMaxSuccessKbNode = orderXML.selectSingleNode(xpath);
//
//        long maxBytesXML = Long.parseLong(groupMaxSuccessKbNode.getText());
//        assertEquals("The group-max-success-kb field should have been updated in the order.xml Document", -1L,
//                maxBytesXML);
//                
//        */        
//    }
//
//    @Category(SlowTest.class)
//    @Test
//    public void testMaxBytesBug652() throws Exception {
//        DomainConfiguration defaultConfig = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//        defaultConfig.setMaxBytes(-1);
//
//        addHarvestDefinitionToDatabaseWithId(TestInfo.HARVESTID);
//        Job j = Job.createSnapShotJob(TestInfo.HARVESTID, lowChan, defaultConfig, 42L, // maxObjectsPerDomain
//                -1L, // maxBytesPerDomain
//                Constants.DEFAULT_MAX_JOB_RUNNING_TIME, // maxJobRunningTime
//                0 // harvestNum
//                );
//        // test default value of forceMaxObjectsPerDomain:
//        assertEquals("No limit of value of forceMaxObjectsPerDomain expected", -1, j.getMaxBytesPerDomain());
//        JobDAO jDao = JobDAO.getInstance();
//        jDao.create(j); // save job in Database.
//        Iterator<Job> jobIterator = jDao.getAll();
//        while (jobIterator.hasNext()) {
//            Job j1 = jobIterator.next();
//            if (j1.getMaxBytesPerDomain() == 1) {
//                fail("Maxbytes (-1) stored as (1)");
//            }
//        }
//    }
//
//    /**
//     * test the method public static List<AliasInfo> Job.getJobAliasInfo(Job job);
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testgetJobAliasInfo() {
//        DomainConfiguration dc = TestInfo.getDefaultConfig(TestInfo.getDefaultDomain());
//        Job job = Job.createJob(TestInfo.HARVESTID, highChan, dc, 0);
//        DomainConfiguration anotherConfig = TestInfo.getConfigurationNotDefault(TestInfo.getDomainNotDefault());
//        job.addConfiguration(anotherConfig);
//        // domains in job: job.getDomainConfigurationMap().keySet();
//        DomainDAO ddao = DomainDAO.getInstance();
//        List<AliasInfo> aliases = job.getJobAliasInfo();
//        // aliases equals #domains being skipped because a domain in job.getDomainConfigurationMap().keySet()
//        // is the aliasFather for that domain
//        assertTrue("No domains are skipped, as no aliases are defined", aliases.isEmpty());
//        DomainDefinition.createDomains("alias1.dk", "alias2.dk", "alias3.dk");
//        Domain d = ddao.read("kb.dk");
//        DomainConfiguration dc1 = TestInfo.getConfig(d, "aliasKonfig");
//        d = ddao.read("dr.dk");
//        DomainConfiguration dc2 = TestInfo.getConfig(d, "aliasKonfig2");
//        d = ddao.read("alias1.dk");
//        d.updateAlias("kb.dk");
//        ddao.update(d);
//        d = ddao.read("alias2.dk");
//        d.updateAlias("kb.dk");
//        ddao.update(d);
//        d = ddao.read("alias3.dk");
//        d.updateAlias("dr.dk");
//        ddao.update(d);
//        job = Job.createJob(TestInfo.HARVESTID, highChan, dc, 0);
//        job.addConfiguration(dc1);
//        job.addConfiguration(dc2);
//        // this should give us a List of size 3:
//        aliases = job.getJobAliasInfo();
//        assertEquals("There should be 3 AliasInfo objects in the List returned", 3, aliases.size());
//    }
//
//    /**
//     * Test method getSortedSeedList.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testGetSortedSeedList() throws Exception {
//        DomainConfiguration dc = TestInfo.getNetarkivetConfiguration();
//        dc.setMaxBytes(-1);
//        final int harvestNum = 4;
//        Job j = new Job(42L, dc, highChan, -1L, -1L, Constants.DEFAULT_MAX_JOB_RUNNING_TIME, harvestNum);
//        String seeds = "http://www.politik.tv2.dk/\n" + "http://dr.dk/valg\n"
//                + "http://www.bt.dk/section/DITVALG/1943\n" + "http://www.kristeligt-dagblad.dk/valg2007\n"
//                + "http://jp.dk/indland/indland_politik/\n" + "http://politiken.dk/politik/\n"
//                + "http://ekstrabladet.dk/nyheder/politik/\n" + "www.fyens.dk/fv2007\n"
//                + "http://information.dk/emne/valg07\n" + "http://jp.dk/webtv/valg07/\n"
//                + "http://borsen.dk/politik/\n" + "http://www.berlingske.dk/section/valg/\n"
//                + "http://www.fyens.dk/fv2007\n" + "http://information.dk/valgaften\n"
//                + "http://www.fyens.dk/indland\n" + "http://www.kvinfo.dk/side/557/article/769/\n"
//                + "http://www.netpressen.dk/index.php?option=com_simpleboard&Itemid=41&func=showcat&catid=31\n"
//                + "http://nordjyske.dk/index.aspx?page=3&action=sektionid%3D157&sender=&target=246&data=\n"
//                + "http://www.fyens.dk/fv2007\n";
//        j.setSeedList(seeds);
//        List<String> list = j.getSortedSeedList();
//        assertTrue(list.size() == 18); // verifies that duplicates (Here, the last seed) are removed.
//
//        // Find locations of
//        // http://www.fyens.dk/fv2007 (1)
//        // www.fyens.dk/fv2007 (2)
//        // http://www.fyens.dk/indland (3)
//
//        // verify that they are placed at consecutive locations:
//        Set<Integer> order = new TreeSet<Integer>();
//        order.add(Integer.valueOf(list.indexOf("http://www.fyens.dk/fv2007")));
//        order.add(Integer.valueOf(list.indexOf("www.fyens.dk/fv2007")));
//        order.add(Integer.valueOf(list.indexOf("http://www.fyens.dk/indland")));
//        int last = -1;
//        for (Integer i : order) {
//            if (last != -1) {
//                assertTrue("The urls must be in consecutive order", i == (last + 1));
//            }
//            last = i;
//        }
//
//        // Verify that getSortedList doesn't throw ArgumentNotValid
//        // (Cf. NAS-2062)
//        String alternateSeeds = "#google.netarkivet.dk\n" + "home.netarkivet.dk\n" + "#home.netarkivet.dk\n"
//                + "www.netarkivet.dk";
//        j.setSeedList(alternateSeeds);
//        List<String> sortedSeeds = j.getSortedSeedList();
//        assertTrue("The two uncommented seeds should have ignored", sortedSeeds.size() == 2);
//    }
//
//    /**
//     * Test method getSubmittedDate().
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testGetSubmittedDate() {
//        Job job = Job.createJob(Long.valueOf(42), highChan, TestInfo.getDefaultConfig(TestInfo.getDefaultDomain()), 0);
//        assertNull("Should be null, before being set explicitly", job.getSubmittedDate());
//        Date now = new Date();
//        job.setSubmittedDate(now);
//        assertEquals("Should be set correctly, after being set explicitly", now, job.getSubmittedDate());
//
//        job = Job.createJob(Long.valueOf(43), highChan, TestInfo.getDefaultConfig(TestInfo.getDefaultDomain()), 0);
//
//        JobDAO.getInstance().create(job);
//        Long jobID = job.getJobID();
//        job.setSubmittedDate(now);
//        JobDAO.getInstance().update(job);
//        job = JobDAO.getInstance().read(jobID);
//        assertEquals(now, job.getSubmittedDate());
//    }
//
//    /**
//     * Test method getCreationDate().
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testGetCreationDate() {
//        Job job = Job.createJob(Long.valueOf(42), highChan, TestInfo.getDefaultConfig(TestInfo.getDefaultDomain()), 0);
//        assertNull("Should be null, before being set explicitly", job.getCreationDate());
//        Date now = new Date();
//        job.setCreationDate(now);
//        assertEquals("Should be set correctly, after being set explicitly", now, job.getCreationDate());
//
//        job = Job.createJob(Long.valueOf(43), highChan, TestInfo.getDefaultConfig(TestInfo.getDefaultDomain()), 0);
//
//        JobDAO.getInstance().create(job);
//        Long jobID = job.getJobID();
//        job.setCreationDate(now);
//        JobDAO.getInstance().update(job);
//        job = JobDAO.getInstance().read(jobID);
//        assertEquals(now, job.getCreationDate());
//    }
//
//    /**
//     * Tests that global crawler traps defined in the dao are added to new jobs.
//     *
//     * @throws FileNotFoundException
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testCreateJobWithGlobalCrawlerTraps() throws FileNotFoundException {
//        GlobalCrawlerTrapList list1 = new GlobalCrawlerTrapList(new FileInputStream(new File(TestInfo.TOPDATADIR,
//                TestInfo.CRAWLER_TRAPS_01)), "list1", "A Description of list1", true);
//        GlobalCrawlerTrapList list2 = new GlobalCrawlerTrapList(new FileInputStream(new File(TestInfo.TOPDATADIR,
//                TestInfo.CRAWLER_TRAPS_02)), "list2", "A Description of list2", true);
//        GlobalCrawlerTrapListDAO trapDao = GlobalCrawlerTrapListDAO.getInstance();
//        trapDao.create(list1);
//        trapDao.create(list2);
//        Job job = Job.createJob(Long.valueOf(42), highChan, TestInfo.getDefaultConfig(TestInfo.getDefaultDomain()), 0);
//        // FIXME this test currently only appropriate for H1 templates
//        /*
//        Document doc = job.getOrderXMLdoc();
//        String TRAPS_XPATH = "/crawl-order/controller/newObject" + "/newObject[@name='decide-rules']"
//                + "/map[@name='rules']/newObject[@name='" + Constants.GLOBAL_CRAWLER_TRAPS_ELEMENT_NAME + "']";
//        Node trapsNode = doc.selectSingleNode(TRAPS_XPATH);
//        assertNotNull("Should have added a node", trapsNode);
//
//        Element stringList = (Element) ((Element) trapsNode).elements("stringList").get(0);
//        assertTrue("Should be several crawler traps present", stringList.elements("string").size() > 2);
//        */
//    }
//
//    /**
//     * test that Job.setsetSeedList does not accept null and/or empty seedlist.
//     */
//    @Category(SlowTest.class)
//    @Test
//    public void testSetSeedlist() {
//        Job job = Job.createJob(Long.valueOf(42), highChan, TestInfo.getDefaultConfig(TestInfo.getDefaultDomain()), 0);
//        String nullSeedlist = null;
//        String emptySeedlist = "";
//        String validSeedlist = "http://www.netarkivet.dk\nhttp://www.kb.dk";
//        try {
//            job.setSeedList(nullSeedlist);
//            fail("Operation setSeedList should fail w/ null argument, but didn't");
//        } catch (ArgumentNotValid e) {
//            // Expected
//        }
//
//        try {
//            job.setSeedList(emptySeedlist);
//            fail("Operation setSeedList should fail w/ empty argument, but didn't");
//        } catch (ArgumentNotValid e) {
//            // Expected
//        }
//
//        try {
//            job.setSeedList(validSeedlist);
//        } catch (ArgumentNotValid e) {
//            fail("Operation setSeedList shouldn't fail w/ valid argument '" + validSeedlist
//                    + "' but did with exception: " + e);
//        }
//
//    }
}
