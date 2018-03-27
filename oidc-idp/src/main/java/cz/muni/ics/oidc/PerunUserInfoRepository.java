package cz.muni.ics.oidc;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.mitre.openid.connect.model.Address;
import org.mitre.openid.connect.model.DefaultAddress;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.repository.UserInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;

import javax.naming.NamingException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Provides data about a user.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunUserInfoRepository implements UserInfoRepository {

	private final static Logger log = LoggerFactory.getLogger(PerunUserInfoRepository.class);

	private PerunConnector perunConnector;
	private Properties properties;
	private LdapTemplate template;

	public void setPerunConnector(PerunConnector perunConnector) {
		this.perunConnector = perunConnector;
	}

	private String subAttribute;
	private String preferredUsernameAttribute;
	private String nameAttribute;
	private String givenNameAttribute;
	private String familyNameAttribute;
	private String emailAttribute;
	private String addressAttribute;
	private String phoneAttribute;
	private String zoneinfoAttribute;
	private String localeAttribute;
	private String extSourceAttribute;
	private String extLoginAttribute;
	private List<PerunCustomClaimDefinition> customClaims = new ArrayList<>();

	public void setSubAttribute(String subAttribute) {
		this.subAttribute = subAttribute;
	}

	public void setPreferredUsernameAttribute(String preferredUsernameAttribute) {
		this.preferredUsernameAttribute = preferredUsernameAttribute;
	}

	public void setTemplate(LdapTemplate template) {
		this.template = template;
	}

	public void setNameAttribute(String nameAttribute) {
		this.nameAttribute = nameAttribute;
	}

	public void setGivenNameAttribute(String givenNameAttribute) {
		this.givenNameAttribute = givenNameAttribute;
	}

	public void setFamilyNameAttribute(String familyNameAttribute) {
		this.familyNameAttribute = familyNameAttribute;
	}

	public void setExtSourceAttribute(String extSourceAttribute) {
		this.extSourceAttribute = extSourceAttribute;
	}

	public void setExtLoginAttribute(String extLoginAttribute) {
		this.extLoginAttribute = extLoginAttribute;
	}

	public void setEmailAttribute(String emailAttribute) {
		this.emailAttribute = emailAttribute;
	}

	public void setAddressAttribute(String addressAttribute) {
		this.addressAttribute = addressAttribute;
	}

	public void setPhoneAttribute(String phoneAttribute) {
		this.phoneAttribute = phoneAttribute;
	}

	public void setZoneinfoAttribute(String zoneinfoAttribute) {
		this.zoneinfoAttribute = zoneinfoAttribute;
	}

	public void setLocaleAttribute(String localeAttribute) {
		this.localeAttribute = localeAttribute;
	}

	public void setCustomClaimNames(List<String> customClaimNames) {
		//PerunCustomClaimDefinition
		this.customClaims = new ArrayList<>(customClaimNames.size());
		for (String claim : customClaimNames) {
			String scopeProperty = "custom.claim." + claim + ".scope";
			String scope = properties.getProperty(scopeProperty);
			if (scope == null) {
				log.error("property {} not found, skipping custom claim {}", scopeProperty, claim);
				continue;
			}
			String attributeProperty = "custom.claim." + claim + ".attribute";
			String perunAttribute = properties.getProperty(attributeProperty);
			if (perunAttribute == null) {
				log.error("property {} not found, skipping custom claim {}", attributeProperty, claim);
				continue;
			}
			customClaims.add(new PerunCustomClaimDefinition(scope, claim, perunAttribute));
		}
	}

	List<PerunCustomClaimDefinition> getCustomClaims() {
		return customClaims;
	}

	@Override
	public UserInfo getByUsername(String username) {
		log.trace("getByUsername({})", username);
		try {
			return cache.get(username);
		} catch (UncheckedExecutionException | ExecutionException e) {
			log.error("cannot get user from cache", e);
			return null;
		}
	}

	@Override
	public UserInfo getByEmailAddress(String email) {
		log.trace("getByEmailAddress({})", email);
		throw new RuntimeException("PerunUserInfoRepository.getByEmailAddress() not implemented");
	}

	public PerunUserInfoRepository() {
		this.cache = CacheBuilder.newBuilder()
				.maximumSize(100)
				.expireAfterAccess(30, TimeUnit.SECONDS)
				.build(cacheLoader);
	}

	private LoadingCache<String, UserInfo> cache;

	@SuppressWarnings("FieldCanBeLocal")
	private CacheLoader<String, UserInfo> cacheLoader = new CacheLoader<String, UserInfo>() {
		@Override
		public UserInfo load(String username) throws Exception {
			log.trace("load({})", username);
			PerunUserInfo ui = getUserById(username);
			log.trace("user loaded");
			return ui;
		}
	};

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public UserInfo getUserByPrincipal(PerunPrincipal perunPrincipal) {
		Filter objClassFilter = new EqualsFilter("objectclass", "perunUser");
		Filter extSource = new EqualsFilter(extSourceAttribute, perunPrincipal.getExtSourceName());
		Filter extLogin = new EqualsFilter(extLoginAttribute, perunPrincipal.getExtLogin());
		AndFilter filter = new AndFilter();
		filter.and(objClassFilter).and(extLogin).and(extSource);

		List<PerunUserInfo> res = template.search(
			perunConnector.getLdapBase(), filter.encode(), new PerunUserContextMapper());

		if (res.size() == 1) {
			return res.get(0);
		} else {
			throw new IllegalArgumentException("User not found: " + perunPrincipal);
		}
	}

	public PerunUserInfo getUserById(String userId) {
		Filter objClassFilter = new EqualsFilter("objectclass", "perunUser");
		Filter subFilter = new EqualsFilter(subAttribute, userId);
		AndFilter andFilter = new AndFilter();
		andFilter.and(objClassFilter).and(subFilter);
		List<PerunUserInfo> res = template.search(
				perunConnector.getLdapBase(), andFilter.encode(), new PerunUserContextMapper());

		if (res.size() == 1) {
			return res.get(0);
		} else {
			throw new IllegalArgumentException("User not found: " + userId);
		}
	}

	public class PerunUserContextMapper implements ContextMapper<PerunUserInfo> {
		@Override
		public PerunUserInfo mapFromContext(Object ctx) throws NamingException {
			DirContextAdapter context = (DirContextAdapter) ctx;

			if (context.getStringAttribute(subAttribute) == null) {
				return null;
			}

			PerunUserInfo ui = new PerunUserInfo();
			ui.setSub(context.getStringAttribute(subAttribute));
			ui.setName(context.getStringAttribute(nameAttribute));
			ui.setPreferredUsername(context.getStringAttribute(preferredUsernameAttribute));
			ui.setFamilyName(context.getStringAttribute(familyNameAttribute));
			ui.setGivenName(context.getStringAttribute(givenNameAttribute));
			ui.setEmail(context.getStringAttribute(emailAttribute));
			ui.setPhoneNumber(context.getStringAttribute(phoneAttribute));
			ui.setZoneinfo(context.getStringAttribute(zoneinfoAttribute));
			ui.setLocale(context.getStringAttribute(localeAttribute));
			Address address = new DefaultAddress();
			address.setFormatted(context.getStringAttribute(addressAttribute));
			ui.setAddress(address);
			for (PerunCustomClaimDefinition pccd : customClaims) {
				String[] vals = context.getStringAttributes(pccd.getPerunAttributeName());
				String val = Arrays.toString(vals);
				val = val.substring(1, val.length() - 1);
				ui.getCustomClaims().put(pccd.getClaim(), val);
			}

			return ui;
		}
	}
}
