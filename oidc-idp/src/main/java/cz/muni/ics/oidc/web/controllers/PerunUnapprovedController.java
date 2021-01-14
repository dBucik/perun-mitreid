package cz.muni.ics.oidc.web.controllers;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.langs.Localization;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.openid.connect.view.HttpCodeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Properties;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_CLIENT_ID;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_HEADER;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_MESSAGE;
import static cz.muni.ics.oidc.web.controllers.ControllerUtils.LANG_PROPS_KEY;

/**
 * Cotnroller for the unapproved page.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Controller
public class PerunUnapprovedController {

    private final static Logger log = LoggerFactory.getLogger(PerunUnapprovedController.class);

    public static final String UNAPPROVED_MAPPING = "/unapproved";
    public static final String UNAPPROVED_SPECIFIC_MAPPING = "/unapproved_spec";

    public static final String OUT_HEADER = "outHeader";
    public static final String OUT_MESSAGE = "outMessage";
    public static final String OUT_CONTACT_P = "outContactP";

    public static final String CONTACT_LANG_PROP_KEY = "contact_p";
    public static final String CONTACT_MAIL = "contactMail";

    private final ClientDetailsEntityService clientService;
    private final PerunOidcConfig perunOidcConfig;
    private final Localization localization;
    private final WebHtmlClasses htmlClasses;

    @Autowired
    public PerunUnapprovedController(ClientDetailsEntityService clientService, PerunOidcConfig perunOidcConfig,
                                     Localization localization, WebHtmlClasses htmlClasses)
    {
        this.clientService = clientService;
        this.perunOidcConfig = perunOidcConfig;
        this.localization = localization;
        this.htmlClasses = htmlClasses;
    }

    @GetMapping(value = UNAPPROVED_MAPPING)
    public String showUnapproved(ServletRequest req, Map<String, Object> model,
                                 @RequestParam(PARAM_CLIENT_ID) String clientId) {
        HttpServletRequest request = (HttpServletRequest) req;
        ClientDetailsEntity client;

        try {
            client = clientService.loadClientByClientId(clientId);
        } catch (OAuth2Exception e) {
            log.error("showUnapproved: OAuth2Exception was thrown when attempting to load client", e);
            model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
            return HttpCodeView.VIEWNAME;
        } catch (IllegalArgumentException e) {
            log.error("showUnapproved: IllegalArgumentException was thrown when attempting to load client", e);
            model.put(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
            return HttpCodeView.VIEWNAME;
        }

        if (client == null) {
            log.error("showUnapproved: could not find client " + clientId);
            model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
            return HttpCodeView.VIEWNAME;
        }

        ControllerUtils.setPageOptions(model, request, localization, htmlClasses, perunOidcConfig);
        model.put("client", client);

        return "unapproved";
    }

    @GetMapping(value = UNAPPROVED_SPECIFIC_MAPPING)
    public String showUnapprovedSpec(ServletRequest req, Map<String, Object> model,
                                     @RequestParam(value = PARAM_HEADER, required = false) String header,
                                     @RequestParam(value = PARAM_MESSAGE, required = false) String message) {

        ControllerUtils.setPageOptions(model, (HttpServletRequest) req, localization, htmlClasses, perunOidcConfig);

        String headerText = getText(model, header);
        String messageText = getText(model, message);
        String contactPText = getText(model, CONTACT_LANG_PROP_KEY);

        model.put(OUT_HEADER, headerText);
        model.put(OUT_MESSAGE, messageText);
        model.put(OUT_CONTACT_P, contactPText);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        return "unapproved_spec";
    }

    private String getText(Map<String, Object> model, String key) {
        Properties langProps = (Properties) model.get(LANG_PROPS_KEY);
        return langProps.getProperty(key);
    }

}
