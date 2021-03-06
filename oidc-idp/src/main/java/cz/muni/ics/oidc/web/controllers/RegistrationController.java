package cz.muni.ics.oidc.web.controllers;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.web.WebHtmlClasses;
import cz.muni.ics.oidc.web.langs.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Controller for the unapproved page which offers registration.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Controller
public class RegistrationController {

    private final static Logger log = LoggerFactory.getLogger(RegistrationController.class);

    public static final String PARAM_TARGET = "target";

    public static final String CONTINUE_DIRECT_MAPPING = "/continueDirect";

    @Autowired
    private PerunOidcConfig perunOidcConfig;

    @Autowired
    private Localization localization;

    @Autowired
    private WebHtmlClasses htmlClasses;

    @GetMapping(value = CONTINUE_DIRECT_MAPPING, params = { PARAM_TARGET })
    public String showRegistrationForm(Map<String, Object> model, HttpServletRequest req,
                                       @RequestParam(PARAM_TARGET) String target)
    {
        model.put(PARAM_TARGET, target);
        ControllerUtils.setPageOptions(model, req, localization, htmlClasses, perunOidcConfig);
        return "continue_direct";
    }

}
