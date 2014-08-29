/*
 * #%L
 * Netarchivesuite - harvester
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
package dk.netarkivet.harvester.datamodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.PermissionDenied;
import dk.netarkivet.common.exceptions.UnknownID;
import dk.netarkivet.common.utils.Named;
import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.harvester.HarvesterSettings;

/**
 * This class describes a configuration for harvesting a domain. It combines a number of seedlists, a number of
 * passwords, an order template, and some specialised settings to define the way to harvest a domain.
 */
public class DomainConfiguration implements Named {

    /** The class logger. */
    private static final Logger log = LoggerFactory.getLogger(DomainConfiguration.class);

    /** The name of the configuration. */
    private String configName;
    /** The name of the order.xml (Heritrix template) used by this configuration. */
    private String orderXmlName = "";
    /** maximum number of objects harvested for this configuration in a snapshot harvest. */
    private long maxObjects;
    /** The maximum request rate. */
    private int maxRequestRate;
    /** Maximum number of bytes to download in a harvest. */
    private long maxBytes;
    /** The domain associated with this configuration. */
    private String domainName;

    /** The list of seedlists. */
    private List<SeedList> seedlists;

    /** The list of passwords that apply in this configuration. */
    private List<Password> passwords;
    /** The comments associated with this configuration. */
    private String comments;

    /** ID autogenerated by DB. */
    private Long id;

    /** The domainhistory associated with the domain. */
    private DomainHistory domainhistory;

    /** The crawlertraps associated with the domain. */
    private List<String> crawlertraps;

    /**
     * How many objects should be harvested in a harvest to trust that our expected size of objects is less than the
     * default number.
     */
    private static final long MIN_OBJECTS_TO_TRUST_SMALL_EXPECTATION = 50L;
    /** The smallest number of bytes we accept per object. */
    private static final int MIN_EXPECTATION = 1;

    /**
     * Create a new configuration for a domain.
     *
     * @param theConfigName The name of this configuration
     * @param domain The domain that this configuration is for
     * @param seedlists Seedlists to use in this configuration.
     * @param passwords Passwords to use in this configuration.
     */
    public DomainConfiguration(String theConfigName, Domain domain, List<SeedList> seedlists, List<Password> passwords) {
        // ArgumentNotValid.checkNotNull(domain, "Domain domain");
        this(theConfigName, domain.getName(), domain.getHistory(), domain.getCrawlerTraps(), seedlists, passwords);
    }

    /**
     * Alternate constructor. TODO Filter all history not relevant for this configuration
     *
     * @param theConfigName theConfigName The name of this configuration
     * @param domainName The name of the domain that this configuration is for
     * @param history The domainhistory belonging the given domain
     * @param crawlertraps The domainhistory belonging the given domain
     * @param seedlists Seedlists to use in this configuration
     * @param passwords Passwords to use in this configuration.
     */
    public DomainConfiguration(String theConfigName, String domainName, DomainHistory history,
            List<String> crawlertraps, List<SeedList> seedlists, List<Password> passwords) {
        ArgumentNotValid.checkNotNullOrEmpty(theConfigName, "theConfigName");
        ArgumentNotValid.checkNotNullOrEmpty(domainName, "domainName");
        ArgumentNotValid.checkNotNull(passwords, "passwords");
        ArgumentNotValid.checkNotNullOrEmpty(seedlists, "seedlists");
        ArgumentNotValid.checkNotNull(passwords, "passwords");

        this.configName = theConfigName;
        this.domainName = domainName;
        this.domainhistory = history; // TODO Filter all history not relevant
        // for this configuration
        this.crawlertraps = crawlertraps;
        this.seedlists = seedlists;
        this.passwords = passwords;
        this.comments = "";
        this.maxRequestRate = Constants.DEFAULT_MAX_REQUEST_RATE;
        this.maxObjects = Constants.DEFAULT_MAX_OBJECTS;
        this.maxBytes = Constants.DEFAULT_MAX_BYTES;
    }

    /**
     * Specify the name of the order.xml template to use.
     *
     * @param ordername order.xml template name
     * @throws ArgumentNotValid if filename null or empty
     */
    public void setOrderXmlName(String ordername) {
        ArgumentNotValid.checkNotNullOrEmpty(ordername, "ordername");
        orderXmlName = ordername;
    }

    /**
     * Specify the maximum number of objects to retrieve from the domain.
     *
     * @param max maximum number of objects to retrieve
     * @throws ArgumentNotValid if max<-1
     */
    public void setMaxObjects(long max) {
        if (max < -MIN_EXPECTATION) {
            String msg = "maxObjects must be either -1 or positive, but was " + max;
            log.debug(msg);
            throw new ArgumentNotValid(msg);
        }

        maxObjects = max;
    }

    /**
     * Specify the maximum request rate to use when harvesting data.
     *
     * @param maxrate the maximum request rate
     * @throws ArgumentNotValid if maxrate<0
     */
    public void setMaxRequestRate(int maxrate) {
        ArgumentNotValid.checkNotNegative(maxrate, "maxrate");

        maxRequestRate = maxrate;
    }

    /**
     * Specify the maximum number of bytes to download from a domain in a single harvest.
     *
     * @param maxBytes Maximum number of bytes to download, or -1 for no limit.
     * @throws ArgumentNotValid if maxBytes < -1
     */
    public void setMaxBytes(long maxBytes) {
        if (maxBytes < -MIN_EXPECTATION) {
            String msg = "DomainConfiguration.maxBytes must be -1 or positive.";
            log.debug(msg);
            throw new ArgumentNotValid(msg);
        }
        this.maxBytes = maxBytes;
    }

    /**
     * Get the configuration name.
     *
     * @return the configuration name
     */
    public String getName() {
        return configName;
    }

    /**
     * Returns comments.
     *
     * @return string containing comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * Returns the name of the order xml file used by the domain.
     *
     * @return name of the order.xml file that should be used when harvesting the domain
     */
    public String getOrderXmlName() {
        return orderXmlName;
    }

    /**
     * Returns the maximum number of objects to harvest from the domain.
     *
     * @return maximum number of objects to harvest
     */
    public long getMaxObjects() {
        return maxObjects;
    }

    /**
     * Returns the maximum request rate to use when harvesting the domain.
     *
     * @return maximum request rate
     */
    public int getMaxRequestRate() {
        return maxRequestRate;
    }

    /**
     * Returns the maximum number of bytes to download during a single harvest of a domain.
     *
     * @return Maximum bytes limit, or -1 for no limit.
     */
    public long getMaxBytes() {
        return maxBytes;
    }

    /**
     * Returns the name of the domain aggregating this configuration.
     *
     * @return the name of the domain aggregating this configuration.
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Get an iterator of seedlists used in this configuration.
     *
     * @return seedlists as iterator
     */
    public Iterator<SeedList> getSeedLists() {
        return seedlists.iterator();
    }

    /**
     * Add a new seedlist to the configuration. Must exist in the associated domain and the equal to that seedlist.
     *
     * @param seedlist the seedlist to add
     * @param domain The domain to check if the seedlist exists
     * @throws ArgumentNotValid if the seedlist is null
     * @throws UnknownID if the seedlist is not defined on the domain
     * @throws PermissionDenied if the seedlist is different from the one on the domain.
     */
    public void addSeedList(Domain domain, SeedList seedlist) {
        ArgumentNotValid.checkNotNull(seedlist, "seedlist");
        SeedList domainSeedlist = domain.getSeedList(seedlist.getName());
        if (!domainSeedlist.equals(seedlist)) {
            String message = "Cannot add seedlist " + seedlist + " to " + this + " as it differs from the one defined "
                    + "for " + domain + ": " + domainSeedlist;
            log.debug(message);
            throw new PermissionDenied(message);
        }
        seedlists.add(domainSeedlist);
    }

    /**
     * Get an iterator of passwords used in this configuration.
     *
     * @return The passwords in an iterator
     */
    public Iterator<Password> getPasswords() {
        return passwords.iterator();
    }

    /**
     * Add password to the configuration.
     *
     * @param password to add (must exist in the domain)
     * @param domain the domain where the password should come from.
     */
    public void addPassword(Domain domain, Password password) {
        ArgumentNotValid.checkNotNull(password, "password");
        Password domainPassword = domain.getPassword(password.getName());
        if (!domainPassword.equals(password)) {
            String message = "Cannot add password " + password + " to " + this + " as it differs from the one defined "
                    + "for " + domain + ": " + domainPassword;
            log.debug(message);
            throw new PermissionDenied(message);
        }
        passwords.add(domainPassword);
    }

    /**
     * Gets the best expectation for how many objects a harvest using this configuration will retrieve, given a job with
     * a maximum limit pr. domain
     *
     * @param objectLimit The maximum limit, or Constants.HERITRIX_MAXOBJECTS_INFINITY for no limit. This limit
     * overrides the limit set on the configuration, unless override is in effect.
     * @param byteLimit The maximum number of bytes that will be used as limit in the harvest. This limit overrides the
     * limit set on the configuration, unless override is in effect.
     * @return The expected number of objects.
     */
    public long getExpectedNumberOfObjects(long objectLimit, long byteLimit) {
        long prevresultfactor = Settings.getLong(HarvesterSettings.ERRORFACTOR_PERMITTED_PREVRESULT);
        HarvestInfo best = DomainHistory.getBestHarvestInfoExpectation(configName, this.domainhistory);

        log.trace("Using domain info '{}' for configuration '{}'", best, toString());

        long expectedObjectSize = getExpectedBytesPerObject(best);
        // The maximum number of objects that the maxBytes or MAX_DOMAIN_SIZE
        // setting gives.
        long maximum;
        if (objectLimit != Constants.HERITRIX_MAXOBJECTS_INFINITY || byteLimit != Constants.HERITRIX_MAXBYTES_INFINITY) {
            maximum = minObjectsBytesLimit(objectLimit, byteLimit, expectedObjectSize);
        } else if (maxObjects != Constants.HERITRIX_MAXOBJECTS_INFINITY
                || maxBytes != Constants.HERITRIX_MAXBYTES_INFINITY) {
            maximum = minObjectsBytesLimit(maxObjects, maxBytes, expectedObjectSize);
        } else {
            maximum = Settings.getLong(HarvesterSettings.MAX_DOMAIN_SIZE);
        }
        // get last number of objects harvested
        long minimum;
        if (best != null) {
            minimum = best.getCountObjectRetrieved();
        } else {
            minimum = NumberUtils.minInf(Constants.HERITRIX_MAXOBJECTS_INFINITY, maxObjects);
        }
        // Calculate the expected number of objects we will harvest.
        long expectation;
        if (best != null) {
            if (best.getStopReason() == StopReason.DOWNLOAD_COMPLETE && maximum != -1) {
                // We set the expectation, so our harvest will exceed the
                // expectation at most <factor> times if the domain is a lot
                // larger than our best guess.
                expectation = minimum + ((maximum - minimum) / prevresultfactor);
            } else {
                // if stopped for different reason than DOWNLOAD_COMPLETE we
                // add half the harvested size to expectation
                expectation = minimum + ((maximum - minimum) / 2);
            }
        } else {
            // Best guess: minimum of default max domain size and domain object
            // limit
            expectation = NumberUtils.minInf(Settings.getLong(HarvesterSettings.MAX_DOMAIN_SIZE), maxObjects);
        }
        // Always limit to domain specifics if set to do so. We always expect
        // to actually hit this limit
        if ((maxObjects > Constants.HERITRIX_MAXOBJECTS_INFINITY && maximum > maxObjects)
                || (maxBytes > Constants.HERITRIX_MAXBYTES_INFINITY && maximum > maxBytes / expectedObjectSize)) {
            maximum = minObjectsBytesLimit(maxObjects, maxBytes, expectedObjectSize);
        }
        // Never return more than allowed maximum
        expectation = Math.min(expectation, maximum);

        log.trace("Expected number of objects for configuration '{}' is {}", toString(), expectation);

        return expectation;
    }

    /**
     * Return the lowest limit for the two values, or MAX_DOMAIN_SIZE if both are infinite, which is the max size we
     * harvest from this domain.
     *
     * @param objectLimit A long value defining an object limit, or 0 for infinite
     * @param byteLimit A long value defining a byte limit, or HarvesterSettings.MAX_DOMAIN_SIZE for infinite.
     * @param expectedObjectSize The expected number of bytes per object
     * @return The lowest of the two boundaries, or MAX_DOMAIN_SIZE if both are unlimited.
     */
    public long minObjectsBytesLimit(long objectLimit, long byteLimit, long expectedObjectSize) {
        long maxObjectsByBytes = byteLimit / expectedObjectSize;
        if (objectLimit != Constants.HERITRIX_MAXOBJECTS_INFINITY) {
            if (byteLimit != Constants.HERITRIX_MAXBYTES_INFINITY) {
                return Math.min(objectLimit, maxObjectsByBytes);
            } else {
                return objectLimit;
            }
        } else {
            if (byteLimit != Constants.HERITRIX_MAXBYTES_INFINITY) {
                return maxObjectsByBytes;
            } else {
                return Settings.getLong(HarvesterSettings.MAX_DOMAIN_SIZE);
            }
        }
    }

    /**
     * How many bytes we can expect the average object of a domain to be. If we have harvested no objects from this
     * domain before, we use a setting EXPECTED_AVERAGE_BYTES_PER_OBJECT. If we have objects, we use the harvestinfo
     * from previous harvests to calculate the harvest, but we only accept a low estimate if the number of harvested
     * objects is greater than the setting MIN_OBJECTS_TO_TRUST_SMALL_EXPECTATION.
     *
     * @param bestInfo The best (newest complete or biggest, as per getBestHarvestInfoExpectation()) harvest info we
     * have for the domain.
     * @return How large we expect the average object to be. This number will be >= MIN_EXPECTATION (unless nothing is
     * harvested and is EXPECTED_AVERAGE_BYTES_PER_OBJECT <= 0).
     */
    private long getExpectedBytesPerObject(HarvestInfo bestInfo) {
        long defaultExpectation = Settings.getLong(HarvesterSettings.EXPECTED_AVERAGE_BYTES_PER_OBJECT);
        if (bestInfo != null && bestInfo.getCountObjectRetrieved() > 0) {
            long expectation = Math.max(MIN_EXPECTATION,
                    bestInfo.getSizeDataRetrieved() / bestInfo.getCountObjectRetrieved());
            if (expectation < defaultExpectation
                    && bestInfo.getCountObjectRetrieved() < MIN_OBJECTS_TO_TRUST_SMALL_EXPECTATION) {
                return defaultExpectation;
            }
            return expectation;
        } else {
            return defaultExpectation;
        }
    }

    /**
     * Set the comments field.
     *
     * @param comments User-entered free-form comments.
     */
    public void setComments(String comments) {
        ArgumentNotValid.checkNotNull(comments, "comments");
        this.comments = comments;
    }

    /**
     * Remove a password from the list of passwords used in this domain.
     *
     * @param passwordName Password to Remove.
     */
    public void removePassword(String passwordName) {
        ArgumentNotValid.checkNotNullOrEmpty(passwordName, "passwordName");
        if (!usesPassword(passwordName)) {
            throw new UnknownID("No password named '" + passwordName + "' found in '" + this + "'");
        }
        for (Iterator<Password> i = passwords.iterator(); i.hasNext();) {
            Password p = i.next();
            if (p.getName().equals(passwordName)) {
                i.remove();
            }
        }
    }

    /**
     * Check whether this domain uses a given password.
     *
     * @param passwordName The given password
     * @return whether the given password is used
     */
    public boolean usesPassword(String passwordName) {
        ArgumentNotValid.checkNotNullOrEmpty(passwordName, "passwordName");
        for (Password p : passwords) {
            if (p.getName().equals(passwordName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the used seedlists to the given list. Note: list is copied.
     *
     * @param newSeedlists The seedlists to use.
     * @param domain The domain where the seedlists should come from
     * @throws ArgumentNotValid if the seedslists are null
     */
    public void setSeedLists(Domain domain, List<SeedList> newSeedlists) {
        ArgumentNotValid.checkNotNull(newSeedlists, "newSeedlists");
        this.seedlists = new ArrayList<SeedList>(newSeedlists.size());
        for (SeedList s : newSeedlists) {
            addSeedList(domain, s);
        }
    }

    /**
     * Sets the used passwords to the given list. Note: list is copied.
     *
     * @param newPasswords The passwords to use.
     * @param domain The domain where the passwords should come from
     * @throws ArgumentNotValid if the passwords are null
     */
    public void setPasswords(Domain domain, List<Password> newPasswords) {
        ArgumentNotValid.checkNotNull(newPasswords, "newPasswords");
        this.passwords = new ArrayList<Password>(newPasswords.size());
        for (Password p : newPasswords) {
            addPassword(domain, p);
        }
    }

    /**
     * Get the ID of this configuration.
     *
     * @return the ID of this configuration
     */
    public long getID() {
        return id;
    }

    /**
     * Set the ID of this configuration. Only for use by DBDAO
     *
     * @param anId use this id for this configuration
     */
    void setID(long anId) {
        this.id = anId;
    }

    /**
     * Check if this configuration has an ID set yet (doesn't happen until the DBDAO persists it).
     *
     * @return true, if the configuration has an ID
     */
    boolean hasID() {
        return id != null;
    }

    /**
     * ToString of DomainConfiguration class.
     *
     * @return a string with info about the instance of this class.
     */
    public String toString() {
        return "Configuration '" + getName() + "' of domain '" + domainName + "'";
    }

    /**
     * Set the crawlerltraps for this configuration.
     *
     * @param someCrawlertraps a list of crawlertraps
     */
    public void setCrawlertraps(List<String> someCrawlertraps) {
        this.crawlertraps = someCrawlertraps;
    }

    /**
     * @return the known crawlertraps for this configuration.
     */
    public List<String> getCrawlertraps() {
        return this.crawlertraps;
    }

    /**
     * @return the domainhistory for this configuration
     */
    public DomainHistory getDomainhistory() {
        return domainhistory;
    }

    /**
     * Set the domainHistory for this configuration.
     *
     * @param newDomainhistory the new domainHistory for this configuration( null is accepted for no History)
     */
    public void setDomainhistory(DomainHistory newDomainhistory) {
        this.domainhistory = newDomainhistory;
    }

}
