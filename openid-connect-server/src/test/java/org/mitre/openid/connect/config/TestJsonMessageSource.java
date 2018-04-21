package org.mitre.openid.connect.config;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestJsonMessageSource {

	private JsonMessageSource jsonMessageSource;

	private Locale localeThatHasAFile = new Locale("en");

	private Locale localeThatDoesNotHaveAFile = new Locale("xx");

	@Before
	public void setup() {
		ConfigurationPropertiesBean config = new ConfigurationPropertiesBean();
		jsonMessageSource = new JsonMessageSource(config);

		//test message files are located in test/resources/js/locale/
		Resource resource = new ClassPathResource("/resources/js/locale/");
		jsonMessageSource.setBaseDirectory(resource);
	}

	@Test
	public void verifyWhenLocaleExists_languageMapIsLoaded() {
		List<JsonObject> languageMap = jsonMessageSource.getLanguageMap(localeThatHasAFile);
		assertNotNull(languageMap);
	}

	@Test
	public void verifyWhenLocaleDoesNotExist_languageMapIsNotLoaded() {
		List<JsonObject> languageMap = jsonMessageSource.getLanguageMap(localeThatDoesNotHaveAFile);
		assertNull(languageMap);
	}

	@Test
	public void verifyWhenLocaleExists_canResolveCode() {
		MessageFormat mf = jsonMessageSource.resolveCode("testAttribute", localeThatHasAFile);
		assertEquals(mf.getLocale().getLanguage(), "en");
		assertEquals(mf.toPattern(), "testValue");
	}

	@Test
	public void verifyWhenLocaleDoesNotExist_cannotResolveCode() {
		MessageFormat mf = jsonMessageSource.resolveCode("test", localeThatDoesNotHaveAFile);
		assertNull(mf);
	}
}
