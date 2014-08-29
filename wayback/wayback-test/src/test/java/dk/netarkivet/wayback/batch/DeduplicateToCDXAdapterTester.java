/*
 * #%L
 * Netarchivesuite - wayback - test
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
package dk.netarkivet.wayback.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.CDXLineToSearchResultAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.testutils.TestFileUtils;
import dk.netarkivet.wayback.TestInfo;

/**
 * Unittest for the class DeduplicateToCDXAdapter.
 */
public class DeduplicateToCDXAdapterTester {

    public static final String DEDUP_CRAWL_LOG = "dedup_crawl_log.txt";
    public static final String DEDUP_CRAWL_STRING = "2009-05-25T13:00:00.992Z   200       "
            + "9717 https://wiki.statsbiblioteket.dk/wiki/summa/img/draft.png LLEE "
            + "https://wiki.statsbiblioteket.dk/wiki/summa/css/screen.css image/png #016 "
            + "20090525130000915+76 sha1:AXH2IFNXC4MUT26SRHRJZHGR3FDAJDNR - duplicate:\"1-1-20090513141823-00008-"
            + "sb-test-har-001.statsbiblioteket.dk.arc,22962264\",content-size:9969";
    public static final String DEDUP_CRAWL_STRING2 = "2011-05-11T16:41:49.968Z 200 50436 http://webtv.metropol.dk/swf/webtv_promohorizontal.swf LLX "
            + "http://www.sporten.dk/sport/fodbold application/x-shockwave-flash #008 20110511164149870+61 "
            + "sha1:KBHBHEUCX5CN7KB3P2ZVBHGCCIFJNIWH - le:IOException@ExtractorSWF,duplicate:"
            + "\"118657-119-20110428163750-00001-kb-prod-har-004.kb.dk.arc,69676377\",content-size:50842";

    @Before
    public void setUp() {
        FileUtils.removeRecursively(TestInfo.WORKING_DIR);
        TestFileUtils.copyDirectoryNonCVS(TestInfo.ORIGINALS_DIR, TestInfo.WORKING_DIR);
        // System.out.println(DEDUP_CRAWL_STRING);
    }

    @After
    public void tearDown() {
        FileUtils.removeRecursively(TestInfo.WORKING_DIR);
    }

    @Test
    public void testCtor() {
        new DeduplicateToCDXAdapter();
    }

    @Test
    public void testAdaptLine() {
        DeduplicateToCDXAdapterInterface adapter = new DeduplicateToCDXAdapter();
        String cdx_line = adapter.adaptLine(DEDUP_CRAWL_STRING);
        CDXLineToSearchResultAdapter adapter2 = new CDXLineToSearchResultAdapter();
        CaptureSearchResult result = adapter2.adapt(cdx_line);
        assertEquals("Should get the arcfilename back out of the cdx line",
                "1-1-20090513141823-00008-sb-test-har-001.statsbiblioteket.dk.arc", result.getFile());
        assertEquals("Should get the right http code out of the cdx line", "200", result.getHttpCode());

        String cdx_line2 = adapter.adaptLine(DEDUP_CRAWL_STRING2);
        CaptureSearchResult result2 = adapter2.adapt(cdx_line2);
        assertEquals("Should get the arcfilename back out of the cdx line",
                "118657-119-20110428163750-00001-kb-prod-har-004.kb.dk.arc", result2.getFile());
        assertEquals("Should get the right http code out of the cdx line", "200", result2.getHttpCode());

    }

    @Test
    public void testAdaptStream() throws IOException {
        InputStream is = new FileInputStream(new File(TestInfo.WORKING_DIR, DEDUP_CRAWL_LOG));
        OutputStream os = new ByteArrayOutputStream();
        DeduplicateToCDXAdapterInterface adapter = new DeduplicateToCDXAdapter();
        adapter.adaptStream(is, os);
        os.close();
        String output = os.toString();
        String[] lines = output.split("\n");
        CDXLineToSearchResultAdapter adapter2 = new CDXLineToSearchResultAdapter();
        for (String line : lines) {
            CaptureSearchResult csr = adapter2.adapt(line);
            assertNotNull("Should have a valid mime type for every line, inclding '" + line + "'", csr.getMimeType());
        }
        assertTrue("expect at least 3 lines of output, got " + lines.length, lines.length > 2);
    }

}
