/*
 * #%L
 * Netarchivesuite - wayback
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
package dk.netarkivet.wayback.batch.copycode;

import org.apache.commons.httpclient.URIException;
import org.archive.url.UsableURI;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

/**
 * This class overrides the standard wayback canonicalizer in order to use our version of UURIFactory (see Bug 1719).
 * Everything in this class is cut and pasted from wayback's AggressiveUrlCanonicalizer, with substitution of
 * NetarchiveSuiteUURIFactory for UURIFactory.
 *
 * @deprecated use org.archive.wayback.util.url.AggressiveUrlCanonicalizer instead
 */
public class NetarchiveSuiteAggressiveUrlCanonicalizer extends AggressiveUrlCanonicalizer {

    @Override
    public String urlStringToKey(String urlString) throws URIException {

        if (urlString.startsWith("dns:")) {
            return urlString;
        }
        String searchUrl = canonicalize(urlString);
        String scheme = NetarchiveSuiteUrlOperations.urlToScheme(urlString);
        if (scheme != null) {
            searchUrl = searchUrl.substring(scheme.length());
        } else {
            scheme = NetarchiveSuiteUrlOperations.HTTP_SCHEME;
        }

        if (-1 == searchUrl.indexOf("/")) {
            searchUrl = scheme + searchUrl + "/";
        } else {
            searchUrl = scheme + searchUrl;
        }

        // TODO These next few lines look crazy -- need to be reworked.. This
        // was the only easy way I could find to get the correct unescaping
        // out of UURIs, possible a bug. Definitely needs some TLC in any case,
        // as building UURIs is *not* a cheap operation.

        // unescape anything that can be:
        UsableURI tmpURI = NetarchiveSuiteUURIFactory.getInstance(searchUrl);
        tmpURI.setPath(tmpURI.getPath());

        // convert to UURI to perform required URI fixup:
        UsableURI searchURI = NetarchiveSuiteUURIFactory.getInstance(tmpURI.getURI());

        // replace ' ' with '+' (this is only to match Alexa's canonicalization)
        String newPath = searchURI.getEscapedPath().replace("%20", "+");

        // replace multiple consecutive '/'s in the path.
        while (newPath.contains("//")) {
            newPath = newPath.replace("//", "/");
        }

        // this would remove trailing a '/' character, unless the path is empty
        // but we're not going to do this just yet..
        // if((newPath.length() > 1) && newPath.endsWith("/")) {
        // newPath = newPath.substring(0,newPath.length()-1);
        // }

        StringBuilder sb = new StringBuilder(searchUrl.length());
        sb.append(searchURI.getHostBasename());

        // omit port if scheme default:
        int defaultSchemePort = NetarchiveSuiteUrlOperations.schemeToDefaultPort(scheme);
        if (searchURI.getPort() != defaultSchemePort && searchURI.getPort() != -1) {

            sb.append(":").append(searchURI.getPort());
        }

        sb.append(newPath);
        if (searchURI.getEscapedQuery() != null) {
            sb.append("?").append(searchURI.getEscapedQuery());
        }

        return sb.toString();
    }
}
