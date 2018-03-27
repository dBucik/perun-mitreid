package cz.muni.ics.oidc;

import org.mitre.openid.connect.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.OrFilter;

import javax.naming.NamingException;
import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class PerunConnector {

    private final static Logger log = LoggerFactory.getLogger(PerunConnector.class);

    private String ldapUrl;
    private String ldapBase;
    private LdapTemplate template;

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

    public boolean isUserOnFacility(String spEntityId, String userId) {
        AndFilter andFilter;
        OrFilter orFilter;

        Filter objClassFilter = new EqualsFilter("objectclass", "perunResource");
        Filter resIdFilter = new EqualsFilter("perunResourceId", spEntityId);
        andFilter = new AndFilter();
        andFilter.and(objClassFilter).and(resIdFilter);

        List<String> resources = template.search(ldapBase, andFilter.encode(), new ContextMapper<String>() {
            @Override
            public String mapFromContext(Object o) throws NamingException {
                DirContextAdapter adapter = (DirContextAdapter) o;

                return adapter.getStringAttribute("perunResourceId");
            }
        });

        if (resources == null || resources.isEmpty()) {
            throw new IllegalArgumentException("Service with spEntityId: " + spEntityId
                    + " hasn't assigned any resource.");
        }

        // create OR string of resourceIds
        orFilter = new OrFilter();
        for(String resource: resources) {
            Filter filter = new EqualsFilter("assignedToResourceId", resource);
            orFilter.or(filter);
        }

        Filter userIdFilter = new EqualsFilter("uniqueMember=perunUserId", userId + "ou=People," + ldapBase);
        andFilter = new AndFilter();
        andFilter.and(orFilter).and(userIdFilter);

        List<String> groupsIds = template.search(ldapBase, andFilter.encode(), new ContextMapper<String>() {
            @Override
            public String mapFromContext(Object o) throws NamingException {
                DirContextAdapter adapter = (DirContextAdapter) o;

                return adapter.getStringAttribute("perunGroupId");
            }
        });

        return (groupsIds == null || groupsIds.isEmpty());

    }
}
