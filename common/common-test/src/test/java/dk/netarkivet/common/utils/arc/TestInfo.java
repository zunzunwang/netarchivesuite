/*
 * #%L
 * Netarchivesuite - common - test
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

package dk.netarkivet.common.utils.arc;

import java.io.File;

/**
 * Static constants for utils.arc and also utils.batch testing.
 */
public class TestInfo {
    public static final File BASE_DIR = new File("tests/dk/netarkivet/common/utils/arc/data");
    public static final File WORKING_DIR = new File(BASE_DIR, "working");
    public static final File ORIGINALS_DIR = new File(BASE_DIR, "originals");

    public static final int LINES_IN_FILEDESC = 4;
    public static final int NON_FILEDESC_LINES_IN_INPUT_1 = 12;
    public static final int NON_FILEDESC_LINES_IN_INPUT_2 = 0;
    public static final int NON_FILEDESC_LINES_IN_INPUT_3 = 42;
    public static final File INPUT_1 = new File(WORKING_DIR, "input-1.arc");
    public static final File INPUT_2 = new File(WORKING_DIR, "input-2.arc");
    public static final File INPUT_3 = new File(WORKING_DIR, "input-3.arc");
    public static final File FAIL_ARCHIVE_DIR = new File(WORKING_DIR, "bitarchive1_to_fail");
}
