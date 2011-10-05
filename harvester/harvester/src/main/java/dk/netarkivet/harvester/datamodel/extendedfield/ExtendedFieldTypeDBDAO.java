/* File:        $Id$Id$
 * Revision:    $Revision$Revision$
 * Author:      $Author$Author$
 * Date:        $Date$Date$
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2010 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package dk.netarkivet.harvester.datamodel.extendedfield;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.UnknownID;
import dk.netarkivet.common.utils.DBUtils;
import dk.netarkivet.common.utils.ExceptionUtils;
import dk.netarkivet.harvester.datamodel.DBSpecifics;
import dk.netarkivet.harvester.datamodel.HarvestDBConnection;

public class ExtendedFieldTypeDBDAO extends ExtendedFieldTypeDAO {
    /** The logger for this class. */
    private final Log log = LogFactory.getLog(getClass());

    protected ExtendedFieldTypeDBDAO() {

        Connection connection = HarvestDBConnection.get();
        try {
            DBSpecifics.getInstance().updateTable(
                    DBSpecifics.EXTENDEDFIELDTYPE_TABLE,
                    DBSpecifics.EXTENDEDFIELDTYPE_TABLE_REQUIRED_VERSION);

            DBSpecifics.getInstance().updateTable(
                    DBSpecifics.EXTENDEDFIELD_TABLE,
                    DBSpecifics.EXTENDEDFIELD_TABLE_REQUIRED_VERSION);

            DBSpecifics.getInstance().updateTable(
                    DBSpecifics.EXTENDEDFIELDVALUE_TABLE,
                    DBSpecifics.EXTENDEDFIELDVALUE_TABLE_REQUIRED_VERSION);
            
        } finally {
            HarvestDBConnection.release(connection);
        }
    }
    
    protected Connection getConnection() {
            return HarvestDBConnection.get();
    }
    
    @Override
    public boolean exists(Long aExtendedfieldtypeId) {
        ArgumentNotValid.checkNotNull(aExtendedfieldtypeId,
                "Long aExtendedfieldtypeId");

        Connection c = getConnection();
        try {
            return exists(c, aExtendedfieldtypeId);
        } finally {
            HarvestDBConnection.release(c);
        }

    }

    private synchronized boolean exists(Connection c, Long aExtendedfieldtypeId) {
        return 1 == DBUtils
                .selectLongValue(
                        c,
                        "SELECT COUNT(*) FROM extendedfieldtype WHERE extendedfieldtype_id = ?",
                        aExtendedfieldtypeId);
    }

	@Override
    public synchronized ExtendedFieldType read(Long aExtendedfieldtypeId) {
        ArgumentNotValid.checkNotNull(aExtendedfieldtypeId,
                "aExtendedfieldtypeId");
        Connection connection = getConnection();
        try {
            return read(connection, aExtendedfieldtypeId);
        } finally {
            HarvestDBConnection.release(connection);
        }
    }

	private synchronized ExtendedFieldType read(Connection connection,
            Long aExtendedfieldtypeId) {
        if (!exists(connection, aExtendedfieldtypeId)) {
            throw new UnknownID("Extended FieldType id "
                    + aExtendedfieldtypeId
                    + " is not known in persistent storage");
        }

        ExtendedFieldType extendedFieldType = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("" + "SELECT name "
                    + "FROM   extendedfieldtype "
                    + "WHERE  extendedfieldtype_id = ? ");

            statement.setLong(1, aExtendedfieldtypeId);
            ResultSet result = statement.executeQuery();
            result.next();

            String name = result.getString(1);

            extendedFieldType = new ExtendedFieldType(aExtendedfieldtypeId,
                    name);

            return extendedFieldType;
        } catch (SQLException e) {
            String message = "SQL error reading extended Field "
                    + aExtendedfieldtypeId + " in database" + "\n"
                    + ExceptionUtils.getSQLExceptionCause(e);
            log.warn(message, e);
            throw new IOFailure(message, e);
        }
    }

	@Override
    public synchronized List<ExtendedFieldType> getAll() {
        Connection c = getConnection();
        try {
            List<Long> idList = DBUtils.selectLongList(c,
                    "SELECT extendedfieldtype_id FROM extendedfieldtype");
            List<ExtendedFieldType> extendedFieldTypes = new LinkedList<ExtendedFieldType>();
            for (Long extendedfieldtypeId : idList) {
                extendedFieldTypes.add(read(c, extendedfieldtypeId));
            }
            return extendedFieldTypes;
        } finally {
            HarvestDBConnection.release(c);
        }
    }

    public static synchronized ExtendedFieldTypeDAO getInstance() {
        if (instance == null) {
            instance = new ExtendedFieldTypeDBDAO();
        }
        return instance;
    }
}