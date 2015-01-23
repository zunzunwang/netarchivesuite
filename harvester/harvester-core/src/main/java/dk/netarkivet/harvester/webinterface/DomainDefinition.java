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

package dk.netarkivet.harvester.webinterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.ForwardedToErrorPage;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.DomainUtils;
import dk.netarkivet.common.utils.I18n;
import dk.netarkivet.common.webinterface.HTMLUtils;
import dk.netarkivet.harvester.datamodel.Domain;
import dk.netarkivet.harvester.datamodel.DomainConfiguration;
import dk.netarkivet.harvester.datamodel.DomainDAO;
import dk.netarkivet.harvester.datamodel.NamedUtils;
import dk.netarkivet.harvester.datamodel.SeedList;
import dk.netarkivet.harvester.datamodel.extendedfield.ExtendedFieldTypes;

/**
 * Utility class for handling update of domain from the domain jsp page.
 */
@SuppressWarnings("unused")
public class DomainDefinition {

    private static Log log = LogFactory.getLog(DomainDefinition.class.getName());

    /** Private constructor to prevent public construction of this class. */
    private DomainDefinition() {
    }

    /**
     * Extracts all required parameters from the request, checks for any inconsistencies, and passes the requisite data
     * to the updateDomain method for processing.
     * <p>
     * For reference, the parameters for this page look something like
     * http://localhost:8076/HarvestDefinition/Definitions-edit-domain.jsp?
     * update=1&name=netarkivet.dk&default=defaultconfig&configName=&order_xml=&
     * load=&maxObjects=&urlListName=&seedList=+&passwordName=&passwordDomain=& passwordRealm=&userName=&password=&
     * crawlertraps=%2Fcgi-bin%2F*%0D%0A%2Ftrap%2F*%0D%0A
     * <p>
     * update: This method throws an exception if update is not set
     * <p>
     * name: must be the name of a known domain
     * <p>
     * comments: optional user-entered comments about the domain
     * <p>
     * default: the defaultconfig is set to this value. Must be non-null and a known configuration of this domain.
     * <p>
     * crawlertraps: a newline-separated list of urls to be ignored. May be empty or null
     * <p>
     * alias: If set, this domain is an alias of the set domain renewAlias: If set, the alias date should be renewed
     *
     * @param context The context of this request
     * @param i18n I18n information
     * @throws IOFailure on updateerrors in the DAO
     * @throws ForwardedToErrorPage if domain is not found, if the edition is out-of-date, or if parameters are missing
     * or invalid
     */
    public static void processRequest(PageContext context, I18n i18n) {
        ArgumentNotValid.checkNotNull(context, "PageContext context");
        ArgumentNotValid.checkNotNull(i18n, "I18n i18n");

        HTMLUtils.forwardOnEmptyParameter(context, Constants.DOMAIN_PARAM, Constants.DEFAULT_PARAM);
        ServletRequest request = context.getRequest();
        String name = request.getParameter(Constants.DOMAIN_PARAM).trim();

        if (!DomainDAO.getInstance().exists(name)) {
            HTMLUtils.forwardWithErrorMessage(context, i18n, "errormsg;unknown.domain.0", name);
            throw new ForwardedToErrorPage("Unknown domain '" + name + "'");
        }
        Domain domain = DomainDAO.getInstance().read(name);

        // check the edition number before updating
        long edition = HTMLUtils.parseOptionalLong(context, Constants.EDITION_PARAM, -1L);

        if (domain.getEdition() != edition) {
            HTMLUtils.forwardWithRawErrorMessage(
                    context,
                    i18n,
                    "errormsg;domain.definition.changed.0.retry.1",
                    "<br/><a href=\"Definitions-edit-domain.jsp?" + Constants.DOMAIN_PARAM + "="
                            + HTMLUtils.escapeHtmlValues(HTMLUtils.encode(name)) + "\">", "</a>");
            throw new ForwardedToErrorPage("Domain '" + name + "' has changed");
        }

        // default configuration
        String defaultConf = request.getParameter(Constants.DEFAULT_PARAM);
        if (!domain.hasConfiguration(defaultConf)) {
            HTMLUtils.forwardWithErrorMessage(context, i18n, "errormsg;unknown.default.configuration.0.for.1",
                    defaultConf, name);
            throw new ForwardedToErrorPage("Unknown default configuration '" + defaultConf + "'");
        }

        String crawlertraps = request.getParameter(Constants.CRAWLERTRAPS_PARAM);
        if (crawlertraps == null) {
            crawlertraps = "";
        }
        String comments = request.getParameter(Constants.COMMENTS_PARAM);
        if (comments == null) {
            comments = "";
        }
        String alias = request.getParameter(Constants.ALIAS_PARAM);
        if (alias == null) {
            alias = "";
        }

        String aliasRenew = request.getParameter(Constants.RENEW_ALIAS_PARAM);
        if (aliasRenew == null) {
            aliasRenew = "no";
        }

        boolean renewAlias = aliasRenew.equals("yes");

        ExtendedFieldValueDefinition.processRequest(context, i18n, domain, ExtendedFieldTypes.DOMAIN);

        updateDomain(domain, defaultConf, crawlertraps, comments, alias, renewAlias);
    }

    /**
     * This updates the given domain in the database.
     *
     * @param domain the given domain
     * @param defaultConfig the name of the default configuration
     * @param crawlertraps the current crawlertraps stated for the domain
     * @param comments User-defined comments for the domain
     * @param alias if this is non-null, this domain is an alias of 'alias'.
     * @param renewAlias true, if alias is to be updated even if it is not changed
     */
    private static void updateDomain(Domain domain, String defaultConfig, String crawlertraps, String comments,
            String alias, boolean renewAlias) {
        // Set default configuration
        domain.setDefaultConfiguration(defaultConfig);
        domain.setComments(comments);

        // Update crawlertraps
        List<String> trapList = new ArrayList<String>();
        if (crawlertraps.length() > 0) {
            String[] traps = crawlertraps.split("[\\r\\n]+");
            for (String trap : traps) {
                if (trap.trim().length() > 0) {
                    trapList.add(trap);
                }
            }
        }
        domain.setCrawlerTraps(trapList, true);

        // Update alias information

        // If alias is empty string, do not regard this domain as an alias any
        // more.
        // If alias is not-empty, update only if alias is different from
        // oldAlias or
        // This only updates alias if it is required: See javadoc below for
        // needToUpdateAlias() 
        String oldAlias = null;
        if (domain.getAliasInfo() != null) {
            oldAlias = domain.getAliasInfo().getAliasOf();
        }
        String newAlias;
        // If alias is empty string, this domain is or should not be an alias.

        if (alias.trim().isEmpty()) {
            newAlias = null;
        } else {
            newAlias = alias.trim();
        }

        if (needToUpdateAlias(oldAlias, newAlias, renewAlias)) {
            domain.updateAlias(newAlias);
        }

        DomainDAO.getInstance().update(domain);
    }

    /**
     * Define the cases where we want to update the alias information. 1. The alias information is updated, if the new
     * alias is null, and the old alias is different from null 2. The alias information is updated, if the new alias is
     * different from null, and old alias is null 3. The alias information is updated, if the new alias is different
     * from null, and the old alias is different from null, and they are not either not equal, or renewAlias is true
     *
     * @param oldAlias the old alias (could be null)
     * @param newAlias the new alias (could be null)
     * @param renewAlias should we renew alias, if the alias is unchanged?
     * @return true, if we want to update the alias information, false otherwise
     */
    private static boolean needToUpdateAlias(String oldAlias, String newAlias, boolean renewAlias) {
        boolean needToUpdate = false;
        // If new alias is null: update if old alias is different from null
        if (newAlias == null) {
            if (oldAlias != null) {
                needToUpdate = true;
            }
        } else { // newAlias is not null
            if (oldAlias == null) {
                needToUpdate = true;
            } else {
                if (oldAlias.equals(newAlias)) {
                    if (renewAlias) {
                        needToUpdate = true;
                    }
                } else {
                    needToUpdate = true;
                }
            }
        }
        return needToUpdate;
    }

    /**
     * Creates domains with default attributes.
     *
     * @param domains a list of domain names
     * @return List of the non-empty domain names that were not legal domain names or already exist.
     */
    public static List<String> createDomains(String... domains) {
        DomainDAO ddao = DomainDAO.getInstance();
        List<String> illegals = new ArrayList<String>();
        List<Domain> domainsToCreate = new ArrayList<Domain>();
        for (String domain : domains) {
            if (DomainUtils.isValidDomainName(domain) && !ddao.exists(domain)) {
                domainsToCreate.add(Domain.getDefaultDomain(domain));
            } else {
                if (domain.trim().length() > 0) {
                    illegals.add(domain);
                }
            }
        }

        ddao.create(domainsToCreate);

        return illegals;
    }

    /**
     * Creates a link to the domain edit page.
     *
     * @param domain The domain to show with a link
     * @return HTML code with the link and the domain name shown
     */
    public static String makeDomainLink(String domain) {
        ArgumentNotValid.checkNotNullOrEmpty(domain, "domain");
        String url = "/HarvestDefinition/Definitions-edit-domain.jsp?" + Constants.DOMAIN_PARAM + "="
                + HTMLUtils.encode(domain);
        return "<a href=\"" + url + "\">" + HTMLUtils.escapeHtmlValues(domain) + "</a>";
    }

    /**
     * Creates a url based on the supplied request where all the parameters are the same, except the
     * <code>ShowUnusedConfigurations</code> boolean, which is flipped.
     *
     * @param request The original 'create domain' request to based the new url on.
     * @return The new url with the <code>ShowUnusedConfigurations</code> boolean switched.
     */
    public static String createDomainUrlWithFlippedShowConfigurations(ServletRequest request) {
        boolean showUnusedConfigurationsParam = Boolean.parseBoolean(request
                .getParameter(Constants.SHOW_UNUSED_CONFIGURATIONS_PARAM));
        boolean showUnusedSeedsParam = Boolean.parseBoolean(request.getParameter(Constants.SHOW_UNUSED_SEEDS_PARAM));
        StringBuilder urlBuilder = new StringBuilder("/HarvestDefinition/Definitions-edit-domain.jsp?");
        urlBuilder
                .append(Constants.DOMAIN_PARAM + "=" + HTMLUtils.encode(request.getParameter(Constants.DOMAIN_PARAM)));
        urlBuilder.append("&" + Constants.SHOW_UNUSED_CONFIGURATIONS_PARAM + "="
                + Boolean.toString(!showUnusedConfigurationsParam));
        urlBuilder.append("&" + Constants.SHOW_UNUSED_SEEDS_PARAM + "=" + Boolean.toString(showUnusedSeedsParam));
        return urlBuilder.toString();
    }

    /**
     * Creates a url based on the supplied request where all the parameters are the same, except the
     * <code>ShowUnusedSeedLists</code> boolean, which is flipped.
     *
     * @param request The original 'create domain' request to based the new url on.
     * @return The new url with the <code>ShowUnusedSeedLists</code> boolean switched.
     */
    public static String createDomainUrlWithFlippedShowSeeds(ServletRequest request) {
        boolean showUnusedConfigurationsParam = Boolean.parseBoolean(request
                .getParameter(Constants.SHOW_UNUSED_CONFIGURATIONS_PARAM));
        boolean showUnusedSeedsParam = Boolean.parseBoolean(request.getParameter(Constants.SHOW_UNUSED_SEEDS_PARAM));
        StringBuilder urlBuilder = new StringBuilder("/HarvestDefinition/Definitions-edit-domain.jsp?");
        urlBuilder
                .append(Constants.DOMAIN_PARAM + "=" + HTMLUtils.encode(request.getParameter(Constants.DOMAIN_PARAM)));
        urlBuilder.append("&" + Constants.SHOW_UNUSED_CONFIGURATIONS_PARAM + "="
                + Boolean.toString(showUnusedConfigurationsParam));
        urlBuilder.append("&" + Constants.SHOW_UNUSED_SEEDS_PARAM + "=" + Boolean.toString(!showUnusedSeedsParam));
        return urlBuilder.toString();
    }

    /**
     * Search for domains matching the following criteria. 
     * TODO Should we allow more than one criteria?
     * TODO use Enum instead for searchType
     *
     * @param context the context of the JSP page calling
     * @param i18n The translation properties file used
     * @param searchQuery The given searchQuery for searching for among the domains.
     * @param searchType The given searchCriteria 
     * @return the set of domain-names matching the given criteria.
     */
    public static List<String> getDomains(PageContext context, I18n i18n, String searchQuery, String searchType) {
        List<String> resultSet = new ArrayList<String>();
        ArgumentNotValid.checkNotNullOrEmpty(searchQuery, "String searchQuery");
        ArgumentNotValid.checkNotNullOrEmpty(searchType, "String searchType");

        try {
            DomainSearchType.parse(searchType);
        } catch (ArgumentNotValid e) {
            HTMLUtils.forwardWithErrorMessage(context, i18n, "errormsg;invalid.domain.search.criteria.0", searchType);
            throw new ForwardedToErrorPage("Unknown domain search criteria '" + searchType + "'");
        }

        log.debug("SearchQuery '" + searchQuery + "', searchType: " + searchType);
        resultSet = DomainDAO.getInstance().getDomains(searchQuery, searchType);
        return resultSet;
    }

    /**
     * Returns the list of domain configurations which are either used in a concrete harvest or is a 'default
     * configuration'.
     * <p>
     * The list is sorted alphabetically by name according to the supplied locale.
     *
     * @param domain The domain to find the used configurations for.
     * @param locale The locale to base the sorting on
     * @return A sorted list of used configurations for the supplied domain.
     */
    public static List<DomainConfiguration> getUsedConfiguration(Domain domain, Locale locale) {
        List<Long> usedConfigurationIDs = DomainDAO.getInstance().findUsedConfigurations(domain.getID());
        List<DomainConfiguration> usedConfigurations = new LinkedList<DomainConfiguration>();

        for (DomainConfiguration configuration : domain.getAllConfigurationsAsSortedList(locale)) {
            if (usedConfigurationIDs.contains(new Long(configuration.getID()))
                    || configuration.getID() == domain.getDefaultConfiguration().getID()) {
                usedConfigurations.add(configuration);
            }
        }

        NamedUtils.sortNamedObjectList(locale, usedConfigurations);
        return usedConfigurations;
    }

    /**
     * Returns the seed lists associated with the supplied configurations.
     *
     * @param configurations The configurations to find seed lists for
     * @return The seed lists used in the supplied configurations.
     */
    public static List<SeedList> getSeedLists(List<DomainConfiguration> configurations) {
        List<SeedList> seedsLists = new LinkedList<SeedList>();
        for (DomainConfiguration configuration : configurations) {
            Iterator<SeedList> seedListIterator = configuration.getSeedLists();
            while (seedListIterator.hasNext()) {
                SeedList seedList = seedListIterator.next();
                if (!seedsLists.contains(seedList)) {
                    seedsLists.add(seedList);
                }
            }
        }

        return seedsLists;
    }
}