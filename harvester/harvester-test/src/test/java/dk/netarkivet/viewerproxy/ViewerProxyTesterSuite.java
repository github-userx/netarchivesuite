/*
 * #%L
 * Netarchivesuite - harvester - test
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

package dk.netarkivet.viewerproxy;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Unit test-suite covering all the classes in the
 * dk.netarkivet.viewerproxy package. 
 *
 */
public class ViewerProxyTesterSuite {
    /**
     * Create a test suite just for these tests.
     */
    public static Test suite() {
        TestSuite suite;
        suite = new TestSuite(ViewerProxyTesterSuite.class.getName());
        addToSuite(suite);
        return suite;
    }

    /**
     * Add the tests here.
     */
    public static void addToSuite(TestSuite suite) {
        suite.addTestSuite(ARCArchiveAccessTester.class);
        suite.addTestSuite(CommandResolverTester.class);
        suite.addTestSuite(CrawlLogLinesMatchingRegexpTester.class);
        suite.addTestSuite(DelegatingControllerTester.class);
        suite.addTestSuite(GetDataResolverTester.class);
        suite.addTestSuite(MissingURIRecorderTester.class);
        suite.addTestSuite(NotifyingURIResolverTester.class);
        suite.addTestSuite(UnknownCommandResolverTester.class);
        suite.addTestSuite(URIObserverTester.class);
	suite.addTestSuite(ViewerProxyTester.class);
        suite.addTestSuite(WebProxyTester.class);
    }

    public static void main(String[] args) {
        String[] args2 = {"-noloading", ViewerProxyTesterSuite.class.getName()};
        TestRunner.main(args2);
    }
}
