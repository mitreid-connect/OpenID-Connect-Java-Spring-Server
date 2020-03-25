package org.mitre.openid.connect.web

import org.mitre.openid.connect.model.DefaultUserInfo
import org.mitre.openid.connect.model.UserInfo
import spock.lang.Specification
import spock.lang.Unroll

class UserInfoInterceptorSpec extends Specification {

	private def userInfoInterceptor = new UserInfoInterceptor()

	// CVE-2020-5497 -> https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/issues/1521
	@Unroll
	def 'User Info is sanitised before making it back to the webpage with payload #payload'() {
		given: 'A user name with a malicious payload'

		UserInfo userInfo = new DefaultUserInfo()
		userInfo.setSub('12318767')
		userInfo.setName("Test" + payload + " Test")
		userInfo.setPreferredUsername('Test')
		userInfo.setGivenName("Test" + payload)
		userInfo.setFamilyName('Test')
		userInfo.setEmail('test@test.com')
		userInfo.setEmailVerified(true)

		when: 'The user info object is passed through the sanitise method'

		userInfoInterceptor.sanitiseUserInfo(userInfo)

		then: 'The malicious names have been sanitised'

		userInfo.getName() == 'Test Test'
		userInfo.getGivenName() == 'Test'

		and: 'The non malicious elements have been unaffected'

		userInfo.getSub() == '12318767'
		userInfo.getPreferredUsername() == 'Test'
		userInfo.getFamilyName() == 'Test'
		userInfo.getEmail() == 'test@test.com'

		where:

		payload | _
		"</script><script>alert(1)</script>" | _
		"<body src=1 href=1 onerror=\"javascript:alert(1)\"></body>" | _
		"<html onMouseWheel html onMouseWheel=\"javascript:javascript:alert(1)\"></html onMouseWheel>" | _
		"<IMG SRC=`javascript:javascript:alert(1)`>" | _
		"<script ~~~>alert(0%0)</script ~~~>" | _
		"<IMG SRC=x onload=\"alert(String.fromCharCode(88,83,83))\">" | _
		"<div STYLE=\"background-image: url(&#1;javascript:document.vulnerable=true;)\">" | _
		"<BODY ONLOAD=javascript:alert(1)>" | _
		"<iframe src=\"vbscript:document.vulnerable=true;\">" | _
		"<br SIZE=\"&{document.vulnerable=true}\">" | _
		"<img src=\"Mario Heiderich says that svg SHOULD not be executed trough image tags\" onerror=\"javascript:document.write('\\u003c\\u0069\\u0066\\u0072\\u0061\\u006d\\u0065\\u0020\\u0073\\u0072\\u0063\\u003d\\u0022\\u0064\\u0061\\u0074\\u0061\\u003a\\u0069\\u006d\\u0061\\u0067\\u0065\\u002f\\u0073\\u0076\\u0067\\u002b\\u0078\\u006d\\u006c\\u003b\\u0062\\u0061\\u0073\\u0065\\u0036\\u0034\\u002c\\u0050\\u0048\\u004e\\u0032\\u005a\\u0079\\u0042\\u0034\\u0062\\u0057\\u0078\\u0075\\u0063\\u007a\\u0030\\u0069\\u0061\\u0048\\u0052\\u0030\\u0063\\u0044\\u006f\\u0076\\u004c\\u0033\\u0064\\u0033\\u0064\\u0079\\u0035\\u0033\\u004d\\u0079\\u0035\\u0076\\u0063\\u006d\\u0063\\u0076\\u004d\\u006a\\u0041\\u0077\\u004d\\u0043\\u0039\\u007a\\u0064\\u006d\\u0063\\u0069\\u0050\\u0069\\u0041\\u0067\\u0043\\u0069\\u0041\\u0067\\u0049\\u0044\\u0078\\u0070\\u0062\\u0057\\u0046\\u006e\\u005a\\u0053\\u0042\\u0076\\u0062\\u006d\\u0078\\u0076\\u0059\\u0057\\u0051\\u0039\\u0049\\u006d\\u0046\\u0073\\u005a\\u0058\\u004a\\u0030\\u004b\\u0044\\u0045\\u0070\\u0049\\u006a\\u0034\\u0038\\u004c\\u0032\\u006c\\u0074\\u0059\\u0057\\u0064\\u006c\\u0050\\u0069\\u0041\\u0067\\u0043\\u0069\\u0041\\u0067\\u0049\\u0044\\u0078\\u007a\\u0064\\u006d\\u0063\\u0067\\u0062\\u0032\\u0035\\u0073\\u0062\\u0032\\u0046\\u006b\\u0050\\u0053\\u004a\\u0068\\u0062\\u0047\\u0056\\u0079\\u0064\\u0043\\u0067\\u0079\\u004b\\u0053\\u0049\\u002b\\u0050\\u0043\\u0039\\u007a\\u0064\\u006d\\u0063\\u002b\\u0049\\u0043\\u0041\\u004b\\u0049\\u0043\\u0041\\u0067\\u0050\\u0048\\u004e\\u006a\\u0063\\u006d\\u006c\\u0077\\u0064\\u0044\\u0035\\u0068\\u0062\\u0047\\u0056\\u0079\\u0064\\u0043\\u0067\\u007a\\u004b\\u0054\\u0077\\u0076\\u0063\\u0032\\u004e\\u0079\\u0061\\u0058\\u0042\\u0030\\u0050\\u0069\\u0041\\u0067\\u0043\\u0069\\u0041\\u0067\\u0049\\u0044\\u0078\\u006b\\u005a\\u0057\\u005a\\u007a\\u0049\\u0047\\u0039\\u0075\\u0062\\u0047\\u0039\\u0068\\u005a\\u0044\\u0030\\u0069\\u0059\\u0057\\u0078\\u006c\\u0063\\u006e\\u0051\\u006f\\u004e\\u0043\\u006b\\u0069\\u0050\\u006a\\u0077\\u0076\\u005a\\u0047\\u0056\\u006d\\u0063\\u007a\\u0034\\u0067\\u0049\\u0041\\u006f\\u0067\\u0049\\u0043\\u0041\\u0038\\u005a\\u0079\\u0042\\u0076\\u0062\\u006d\\u0078\\u0076\\u0059\\u0057\\u0051\\u0039\\u0049\\u006d\\u0046\\u0073\\u005a\\u0058\\u004a\\u0030\\u004b\\u0044\\u0055\\u0070\\u0049\\u006a\\u0034\\u0067\\u0049\\u0041\\u006f\\u0067\\u0049\\u0043\\u0041\\u0067\\u0049\\u0043\\u0041\\u0067\\u0050\\u0047\\u004e\\u0070\\u0063\\u006d\\u004e\\u0073\\u005a\\u0053\\u0042\\u0076\\u0062\\u006d\\u0078\\u0076\\u0059\\u0057\\u0051\\u0039\\u0049\\u006d\\u0046\\u0073\\u005a\\u0058\\u004a\\u0030\\u004b\\u0044\\u0059\\u0070\\u0049\\u0069\\u0041\\u0076\\u0050\\u0069\\u0041\\u0067\\u0043\\u0069\\u0041\\u0067\\u0049\\u0043\\u0041\\u0067\\u0049\\u0043\\u0041\\u0038\\u0064\\u0047\\u0056\\u0034\\u0064\\u0043\\u0042\\u0076\\u0062\\u006d\\u0078\\u0076\\u0059\\u0057\\u0051\\u0039\\u0049\\u006d\\u0046\\u0073\\u005a\\u0058\\u004a\\u0030\\u004b\\u0044\\u0063\\u0070\\u0049\\u006a\\u0034\\u0038\\u004c\\u0033\\u0052\\u006c\\u0065\\u0048\\u0051\\u002b\\u0049\\u0043\\u0041\\u004b\\u0049\\u0043\\u0041\\u0067\\u0050\\u0043\\u0039\\u006e\\u0050\\u0069\\u0041\\u0067\\u0043\\u006a\\u0077\\u0076\\u0063\\u0033\\u005a\\u006e\\u0050\\u0069\\u0041\\u0067\\u0022\\u003e\\u003c\\u002f\\u0069\\u0066\\u0072\\u0061\\u006d\\u0065\\u003e');\"></img>" | _
	}

	@Unroll
	def 'User Info is santised to an extent to not produce an XSS with payload #payload'() {
		given: 'A user name with a malicious payload'

		UserInfo userInfo = new DefaultUserInfo()
		userInfo.setSub('12318767')
		userInfo.setName("Test" + payload + " Test")
		userInfo.setPreferredUsername('Test')
		userInfo.setGivenName("Test" + payload)
		userInfo.setFamilyName('Test')
		userInfo.setEmail('test@test.com')
		userInfo.setEmailVerified(true)

		when: 'The user info object is passed through the sanitise method'

		userInfoInterceptor.sanitiseUserInfo(userInfo)

		then: 'The malicious names have been sanitised'

		userInfo.getName() == 'Test' + expectedResponse + ' Test'
		userInfo.getGivenName() == 'Test' + expectedResponse

		and: 'The non malicious elements have been unaffected'

		userInfo.getSub() == '12318767'
		userInfo.getPreferredUsername() == 'Test'
		userInfo.getFamilyName() == 'Test'
		userInfo.getEmail() == 'test@test.com'

		where:

		payload																										| expectedResponse
		"'\"></title><script>alert(1111)</script>"																	| "'\"&gt;"
		"'>//\\\\,<'>\">\">\"*\""																					| "'&gt;//\\\\,&lt;'&gt;\"&gt;\"&gt;\"*\""
		"'\"\"><script language=\"JavaScript\"> alert('X \\nS \\nS');</script>"										| "'\"\"&gt;"
		"!--\" /><script>alert('xss');</script>"																	| "!--\" /&gt;"
		"\">/XaDoS/><script>alert(document.cookie)</script><script src=\"http://www.site.com/XSS.js\"></script>"	| "\"&gt;/XaDoS/&gt;"
	}

}
