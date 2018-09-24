package org.mitre.openid.connect.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component("defaultPasswordEncoder")
public class DefaultPasswordEncoder extends BCryptPasswordEncoder {

	public DefaultPasswordEncoder() {
		super(11);
	}

}
