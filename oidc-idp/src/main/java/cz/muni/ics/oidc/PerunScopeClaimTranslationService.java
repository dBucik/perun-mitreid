package cz.muni.ics.oidc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.mitre.openid.connect.service.ScopeClaimTranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Translates scopes to claims. A single scope can provide access to multiple claims.
 * Set this as spring bean named "scopeClaimTranslator". This code is copied from class
 * org.mitre.openid.connect.service.impl.DefaultScopeClaimTranslationService
 * which for some reason is not accessible in this project, and extended.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
//@Service("scopeClaimTranslator")
public class PerunScopeClaimTranslationService implements ScopeClaimTranslationService {

	private final static Logger log = LoggerFactory.getLogger(PerunScopeClaimTranslationService.class);

	private SetMultimap<String, String> scopesToClaims = HashMultimap.create();

	public void setPerunUserInfoRepository(PerunUserInfoRepository perunUserInfoRepository) {
		for(PerunCustomClaimDefinition pccd : perunUserInfoRepository.getCustomClaims()) {
			log.info("adding custom claim \"{}\" in scope \"{}\" ",pccd.getClaim(),pccd.getScope());
			scopesToClaims.put(pccd.getScope(),pccd.getClaim());
		}
	}

	/**
	 * Default constructor; initializes scopesToClaims map
	 */
	public PerunScopeClaimTranslationService() {
		log.trace("initialized");
		scopesToClaims.put("openid", "sub");

		scopesToClaims.put("profile", "name");
		scopesToClaims.put("profile", "preferred_username");
		scopesToClaims.put("profile", "given_name");
		scopesToClaims.put("profile", "family_name");
		scopesToClaims.put("profile", "middle_name");
		scopesToClaims.put("profile", "nickname");
		scopesToClaims.put("profile", "profile");
		scopesToClaims.put("profile", "picture");
		scopesToClaims.put("profile", "website");
		scopesToClaims.put("profile", "gender");
		scopesToClaims.put("profile", "zoneinfo");
		scopesToClaims.put("profile", "locale");
		scopesToClaims.put("profile", "updated_at");
		scopesToClaims.put("profile", "birthdate");

		scopesToClaims.put("email", "email");
		scopesToClaims.put("email", "email_verified");

		scopesToClaims.put("phone", "phone_number");
		scopesToClaims.put("phone", "phone_number_verified");

		scopesToClaims.put("address", "address");
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.ScopeClaimTranslationService#getClaimsForScope(java.lang.String)
	 */
	@Override
	public Set<String> getClaimsForScope(String scope) {
		log.trace("getClaimsForScope({})",scope);
		if (scopesToClaims.containsKey(scope)) {
			return scopesToClaims.get(scope);
		} else {
			return new HashSet<>();
		}
	}

	/* (non-Javadoc)
	 * @see org.mitre.openid.connect.service.ScopeClaimTranslationService#getClaimsForScopeSet(java.util.Set)
	 */
	@Override
	public Set<String> getClaimsForScopeSet(Set<String> scopes) {
		Set<String> result = new HashSet<>();
		for (String scope : scopes) {
			result.addAll(getClaimsForScope(scope));
		}
		return result;
	}
}
