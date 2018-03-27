package cz.muni.ics.oidc;

import com.google.gson.JsonObject;
import org.mitre.openid.connect.model.DefaultUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements UserInfo by inheriting from DefaultUserInfo and adding more claims.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunUserInfo extends DefaultUserInfo {

	private final static Logger log = LoggerFactory.getLogger(PerunUserInfo.class);

	private Map<String, String> customClaims = new LinkedHashMap<>();

	Map<String, String> getCustomClaims() {
		return customClaims;
	}

	private JsonObject obj;

	@Override
	public JsonObject toJson() {
		if(obj==null) {
			//delegate standard claims to DefaultUserInfo
			obj = super.toJson();
			//add custom claims
			for (Map.Entry<String, String> entry : customClaims.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (value == null) {
					obj.addProperty(key, (String) null);
					log.debug("adding null claim {}=null", key);
				} else {
					obj.addProperty(key, value);
					log.debug("adding string claim {}={}", key, value);
				}
			}
		} else {
			log.debug("already rendered to JSON");
		}
		return obj;
	}

}
