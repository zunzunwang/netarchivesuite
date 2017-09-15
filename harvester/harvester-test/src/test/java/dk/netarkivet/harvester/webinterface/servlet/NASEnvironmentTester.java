package dk.netarkivet.harvester.webinterface.servlet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.netarkivet.harvester.datamodel.DataModelTestCase;


public class NASEnvironmentTester extends DataModelTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }


    /**
     * Test that getCrawledUrls() gets the crawled URLs of the crawllog for the running job with the given job id
     */
    @Test
    public void testGetCrawledUrls() throws Exception {

        // Create a mock crawllog file
        String mockCrawllogContent
                = "2005-05-06T11:47:26.550Z     1         53 dns:www.kb.dk P http://www.kb.dk/ text/dns #002 20050506114726441+2 - -\n"
                + "2005-05-06T11:47:28.464Z   404        278 http://www.netarkivet.dk/robots.txt P http://www.netarkivet.dk/ text/html #028 20050506114728458+5 NYN2HPNQGIPJTPMGAV4QPBUCVJVNMM54 -\n"
                + "2005-05-06T11:47:34.753Z -9998          - https://rex.kb.dk/F L http://www.kb.dk/ no-type #030 - - 3t\n"
                + "2005-05-06T11:47:30.544Z   200      13750 http://www.kb.dk/ - - text/html #001 20050506114730466+32 U4X3Z5EGCNUYTMIXST6BJXGA5SBKTEAJ 3t\n";

        File tempFile = File.createTempFile("NASEnvironmentTest-mock-crawllog-", ".tmp");
        tempFile.deleteOnExit();
        String crawlLogFilePath = tempFile.getAbsolutePath();

        FileWriter fileWriter = new FileWriter(tempFile, false);
        BufferedWriter bw = new BufferedWriter(fileWriter);
        bw.write(mockCrawllogContent);
        bw.close();


        // Create mock NAS environment, use it to get the crawled URLs using method to be tested
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getRealPath(any())).thenReturn("");
        ServletConfig servletConfig = mock(ServletConfig.class);


        NASEnvironment environment = new NASEnvironment(servletContext, servletConfig);

        Heritrix3JobMonitor h3Job = new Heritrix3JobMonitor();
        h3Job.setCrawlLogFilePath(crawlLogFilePath);

        List<String> crawledUrls = environment.getCrawledUrls(1, h3Job);

        // Check whether output corresponds to the input crawllog-mock-file
        if (crawledUrls.size() != 2) {
            fail("Wrong amount (" + crawledUrls.size() + ") of URLs extracted from crawllog!");
            return;
        }
        if (!crawledUrls.get(0).equalsIgnoreCase("http://www.netarkivet.dk/robots.txt")
                || !crawledUrls.get(1).equalsIgnoreCase("http://www.kb.dk/")) {
            fail("URL(s) extracted from crawllog do not match!");
            return;
        }
    }

}