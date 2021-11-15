package cz.muni.ics.oidc.server.claims.modifiers;

import cz.muni.ics.oidc.server.claims.ClaimModifier;
import cz.muni.ics.oidc.server.claims.ClaimModifierInitContext;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replace regex modifier. Replaces parts matched by regex with string using backreferences to groups.
 *
 * Configuration (replace [claimName] with the name of the claim and [modifierName] with the name of modifier):
 * <ul>
 *     <li><b>custom.claim.[claimName].modifier.[modifierName].find</b> - string to be replaced, can be a regex</li>
 *     <li><b>custom.claim.[claimName].modifier.[modifierName].replace</b> - string to be used as replacement</li>
 * </ul>
 *
 * @see java.util.regex.Matcher#replaceAll(String)
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
@Slf4j
public class RegexReplaceModifier extends ClaimModifier {

	private static final String FIND = "find";
	private static final String REPLACE = "replace";

	private final Pattern regex;
	private final String replacement;

	public RegexReplaceModifier(ClaimModifierInitContext ctx) {
		super(ctx);
		regex = Pattern.compile(ctx.getProperty(FIND, ""));
		replacement = ctx.getProperty(REPLACE, "");
		log.debug("{}(modifier) - regex: '{}', replacement: '{}'", getUnifiedName(), regex, replacement);
	}

	@Override
	public String modify(String value) {
		String modified = regex.matcher(value).replaceAll(replacement);
		log.trace("{} - modifying value '{}' by replacing matched part ('{}') with: '{}'", getUnifiedName(),
				value, regex, replacement);
		log.trace("{} - new value: '{}", getUnifiedName(), modified);
		return modified;
	}

	@Override
	public String toString() {
		return getUnifiedName() + " - RegexReplaceModifier replacing '" + regex.pattern()
				+ "' with '" + replacement + '\'';
	}
}
