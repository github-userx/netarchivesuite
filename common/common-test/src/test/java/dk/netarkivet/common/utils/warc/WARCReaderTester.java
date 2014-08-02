/*
 * #%L
 * Netarchivesuite - common - test
 * %%
 * Copyright (C) 2005 - 2014 The Royal Danish Library, the Danish State and University Library,
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
package dk.netarkivet.common.utils.warc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.util.FileUtils;
import org.junit.Test;

import dk.netarkivet.common.distribute.arcrepository.BitarchiveRecord;

/**
 * A simple test of the WARCREADER that is bundled with Heritrix 1.14.4.
 */
@SuppressWarnings({ "unused" })
public class WARCReaderTester {

    public static final String ARCHIVE_DIR = "tests/dk/netarkivet/common/utils/warc/data/input/";
    public static final String testFileName = "working.warc";

    @Test
    public void testARCReaderClose() throws FileNotFoundException, IOException {
        final File testfile = new File(ARCHIVE_DIR + testFileName);
        FileUtils.copyFile(new File(ARCHIVE_DIR + "fyensdk.warc"), testfile);

        WARCReader reader = WARCReaderFactory.get(testfile);
        WARCRecord record = (WARCRecord) reader.get(0);
        BitarchiveRecord rec = new BitarchiveRecord(record, testFileName);
        record.close();
        reader.close();
        testfile.delete();
    }
}
