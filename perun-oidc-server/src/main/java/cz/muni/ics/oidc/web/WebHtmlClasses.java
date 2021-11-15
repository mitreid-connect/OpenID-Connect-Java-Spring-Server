package cz.muni.ics.oidc.web;

import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * Static utility class for HTML pages. Contains properties that can be rendered as element classes in HTML.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class WebHtmlClasses {

	private String classesFilePath;
	private Properties webHtmlClassesProperties;

	public WebHtmlClasses(PerunOidcConfig perunOidcConfig) {
		this.classesFilePath = perunOidcConfig.getWebClassesFilePath();
		initFile();
	}

	public String getClassesFilePath() {
		return classesFilePath;
	}

	public Properties getWebHtmlClassesProperties() {
		return webHtmlClassesProperties;
	}

	private void initFile() {
		Properties webHtmlClassesProps = new Properties();
		String resourceFileName = "web_classes/web_html_classes.properties";
		try (InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(
				getClass().getClassLoader().getResourceAsStream(resourceFileName)),StandardCharsets.UTF_8)) {
			webHtmlClassesProps.load(isr);
			log.debug("Loaded web html classes file: {}", resourceFileName);
		} catch (IOException e) {
			log.warn("Exception caught when reading {}", resourceFileName, e);
		}

		String customFileName = classesFilePath;
		try (InputStreamReader isr = new InputStreamReader(
				new FileInputStream(customFileName), StandardCharsets.UTF_8
		)) {
			webHtmlClassesProps.load(isr);
			log.debug("Loaded web html classes file: {}", customFileName);
		} catch (FileNotFoundException e) {
			log.warn("File: {} not found", customFileName);
			e.printStackTrace();
		} catch (IOException e) {
			log.warn("Exception caught when reading {}", customFileName, e);
		}

		this.webHtmlClassesProperties = webHtmlClassesProps;
	}
}
