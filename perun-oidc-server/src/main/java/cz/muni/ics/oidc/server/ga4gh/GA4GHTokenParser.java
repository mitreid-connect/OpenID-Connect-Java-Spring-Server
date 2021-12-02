package cz.muni.ics.oidc.server.ga4gh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a command-line debugging tool. It parses JSON in GA4GH Passport format,
 * verifies signatures on Passport Visas (JWT tokens), and prints them in human-readable format.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
public class GA4GHTokenParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final List<Ga4ghClaimRepository> CLAIM_REPOSITORIES = new ArrayList<>();
	private static final Map<URI, RemoteJWKSet<SecurityContext>> REMOTE_JWK_SETS = new HashMap<>();
	private static final Map<URI, String> SIGNERS = new HashMap<>();

	public static void main(String[] args) throws IOException, ParseException, JOSEException {
		Ga4ghUtils.parseConfigFile("ga4gh_config.yml", CLAIM_REPOSITORIES, REMOTE_JWK_SETS, SIGNERS);
		String userinfo = "/tmp/ga4gh.json";
		JsonNode doc = MAPPER.readValue(new File(userinfo), JsonNode.class);
		JsonNode ga4gh = doc.get(Ga4ghPassportAndVisaClaimSource.GA4GH_CLAIM);
		long startx = System.currentTimeMillis();
		System.out.println();
		for (JsonNode jwtString : ga4gh) {
			String s = jwtString.asText();
			Ga4ghPassportVisa visa = Ga4ghUtils.parseAndVerifyVisa(s, SIGNERS, REMOTE_JWK_SETS, MAPPER);
			if(!visa.isVerified()) {
				System.out.println("visa not verified: " + s);
				System.out.println("visa = " + visa.getPrettyString());
			} else {
				System.out.println("OK: " + visa.getPrettyString());
			}
			SignedJWT jwt = (SignedJWT) JWTParser.parse(s);
			ObjectWriter prettyPrinter = MAPPER.writerWithDefaultPrettyPrinter();

			JsonNode visaHeader = MAPPER.readValue(jwt.getHeader().toString(), JsonNode.class);
			System.out.println(prettyPrinter.writeValueAsString(visaHeader));

			JsonNode visaPayload = MAPPER.readValue(jwt.getPayload().toString(), JsonNode.class);
			System.out.println(prettyPrinter.writeValueAsString(visaPayload));
		}
		long endx = System.currentTimeMillis();
		System.out.println("signature verification time: " + (endx - startx));
	}

}
