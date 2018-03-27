package cz.muni.ics.oidc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerunConnector {

    private final static Logger log = LoggerFactory.getLogger(PerunConnector.class);

    private String ldapUrl;
    private String ldapBase;

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public void setLdapBase(String ldapBase) {
        this.ldapBase = ldapBase;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public String getLdapBase() {
        return ldapBase;
    }
}
