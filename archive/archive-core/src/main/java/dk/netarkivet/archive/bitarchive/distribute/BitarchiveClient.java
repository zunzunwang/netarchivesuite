/*
 * #%L
 * Netarchivesuite - archive
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
package dk.netarkivet.archive.bitarchive.distribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.netarkivet.archive.checksum.distribute.CorrectMessage;
import dk.netarkivet.archive.checksum.distribute.GetAllChecksumsMessage;
import dk.netarkivet.archive.checksum.distribute.GetAllFilenamesMessage;
import dk.netarkivet.archive.checksum.distribute.GetChecksumMessage;
import dk.netarkivet.archive.distribute.ReplicaClient;
import dk.netarkivet.common.distribute.ChannelID;
import dk.netarkivet.common.distribute.Channels;
import dk.netarkivet.common.distribute.JMSConnection;
import dk.netarkivet.common.distribute.JMSConnectionFactory;
import dk.netarkivet.common.distribute.RemoteFile;
import dk.netarkivet.common.distribute.arcrepository.ReplicaType;
import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.batch.FileBatchJob;

/**
 * Proxy for remote bitarchive. Establishes a JMS connection to the remote bitarchive.
 */
public final class BitarchiveClient implements ReplicaClient {

    // Each message is assigned a message id
    /** The log. */
    private static final Logger log = LoggerFactory.getLogger(BitarchiveClient.class);

    /** Connection to JMS provider. */
    private JMSConnection jmsCon;

    // connection information
    /** The ALL_BA channel for this replica. */
    private ChannelID allBa;
    /** The ANY_BA channel for this replica. */
    private ChannelID anyBa;
    /** The THE_BAMON channel for this replica. */
    private ChannelID theBamon;
    /** The channel to the ArcRepository. */
    private ChannelID clientId = Channels.getTheRepos();
    /** The name of the replica whose client this is. */
    private String replicaId;

    /**
     * Establish the connection to the server.
     *
     * @param allBaIn topic to all bitarchives
     * @param anyBaIn queue to one of the bitarchives
     * @param theBamonIn queue to the bitarchive monitor
     * @throws IOFailure If there is a problem making the connection.
     */
    private BitarchiveClient(ChannelID allBaIn, ChannelID anyBaIn, ChannelID theBamonIn) throws IOFailure {
        this.allBa = allBaIn;
        this.anyBa = anyBaIn;
        this.theBamon = theBamonIn;
        replicaId = Channels.retrieveReplicaFromIdentifierChannel(theBamon.getName()).getId();
        jmsCon = JMSConnectionFactory.getInstance();
    }

    /**
     * Factory that establish the connection to the server.
     *
     * @param allBaIn topic to all bitarchives
     * @param anyBaIn queue to one of the bitarchives
     * @param theBamonIn queue to the bitarchive monitor
     * @return A BitarchiveClient
     * @throws IOFailure If there is a problem making the connection.
     */
    public static BitarchiveClient getInstance(ChannelID allBaIn, ChannelID anyBaIn, ChannelID theBamonIn)
            throws IOFailure {
        return new BitarchiveClient(allBaIn, anyBaIn, theBamonIn);
    }

    /**
     * Submit a get request to the bitarchive.
     *
     * @param arcfile The file containing the requested record
     * @param index Offset of the ARC record in the file
     * @return The submitted message or null if an error occured
     */
    public GetMessage get(String arcfile, long index) {
        ArgumentNotValid.checkNotNullOrEmpty(arcfile, "arcfile");
        ArgumentNotValid.checkNotNegative(index, "index");

        // Create and send get message
        GetMessage msg = new GetMessage(allBa, clientId, arcfile, index);
        jmsCon.send(msg);

        return msg;
    }

    /**
     * Submit an already constructed batch message to the archive. The reply goes directly back to whoever sent the
     * message.
     *
     * @param msg the message to be processed by the get command.
     */
    public void sendGetMessage(GetMessage msg) {
        ArgumentNotValid.checkNotNull(msg, "msg");

        log.debug("Resending get message '{}' to bitarchives", msg);

        try {
            jmsCon.resend(msg, Channels.getAllBa());
        } catch (Throwable t) {
            log.warn("Failure while resending {}", msg, t);
            try {
                msg.setNotOk(t);
                jmsCon.reply(msg);
            } catch (Throwable t1) {
                log.warn("Failed to send error message back", t1);
            }
        }
    }

    /**
     * Submit an already constructed getfile message to the archive.
     *
     * @param msg get file message to retrieve.
     */
    public void sendGetFileMessage(GetFileMessage msg) {
        ArgumentNotValid.checkNotNull(msg, "msg");
        log.debug("Resending get file message '{}' to bitarchives", msg);
        jmsCon.resend(msg, this.allBa);
    }

    /**
     * Forward the message to ALL_BA.
     *
     * @param msg the message to forward.
     */
    public void sendRemoveAndGetFileMessage(RemoveAndGetFileMessage msg) {
        ArgumentNotValid.checkNotNull(msg, "msg");
        jmsCon.resend(msg, this.allBa);
    }

    /**
     * Sends a message to terminate a running batchjob.
     *
     * @param batchID The ID of the batchjob to terminate.
     * @throws ArgumentNotValid If the batchID is either null or the empty string.
     */
    public void sendBatchTerminationMessage(String batchID) throws ArgumentNotValid {
        ArgumentNotValid.checkNotNullOrEmpty(batchID, "String batchID");
        // create and send the BatchTerminationMessage.
        BatchTerminationMessage msg = new BatchTerminationMessage(this.allBa, batchID);
        jmsCon.send(msg);
    }

    /**
     * Submit an upload request to the bitarchive.
     *
     * @param rf The file to upload.
     * @param precomputedChecksum A precomputed checksum
     * 		
     * @throws IOFailure If access to file denied.
     * @throws ArgumentNotValid If arcfile is null.
     */
    public void sendUploadMessage(RemoteFile rf, String precomputedChecksum) throws IOFailure, ArgumentNotValid {
        ArgumentNotValid.checkNotNull(rf, "rf");
        UploadMessage up = new UploadMessage(anyBa, clientId, rf);
        up.setPrecomputedChecksum(precomputedChecksum);
        log.debug("Sending upload message\n{}", up.toString());
        jmsCon.send(up);
    }

    /**
     * Submit an already constructed get message to the archive. This is used by the ArcRepository when forwarding batch
     * jobs from its clients.
     *
     * @param bMsg a BatchMessage.
     * @return The submitted message.
     * @throws ArgumentNotValid If message is null.
     */
    public BatchMessage sendBatchJob(BatchMessage bMsg) throws ArgumentNotValid {
        ArgumentNotValid.checkNotNull(bMsg, "bMsg");
        log.debug("Resending batch message '{}' to bitarchive monitor {}", bMsg, this.theBamon);
        jmsCon.resend(bMsg, this.theBamon);
        return bMsg;
    }

    /**
     * Submit a batch job to the archive. This is used by the ArcRepository when it needs to run batch jobs for its own
     * reasons, i.e. when checksumming a file as part of the Store operation.
     *
     * @param replyChannel The channel that the reply of this job should be sent to.
     * @param job The job that should be run on the bit archive handled by this client.
     * @return The submitted message.
     * @throws ArgumentNotValid If any parameter was null.
     * @throws IOFailure If sending the batch message did not succeed.
     */
    public BatchMessage sendBatchJob(ChannelID replyChannel, FileBatchJob job) throws ArgumentNotValid, IOFailure {
        ArgumentNotValid.checkNotNull(replyChannel, "replyChannel");
        ArgumentNotValid.checkNotNull(job, "job");
        BatchMessage bMsg = new BatchMessage(this.theBamon, replyChannel, job, replicaId);
        jmsCon.send(bMsg);
        return bMsg;
    }

    /**
     * Release jms connections.
     */
    public void close() {
        log.debug("Client has been shutdown");
    }

    /**
     * For correcting an erroneous entry in the archive. The message is sent the replica for correcting the 'bad' entry.
     *
     * @param msg The correct message to correct the bad entry in the archive.
     * @throws ArgumentNotValid If the CorrectMessage is null.
     */
    @Override
    public void sendCorrectMessage(CorrectMessage msg) throws ArgumentNotValid {
        ArgumentNotValid.checkNotNull(msg, "CorrectMessage msg");

        jmsCon.resend(msg, theBamon);

        log.debug("Sending CorrectMessage: '{}'", msg);
    }

    /**
     * Method for sending a GetAllFilenamesMessage to a checksum archive.
     *
     * @param msg The GetAllFilenamesMessage, which will be sent through the jms connection to the checksum archive.
     * @throws ArgumentNotValid If the GetAllFilenamesMessage is null.
     */
    public void sendGetAllFilenamesMessage(GetAllFilenamesMessage msg) throws ArgumentNotValid {
        ArgumentNotValid.checkNotNull(msg, "GetAllFilenamesMessage msg");
        // send the message to the archive.
        jmsCon.resend(msg, theBamon);

        // log message.
        log.debug("Resending GetAllFilenamesMessage: '{}'.", msg.toString());
    }

    /**
     * Method for sending the GetAllChecksumMessage to the ChecksumReplica.
     *
     * @param msg The GetAllChecksumMessage, which will be sent through the jms connection to the checksum archive.
     * @throws ArgumentNotValid If the GetAllChecksumsMessage is null.
     */
    public void sendGetAllChecksumsMessage(GetAllChecksumsMessage msg) throws ArgumentNotValid {
        ArgumentNotValid.checkNotNull(msg, "GetAllChecksumsMessage msg");
        // send the message to the archive.
        jmsCon.resend(msg, theBamon);

        // log message.
        log.debug("Sending GetAllChecksumMessage: '{}'.", msg.toString());
    }

    /**
     * Method for retrieving the checksum of a specific arcfile within the archive.
     *
     * @param msg The GetChecksumMessage which will be sent to the checksum archive though the jms connection.
     * @throws ArgumentNotValid If the GetChecksumMessage is null.
     */
    public void sendGetChecksumMessage(GetChecksumMessage msg) throws ArgumentNotValid {
        // Validate arguments
        ArgumentNotValid.checkNotNull(msg, "GetChecksumMessage msg");

        jmsCon.resend(msg, theBamon);

        // log what we are doing.
        log.debug("Sending GetChecksumMessage: '{}'.", msg.toString());
    }

    /**
     * Method for retrieving the checksum of a specific arcfile within the archive.
     *
     * @param replyChannel The channel where the reply should be sent.
     * @param filename The GetChecksumMessage which has been sent to the checksum archive though the jms connection.
     * @return The GetChecksumMessage which is sent.
     * @throws ArgumentNotValid If the reply channel is null or if the filename is either null or the empty string.
     */
    public GetChecksumMessage sendGetChecksumMessage(ChannelID replyChannel, String filename) throws ArgumentNotValid {
        // Validate arguments
        ArgumentNotValid.checkNotNull(replyChannel, "ChannelID replyChannel");
        ArgumentNotValid.checkNotNullOrEmpty(filename, "String filename");

        // Send a GetChecksumMessage to the replica.
        GetChecksumMessage msg = new GetChecksumMessage(theBamon, replyChannel, filename, replicaId);
        jmsCon.send(msg);

        // log what we are doing.
        log.debug("Sending GetChecksumMessage: '{}'.", msg.toString());

        return msg;
    }

    /**
     * Retrieves the type of replica.
     *
     * @return The type of this replica. In this case Bitarchive.
     */
    public ReplicaType getType() {
        return ReplicaType.BITARCHIVE;
    }

}
