package cz.muni.ics.oidc.web.controllers;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.langs.Localization;
import org.mitre.openid.connect.view.HttpCodeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_ACCEPTED;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_REASON;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_TARGET;
import static cz.muni.ics.oidc.web.controllers.PerunUnapprovedController.CONTACT_LANG_PROP_KEY;
import static cz.muni.ics.oidc.web.controllers.PerunUnapprovedController.CONTACT_MAIL;
import static cz.muni.ics.oidc.web.controllers.PerunUnapprovedController.OUT_CONTACT_P;
import static cz.muni.ics.oidc.web.controllers.PerunUnapprovedController.OUT_HEADER;
import static cz.muni.ics.oidc.web.controllers.PerunUnapprovedController.OUT_MESSAGE;

/**
 * Controller for IS CESNET ELIGIBLE pages.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Controller
public class IsCesnetEligibleController {

    private static final Logger log = LoggerFactory.getLogger(IsCesnetEligibleController.class);

    private static final String NOT_SET_HDR = "403_isCesnetEligible_notSet_hdr";
    private static final String NOT_SET_MSG = "403_isCesnetEligible_notSet_msg";
    private static final String EXPIRED_HDR = "403_isCesnetEligible_expired_hdr";
    private static final String EXPIRED_MSG = "403_isCesnetEligible_expired_msg";

    private static final String TARGET_URL_PLACEHOLDER = "%%TARGET%%";

    public static final String REASON_NOT_SET = "notSet";
    public static final String REASON_EXPIRED = "expired";
    public static final String REASON_WARNING = "warning";

    public static final String IS_CESNET_ELIGIBLE_APPROVED_SESS = "isCesnetEligibleApprovedSession";
    public static final String PREFIX = "/isCesnetEligible";
    public static final String WARNING_MAPPING = PREFIX + "/warning";
    public static final String UNAPPROVED_MAPPING = PREFIX + "/unapproved";

    private static final String TARGET = "target";
    private static final String ACTION = "action";

    private final Localization localization;
    private final WebHtmlClasses htmlClasses;
    private final PerunOidcConfig perunOidcConfig;

    @Autowired
    public IsCesnetEligibleController(Localization localization,
                                      WebHtmlClasses htmlClasses,
                                      PerunOidcConfig perunOidcConfig)
    {
        this.localization = localization;
        this.htmlClasses = htmlClasses;
        this.perunOidcConfig = perunOidcConfig;
    }

    @GetMapping(value = WARNING_MAPPING, params = PARAM_TARGET)
    public String warningValueOld(HttpServletRequest req,
                                  Map<String, Object> model,
                                  @RequestParam(PARAM_TARGET) String returnUrl)
    {
        log.debug("Display warning page for isCesnetEligible");
        model.put(TARGET, returnUrl);
        model.put(ACTION, req.getRequestURL().toString());
        ControllerUtils.setPageOptions(model, req, localization, htmlClasses, perunOidcConfig);
        return "isCesnetEligibleWarning";
    }

    @GetMapping(value = WARNING_MAPPING, params = {PARAM_TARGET, PARAM_ACCEPTED})
    public String warningApproved(HttpServletRequest request,
                                  @RequestParam(PARAM_TARGET) String target,
                                  @RequestParam(PARAM_ACCEPTED) boolean accepted)
    {
        log.debug("Warning approved, set session attribute and redirect to {}", target);
        if (accepted) {
            HttpSession sess = request.getSession();
            sess.setAttribute(IS_CESNET_ELIGIBLE_APPROVED_SESS, true);
        }
        return "redirect:" + target;
    }

    @GetMapping(value = UNAPPROVED_MAPPING, params = {PARAM_TARGET, PARAM_REASON})
    public String showUnapprovedIsCesnetEligible(ServletRequest req, Map<String, Object> model,
                                                 @RequestParam(value = PARAM_TARGET) String target,
                                                 @RequestParam(value = PARAM_REASON) String reason) {

        ControllerUtils.setPageOptions(model, (HttpServletRequest) req, localization, htmlClasses, perunOidcConfig);

        String header;
        String message;
        String contactPText = ControllerUtils.getLangPropKey(model, CONTACT_LANG_PROP_KEY);

        if (REASON_EXPIRED.equals(reason)) {
            header = ControllerUtils.getLangPropKey(model, EXPIRED_HDR);
            message = ControllerUtils.getLangPropKey(model, EXPIRED_MSG);
        } else if (REASON_NOT_SET.equals(reason)){
            header = ControllerUtils.getLangPropKey(model, NOT_SET_HDR);
            message = ControllerUtils.getLangPropKey(model, NOT_SET_MSG);
        } else {
            model.put(HttpCodeView.CODE, HttpStatus.NOT_FOUND);
            return HttpCodeView.VIEWNAME;
        }

        header = ControllerUtils.replace(header, TARGET_URL_PLACEHOLDER, target);
        message = ControllerUtils.replace(message, TARGET_URL_PLACEHOLDER, target);

        model.put(OUT_HEADER, header);
        model.put(OUT_MESSAGE, message);
        model.put(OUT_CONTACT_P, contactPText);
        model.put(CONTACT_MAIL, perunOidcConfig.getEmailContact());

        return "unapproved_spec";
    }

}
