package cz.muni.ics.oidc.server.elixir;

import static cz.muni.ics.oidc.server.elixir.GA4GHClaimSource.parseAndVerifyVisa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is a command-line debugging tool. It parses JSON in GA4GH Passport format,
 * verifies signatures on Passport Visas (JWT tokens), and prints them in human-readable format.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
public class GA4GHTokenParser {

	static ObjectMapper jsonMapper = new ObjectMapper();

	public static void main(String[] args) throws IOException, ParseException, JOSEException {
		GA4GHClaimSource.parseConfigFile("ga4gh_config.yml");
		String userinfo = "/tmp/ga4gh.json";
		JsonNode doc = jsonMapper.readValue(new File(userinfo), JsonNode.class);
		JsonNode ga4gh = doc.get("ga4gh_passport_v1");
		long startx = System.currentTimeMillis();
		System.out.println();
		for (JsonNode jwtString : ga4gh) {
			String s = jwtString.asText();
			GA4GHClaimSource.PassportVisa visa = parseAndVerifyVisa(s);
			if(!visa.isVerified()) {
				System.out.println("visa not verified: "+s);
				System.out.println("visa = " + visa.getPrettyString());

//				System.exit(1);
			} else {
				System.out.println("OK: "+visa.getPrettyString());
			}
			SignedJWT jwt = (SignedJWT) JWTParser.parse(s);
			ObjectWriter prettyPrinter = jsonMapper.writerWithDefaultPrettyPrinter();

			JsonNode visaHeader = jsonMapper.readValue(jwt.getHeader().toString(), JsonNode.class);
			System.out.println(prettyPrinter.writeValueAsString(visaHeader));

			JsonNode visaPayload = jsonMapper.readValue(jwt.getPayload().toString(), JsonNode.class);
			System.out.println(prettyPrinter.writeValueAsString(visaPayload));
		}
		long endx = System.currentTimeMillis();
		System.out.println("signature verification time: " + (endx - startx));

	}

	private static String isoDateTime(long linuxTime) {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochSecond(linuxTime), ZoneId.systemDefault()));
	}
}
