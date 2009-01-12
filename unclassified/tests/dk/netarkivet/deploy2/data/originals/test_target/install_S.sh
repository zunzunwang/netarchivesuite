#!/bin/bash
echo --------------------------------------------
echo INSTALLING TO MACHINE: netarkiv@sb-test-har-001.statsbiblioteket.dk
echo copying null.zip to:sb-test-har-001.statsbiblioteket.dk
scp null.zip netarkiv@sb-test-har-001.statsbiblioteket.dk:/home/netarkiv
echo unzipping null.zip at:sb-test-har-001.statsbiblioteket.dk
ssh netarkiv@sb-test-har-001.statsbiblioteket.dk unzip -q -o /home/netarkiv/null.zip -d /home/netarkiv/test
echo copying settings and scripts
scp -r sb-test-har-001.statsbiblioteket.dk/* netarkiv@sb-test-har-001.statsbiblioteket.dk:/home/netarkiv/test/conf/
echo make scripts executable
ssh netarkiv@sb-test-har-001.statsbiblioteket.dk "chmod +x /home/netarkiv/test/conf/*.sh "
echo make password files readonly
ssh netarkiv@sb-test-har-001.statsbiblioteket.dk "chmod 400 /home/netarkiv/test/conf/jmxremote.password"
echo --------------------------------------------
echo INSTALLING TO MACHINE: netarkiv@sb-test-bar-001.statsbiblioteket.dk
echo copying null.zip to:sb-test-bar-001.statsbiblioteket.dk
scp null.zip netarkiv@sb-test-bar-001.statsbiblioteket.dk:/home/netarkiv
echo unzipping null.zip at:sb-test-bar-001.statsbiblioteket.dk
ssh netarkiv@sb-test-bar-001.statsbiblioteket.dk unzip -q -o /home/netarkiv/null.zip -d /home/netarkiv/test
echo copying settings and scripts
scp -r sb-test-bar-001.statsbiblioteket.dk/* netarkiv@sb-test-bar-001.statsbiblioteket.dk:/home/netarkiv/test/conf/
echo make scripts executable
ssh netarkiv@sb-test-bar-001.statsbiblioteket.dk "chmod +x /home/netarkiv/test/conf/*.sh "
echo make password files readonly
ssh netarkiv@sb-test-bar-001.statsbiblioteket.dk "chmod 400 /home/netarkiv/test/conf/jmxremote.password"
echo --------------------------------------------
echo INSTALLING TO MACHINE: netarkiv@sb-test-acs-001.statsbiblioteket.dk
echo copying null.zip to:sb-test-acs-001.statsbiblioteket.dk
scp null.zip netarkiv@sb-test-acs-001.statsbiblioteket.dk:/home/netarkiv
echo unzipping null.zip at:sb-test-acs-001.statsbiblioteket.dk
ssh netarkiv@sb-test-acs-001.statsbiblioteket.dk unzip -q -o /home/netarkiv/null.zip -d /home/netarkiv/test
echo copying settings and scripts
scp -r sb-test-acs-001.statsbiblioteket.dk/* netarkiv@sb-test-acs-001.statsbiblioteket.dk:/home/netarkiv/test/conf/
echo make scripts executable
ssh netarkiv@sb-test-acs-001.statsbiblioteket.dk "chmod +x /home/netarkiv/test/conf/*.sh "
echo make password files readonly
ssh netarkiv@sb-test-acs-001.statsbiblioteket.dk "chmod 400 /home/netarkiv/test/conf/jmxremote.password"
echo --------------------------------------------
