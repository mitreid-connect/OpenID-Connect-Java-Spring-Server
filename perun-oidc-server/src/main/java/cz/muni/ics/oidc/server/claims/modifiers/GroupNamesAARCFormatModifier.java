package cz.muni.ics.oidc.server.claims.modifiers;

import com.google.common.net.UrlEscapers;
import cz.muni.ics.oidc.server.claims.ClaimModifier;
import cz.muni.ics.oidc.server.claims.ClaimModifierInitContext;
import cz.muni.ics.oidc.server.claims.ClaimUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupName to AARC Format modifier. Converts groupName values to AARC format.
 * Construction: prefix:URL_ENCODED_VALUE#authority
 * Example: urn:geant:cesnet.cz:group:some%20value#perun.cesnet.cz
 *
 * Configuration (replace [claimName] with the name of the claim and [modifierName] with the name of modifier):
 * <ul>
 *     <li><b>custom.claim.[claimName].modifier.[modifierName].prefix</b> - string to be prepended to the value,
 *         defaults to <i>urn:geant:cesnet.cz:group:</i>
 *     </li>
 *     <li><b>custom.claim.[claimName].modifier.[modifierName].authority</b> - string to be appended to the value,
 *         represents authority who has released the value, defaults to <i>perun.cesnet.cz</i>
 *     </li>
 * </ul>
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@SuppressWarnings("unused")
@Slf4j
public class GroupNamesAARCFormatModifier extends ClaimModifier {

	public static final String PREFIX = "prefix";
	public static final String AUTHORITY = "authority";

	private final String prefix;
	private final String authority;

	public GroupNamesAARCFormatModifier(ClaimModifierInitContext ctx) {
		super(ctx);
		this.prefix = ClaimUtils.fillStringPropertyOrNoVal(PREFIX, ctx);
		if (!ClaimUtils.isPropSet(this.prefix)) {
			throw new IllegalArgumentException(getUnifiedName() + " - missing mandatory configuration option: " + PREFIX);
		}
		this.authority = ClaimUtils.fillStringPropertyOrNoVal(AUTHORITY, ctx);
		if (!ClaimUtils.isPropSet(this.authority)) {
			throw new IllegalArgumentException(getUnifiedName() + " - missing mandatory configuration option: " + AUTHORITY);
		}
		log.debug("{}:{}(modifier) - prefix: '{}', authority: '{}'", getClaimName(), getModifierName(), prefix, authority);
	}

	@Override
	public String modify(String value) {
		String modified = prefix + UrlEscapers.urlPathSegmentEscaper().escape(value) + "#" + authority;
		log.trace("{} - modifying value '{}' to AARC format", getUnifiedName(), value);
		log.trace("{} - new value: '{}", getUnifiedName(), modified);
		return modified;
	}

	@Override
	public String toString() {
		return getUnifiedName() +  " - GroupNamesAARCFormatModifier to " + prefix + "<GROUP>#" + authority;
	}

}
