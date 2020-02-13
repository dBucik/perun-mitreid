package cz.muni.ics.oidc.server.userInfo;

import cz.muni.ics.oidc.server.connectors.PerunConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Context for UserInfoModifiers.
 *
 * @author Dominik Baránek 0Baranek.dominik0@gmail.com
 */
public class UserInfoModifierContext {

	private static final Logger log = LoggerFactory.getLogger(PerunUserInfoService.class);

	private static final String MODIFIER_CLASS = ".class";

	private Properties properties;
	private PerunConnector perunConnector;
	private List<UserInfoModifier> modifiers;

	public UserInfoModifierContext(Properties properties, PerunConnector perunConnector) {
		this.properties = properties;
		this.perunConnector = perunConnector;
		this.modifiers = new LinkedList<>();

		String modifierNamesProperty = properties.getProperty("userInfo.modifiers");
		String[] modifierNames = modifierNamesProperty.split(",");
		for (String m : modifierNames) {
			UserInfoModifier modifier = loadModifier("userInfo.modifier." + m);
			if (modifier != null) {
				log.trace("Executing modifier {}", m);
				modifiers.add(modifier);
			}
		}

	}

	public PerunUserInfo modify(PerunUserInfo perunUserInfo, String clientId) {
		log.trace("modify({}, {})", perunUserInfo, clientId);

		for (UserInfoModifier m : modifiers) {
			m.modify(perunUserInfo, clientId);
		}

		return perunUserInfo;
	}

	private UserInfoModifier loadModifier(String propertyPrefix) {

		String modifierClass = properties.getProperty(propertyPrefix + MODIFIER_CLASS, null);
		if (modifierClass == null) {
			return null;
		}
		try {
			Class<?> rawClazz = Class.forName(modifierClass);
			if (!UserInfoModifier.class.isAssignableFrom(rawClazz)) {
				log.error("modifier class {} does not extend UserInfoModifier", modifierClass);
				return null;
			}
			@SuppressWarnings("unchecked") Class<UserInfoModifier> clazz = (Class<UserInfoModifier>) rawClazz;
			Constructor<UserInfoModifier> constructor = clazz.getConstructor(UserInfoModifierInitContext.class);
			UserInfoModifierInitContext ctx = new UserInfoModifierInitContext(propertyPrefix, properties, perunConnector);
			UserInfoModifier userInfoModifier = constructor.newInstance(ctx);
			log.info("loaded a modifier '{}' for {}", userInfoModifier, propertyPrefix);
			return userInfoModifier;
		} catch (ClassNotFoundException e) {
			log.error("modifier class {} not found", modifierClass);
			return null;
		} catch (NoSuchMethodException e) {
			log.error("modifier class {} does not have proper constructor", modifierClass);
			return null;
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			log.error("cannot instantiate " + modifierClass, e);
			log.error("modifier class {} cannot be instantiated", modifierClass);
			return null;
		}
	}
}
