#!/bin/bash
echo --------------------------------------------
echo STARTING MACHINE: test@kb-test-adm-001.kb.dk
ssh test@kb-test-adm-001.kb.dk ". /etc/profile; /home/test/test/conf/startall.sh; sleep 5; cat /home/test/test/*.log"
echo --------------------------------------------
echo STARTING MACHINE: ba-test@kb-test-bar-010.bitarkiv.kb.dk
ssh ba-test@kb-test-bar-010.bitarkiv.kb.dk "cmd /c  c:\Documents and Settings\ba-test\test\conf\startall.bat " 
echo --------------------------------------------
echo STARTING MACHINE: ba-test@kb-test-bar-011.bitarkiv.kb.dk
ssh ba-test@kb-test-bar-011.bitarkiv.kb.dk "cmd /c  c:\Documents and Settings\ba-test\test\conf\startall.bat " 
echo --------------------------------------------
echo STARTING MACHINE: test@kb-test-har-001.kb.dk
ssh test@kb-test-har-001.kb.dk ". /etc/profile; /home/test/test/conf/startall.sh; sleep 5; cat /home/test/test/*.log"
echo --------------------------------------------
echo STARTING MACHINE: test@kb-test-har-002.kb.dk
ssh test@kb-test-har-002.kb.dk ". /etc/profile; /home/test/test/conf/startall.sh; sleep 5; cat /home/test/test/*.log"
echo --------------------------------------------
echo STARTING MACHINE: test@kb-test-acs-001.kb.dk
ssh test@kb-test-acs-001.kb.dk ". /etc/profile; /home/test/test/conf/startall.sh; sleep 5; cat /home/test/test/*.log"
echo --------------------------------------------
