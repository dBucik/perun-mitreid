package cz.muni.ics.oidc.server.filters.impl;

import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.server.filters.PerunFilterConstants;
import cz.muni.ics.oidc.server.filters.PerunRequestFilter;
import cz.muni.ics.oidc.server.filters.PerunRequestFilterParams;
import cz.muni.ics.oidc.web.controllers.ControllerUtils;
import cz.muni.ics.oidc.web.controllers.IsCesnetEligibleController;
import cz.muni.ics.oidc.web.controllers.PerunUnapprovedController;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.AUTHORIZE_REQ_PATTERN;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_FORCE_AUTHN;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_REASON;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_SCOPE;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_TARGET;
import static cz.muni.ics.oidc.web.controllers.IsCesnetEligibleController.IS_CESNET_ELIGIBLE_APPROVED_SESS;
import static cz.muni.ics.oidc.web.controllers.IsCesnetEligibleController.UNAPPROVED_MAPPING;

/**
 * This filter verifies that user attribute isCesnetEligible is not older than given time frame.
 * In case the value is older, denies access to the service and forces user to use verified identity.
 * Otherwise, user can to access the service.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * <ul>
 *     <li><b>filter.[name].isCesnetEligibleAttr</b> - mapping to isCesnetEligible attribute</li>
 *     <li><b>filter.[name].validityPeriod</b> - specify in months, how long the value can be old, if no value
 *         or invalid value has been provided, defaults to 12 months</li>
 * </ul>
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class PerunIsCesnetEligibleFilter extends PerunRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PerunIsCesnetEligibleFilter.class);

    /* CONFIGURATION PROPERTIES */
    private static final String IS_CESNET_ELIGIBLE_ATTR_NAME = "isCesnetEligibleAttr";
    private static final String IS_CESNET_ELIGIBLE_SCOPE = "isCesnetEligibleScope";
    private static final String VALIDITY_PERIOD = "validityPeriod";
    private static final String WARNING_PERIOD = "warningPeriod";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final String isCesnetEligibleAttrName;
    private final String isCesnetEligibleScope;
    private final int warningPeriod;
    private final int validityPeriod;
    /* END OF CONFIGURATION PROPERTIES */

    private final PerunAdapter perunAdapter;
    private final String filterName;

    public PerunIsCesnetEligibleFilter(PerunRequestFilterParams params) {
        super(params);
        BeanUtil beanUtil = params.getBeanUtil();
        this.perunAdapter = beanUtil.getBean(PerunAdapter.class);
        this.isCesnetEligibleAttrName = params.getProperty(IS_CESNET_ELIGIBLE_ATTR_NAME);
        this.triggerScope = params.getProperty(IS_CESNET_ELIGIBLE_SCOPE);
        int validityPeriodParam = 12;
        int warninPeriodParam = 3;
        if (params.hasProperty(WARNING_PERIOD)) {
            try {
                warninPeriodParam = Integer.parseInt(params.getProperty(VALIDITY_PERIOD));
            } catch (NumberFormatException ignored) {
                //no problem, we have default value
            }
        }
        if (params.hasProperty(VALIDITY_PERIOD)) {
            try {
                validityPeriodParam = Integer.parseInt(params.getProperty(VALIDITY_PERIOD));
            } catch (NumberFormatException ignored) {
                //no problem, we have default values
            }
        }
        this.warningPeriod = warninPeriodParam;
        this.validityPeriod = validityPeriodParam;
        this.filterName = params.getFilterName();
    }

    @Override
    protected boolean process(ServletRequest req, ServletResponse res, FilterParams params) {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!FiltersUtils.isScopePresent(request.getParameter(PARAM_SCOPE), triggerScope)) {
            log.debug("{} - skip execution: scope '{}' is not present in request", filterName, triggerScope);
            return true;
        } else if (warningApproved(request)) {
            log.debug("Warning already approved, continue to the next filter");
            return true;
        }

        PerunUser user = params.getUser();
        if (user == null || user.getId() == null) {
            log.debug("{} - skip execution: no user provider", filterName);
            return true;
        }

        String reason = IsCesnetEligibleController.REASON_NOT_SET;
        PerunAttributeValue attrValue = perunAdapter.getUserAttributeValue(user.getId(), isCesnetEligibleAttrName);
        if (attrValue != null) {
            LocalDateTime timeStamp;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
                timeStamp = LocalDateTime.parse(attrValue.valueAsString(), formatter);
            } catch (DateTimeParseException e) {
                log.warn("{} - could not parse timestamp from attribute '{}' value: '{}'",
                        filterName, isCesnetEligibleAttrName, attrValue.valueAsString());
                log.debug("{} - skip execution: no timestamp to compare to", filterName);
                log.trace("{} - details:", filterName, e);
                return true;
            }

            LocalDateTime now = LocalDateTime.now();
            if (now.minusMonths(validityPeriod).isAfter(timeStamp)) {
                reason = IsCesnetEligibleController.REASON_EXPIRED;
            } else if (now.minusMonths(warningPeriod).isAfter(timeStamp)) {
                reason = IsCesnetEligibleController.REASON_WARNING;
            } else {
                log.debug("{} valid, go to the next filter", isCesnetEligibleAttrName);
                return true;
            }
        }

        log.debug("{} - attribute '{}' value is invalid, stop user at this point", filterName, attrValue);
        this.redirect(request, response, reason);
        return false;
    }

    private boolean warningApproved(HttpServletRequest req) {
        if (req.getSession() == null) {
            return false;
        }
        boolean approved = false;
        if (req.getSession().getAttribute(IS_CESNET_ELIGIBLE_APPROVED_SESS) != null) {
            approved = (Boolean) req.getSession().getAttribute(IS_CESNET_ELIGIBLE_APPROVED_SESS);
            req.getSession().removeAttribute(IS_CESNET_ELIGIBLE_APPROVED_SESS);
        }
        return approved;
    }

    private void redirect(HttpServletRequest req, HttpServletResponse res, String reason) {
        if (IsCesnetEligibleController.REASON_WARNING.equalsIgnoreCase(reason)) {
            redirectToWarning(req, res);
        } else {
            redirectUnapproved(req, res, reason);
        }
    }

    private void redirectUnapproved(HttpServletRequest req, HttpServletResponse res, String reason) {
        String targetURL = FiltersUtils.buildRequestURL(req, Collections.singletonMap(PARAM_FORCE_AUTHN, "true"));
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TARGET, targetURL);
        params.put(PARAM_REASON, reason);

        String redirectUrl = ControllerUtils.createRedirectUrl(req, PerunFilterConstants.AUTHORIZE_REQ_PATTERN,
                UNAPPROVED_MAPPING, params);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

    private void redirectToWarning(HttpServletRequest req, HttpServletResponse res) {
        String targetURL = FiltersUtils.buildRequestURL(req);

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TARGET, targetURL);
        String redirectUrl = ControllerUtils.createRedirectUrl(req, AUTHORIZE_REQ_PATTERN,
                IsCesnetEligibleController.WARNING_MAPPING, params);
        res.reset();
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

}
