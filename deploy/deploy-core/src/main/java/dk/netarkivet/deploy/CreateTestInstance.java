/*
 * #%L
 * Netarchivesuite - deploy
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
package dk.netarkivet.deploy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.dom4j.Element;

import dk.netarkivet.common.exceptions.ArgumentNotValid;

/**
 * This class applies the test variables.
 * <p>
 * It creates a new instance of the settings where the variables are changed, and then writes it out as a new
 * deploy-configuration file (does not overwrite the original, but creates a new in same directory).
 * <p>
 * This class is prompted by our need to be able to install and run multiple instances the NetarchiveSuite in our
 * test-environment simultaneously.
 */
public class CreateTestInstance {
    /** The source configuration file. */
    private File source;
    /**
     * The configuration instance. Loaded from the source file, changed and saved.
     */
    private XmlStructure deployConfiguration;
    /** The string value of the calculated offset. */
    private String offsetVal;
    /** The paths to where the positions the offset are to be used. */
    private OffsetSystem[] offsetPaths;
    /** The new value for the HTTP port. */
    private String httpPortVal;
    /** The path to the HTTP port. */
    private String[] httpPortPath;
    /** The new value for the environment name. */
    private String environmentNameVal;
    /** The path to the environment name. */
    private String[] environmentNamePath;
    /** The new value of the mail receiver. */
    private String mailReceiverVal;
    /** The path to the mail receiver. */
    private String[] mailReceiverPath;

    /**
     * The constructor.
     *
     * @param configSource The source configuration file.
     */
    public CreateTestInstance(File configSource) {
        ArgumentNotValid.checkNotNull(configSource, "File configSource");

        source = configSource;
        deployConfiguration = new XmlStructure(source, Charset.defaultCharset().name());

        offsetPaths = new OffsetSystem[0];
        offsetVal = "";
    }

    /**
     * Function to apply the variables.
     *
     * @param offset The input offset value (1-9 below httpPort).
     * @param httpPort The new value for the HTTP port.
     * @param environmentName The new value for the environment name.
     * @param mailReceiver The new value for the mailReceiver.
     */
    public void applyTestArguments(String offset, String httpPort, String environmentName, String mailReceiver) {
        ArgumentNotValid.checkNotNullOrEmpty(offset, "String offset");
        ArgumentNotValid.checkNotNullOrEmpty(httpPort, "String httpPort");
        ArgumentNotValid.checkNotNullOrEmpty(environmentName, "String environmentName");
        ArgumentNotValid.checkNotNullOrEmpty(mailReceiver, "String mailReceiver");

        // calculate offset
        int offsetInt = (new Integer(httpPort)).intValue() - (new Integer(offset)).intValue();

        if (offsetInt > Constants.TEST_OFFSET_INTEGER_MAXIMUM_VALUE || offsetInt < 0) {
            System.err.print(Constants.MSG_ERROR_TEST_OFFSET);
            System.out.println();
            System.exit(1);
        }
        // change integer to string (easiest way to change integer to String)
        offsetVal = Integer.toString(offsetInt);

        // vaildate the environment name.
        if (!Constants.validEnvironmentName(environmentName)) {
            System.err.print(Constants.MSG_ERROR_INVALID_ENVIRONMENT_NAME + environmentName);
            System.out.println();
            System.exit(1);
        }

        // Get values
        httpPortVal = httpPort;
        environmentNameVal = environmentName;
        mailReceiverVal = mailReceiver;

        // make paths
        httpPortPath = Constants.COMPLETE_HTTP_PORT_LEAF;
        environmentNamePath = Constants.COMPLETE_ENVIRONMENT_NAME_LEAF;
        mailReceiverPath = Constants.SETTINGS_NOTIFICATION_RECEIVER_PATH;

        // make offset paths
        offsetPaths = new OffsetSystem[] {
                new OffsetSystem(Constants.TEST_OFFSET_MONITOR_JMX_PORT, Constants.COMPLETE_JMX_PORT_PATH),
                new OffsetSystem(Constants.TEST_OFFSET_MONITOR_RMI_PORT, Constants.COMPLETE_JMX_RMIPORT_PATH),
                new OffsetSystem(Constants.TEST_OFFSET_HERITRIX_GUI_PORT,
                        Constants.COMPLETE_HARVEST_HERITRIX_GUI_PORT_PATH),
                new OffsetSystem(Constants.TEST_OFFSET_HERITRIX_JMX_PORT, Constants.COMPLETE_HARVEST_HERITRIX_JMX_PORT),
        // new OffsetSystem(Constants.TEST_OFFSET_ARCHIVE_DB_URL_PORT,
        // Constants.COMPLETE_ARCHIVE_DATABASE_PORT),
        // new OffsetSystem(Constants.TEST_OFFSET_HARVEST_DB_URL_PORT,
        // Constants.COMPLETE_HARVEST_DATABASE_PORT)
        };

        // apply the arguments
        apply();
    }

    /**
     * Applies the new variables. Goes through all element instances and applies the variables.
     */
    @SuppressWarnings("unchecked")
    private void apply() {
        // apply on root
        applyOnElement(deployConfiguration.getRoot());

        List<Element> physLocs = deployConfiguration.getChildren(Constants.DEPLOY_PHYSICAL_LOCATION);

        for (Element pl : physLocs) {
            // apply on every physical location
            applyOnElement(pl);

            List<Element> machines = pl.elements(Constants.DEPLOY_MACHINE);
            for (Element mac : machines) {
                // apply on every machine
                applyOnElement(mac);

                List<Element> applications = mac.elements(Constants.DEPLOY_APPLICATION_NAME);
                for (Element app : applications) {
                    // apply on every application
                    applyOnElement(app);

                    applyEnvironmentNameOnBaseFileDir(app);
                }
            }
        }
    }

    /**
     * Applies the new variables on a specific element.
     *
     * @param e The element where the variables are to be applied.
     */
    private void applyOnElement(Element e) {
        // Check argument valid
        ArgumentNotValid.checkNotNull(e, "Element e");

        // Check the following!
        deployConfiguration.overWriteOnly(e, httpPortVal, httpPortPath);
        deployConfiguration.overWriteOnly(e, environmentNameVal, environmentNamePath);
        deployConfiguration.overWriteOnly(e, mailReceiverVal, mailReceiverPath);

        for (OffsetSystem ofs : offsetPaths) {
            deployConfiguration.overWriteOnlyInt(e, ofs.getIndex(), offsetVal.charAt(0), ofs.getPath());
        }
    }

    /**
     * Applies the environment name on the name of the file-directories. Thus: fileDir -> fileDir/environmentName
     *
     * @param app The application where this has to be applied.
     */
    private void applyEnvironmentNameOnBaseFileDir(Element app) {
        ArgumentNotValid.checkNotNull(app, "Element app");

        // Get the list of leaf elements along the path to the base_file_dir
        List<Element> elems = XmlStructure.getAllChildrenAlongPath(app.element(Constants.COMPLETE_SETTINGS_BRANCH),
                Constants.SETTINGS_BITARCHIVE_BASEFILEDIR_LEAF);
        // append the environment name as sub directory to these leafs.
        for (Element el : elems) {
            StringBuilder content = new StringBuilder(el.getText().trim());
            // check if windows format has been used (if the index of the
            // windows directory separator is different from -1).
            if (content.indexOf(Constants.BACKSLASH) > -1) {
                content.append(Constants.BACKSLASH + environmentNameVal);
            } else {
                content.append(Constants.SLASH + environmentNameVal);
            }
            // then set new value.
            el.setText(content.toString());
        }
    }

    /**
     * Creates a file containing the new configuration instance.
     *
     * @param filename The name of the file to be written.
     * @throws IOException If anything goes wrong.
     */
    public void createConfigurationFile(String filename) throws IOException {
        ArgumentNotValid.checkNotNullOrEmpty(filename, "String filename");
        File f = new File(filename);

        FileWriter fw = new FileWriter(f);
        try {
            fw.write(deployConfiguration.getXML());
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    /**
     * Structure for handling where to apply the new offset value.
     */
    private static class OffsetSystem {
        /** The index of the decimal to be replaced by the offset. */
        private int index;
        /** The path to the leaf where the offset are to be applied. */
        private String[] path;

        /**
         * The constructor.
         *
         * @param i The index variable.
         * @param p The path variable.
         */
        public OffsetSystem(int i, String[] p) {
            index = i;
            path = p;
        }

        /**
         * For retrieving the index.
         *
         * @return The index where the offset should be applied.
         */
        public int getIndex() {
            return index;
        }

        /**
         * For retrieving the path.
         *
         * @return The path in the xml-structure to the element which should have a character changed.
         */
        public String[] getPath() {
            return path;
        }
    }
}
