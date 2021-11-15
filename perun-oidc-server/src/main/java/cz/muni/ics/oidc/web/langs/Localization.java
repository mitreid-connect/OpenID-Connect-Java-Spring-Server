package cz.muni.ics.oidc.web.langs;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.SameLen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utility class for Language Bar displayed on custom pages.
 *
 * It contains mapping with language keys to language displayed names.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class Localization {

	private Map<String, String> localizationEntries;
	private Map<String, Properties> localizationFiles;
	private final String localizationFilesPath;
	private final List<String> enabledLanguages;

	public Localization(PerunOidcConfig perunOidcConfig) {
		this.enabledLanguages = perunOidcConfig.getAvailableLangs();
		this.localizationFilesPath = perunOidcConfig.getLocalizationFilesPath();
		this.initEntriesAndFiles();
	}

	public Map<String, String> getLocalizationEntries() {
		return localizationEntries;
	}

	public Map<String, Properties> getLocalizationFiles() {
		return localizationFiles;
	}

	public List<String> getEnabledLanguages() {
		return enabledLanguages;
	}

	/**
	 * Get mapping for the languages available
	 * @return Map with key = language code, value = language displayed text
	 */
	public Map<String, String> getEntriesAvailable() {
		Map<String, String> result = new HashMap<>();

		for (String key: enabledLanguages) {
			String lower = key.toLowerCase();
			if (localizationEntries.containsKey(lower)) {
				result.put(lower, localizationEntries.get(lower));
			}
		}

		return result;
	}

	private void initEntriesAndFiles() {
		localizationEntries = new HashMap<>();
		localizationEntries.put("en", "English");
		localizationEntries.put("cs", "Čeština");
		localizationEntries.put("sk", "Slovenčina");

		localizationFiles = new HashMap<>();
		for (String lang: enabledLanguages) {
			lang = lang.toLowerCase();
			if (! localizationEntries.containsKey(lang)) {
				continue;
			}

			Properties langProps = new Properties();
			String resourceFileName = "localization/" + lang + ".properties";
			try (InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(
					getClass().getClassLoader().getResourceAsStream(resourceFileName)), StandardCharsets.UTF_8)) {
				langProps.load(isr);
				log.debug("Loaded localization file: {}", resourceFileName);
				localizationFiles.put(lang, langProps);
			} catch (IOException e) {
				log.warn("Exception caught when reading {}", resourceFileName, e);
			}

			String customFileName = localizationFilesPath + '/' +lang + ".properties";
			try (InputStreamReader isr = new InputStreamReader(
					new FileInputStream(customFileName), StandardCharsets.UTF_8
			)) {
				langProps.load(isr);
				log.debug("Loaded localization file: {}", customFileName);
			} catch (FileNotFoundException e) {
				log.warn("File: {} not found", customFileName, e);
			} catch (IOException e) {
				log.warn("Exception caught when reading {}", customFileName, e);
			}
		}
	}
}
