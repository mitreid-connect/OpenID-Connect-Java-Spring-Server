package org.mitre.openid.connect.web

import org.mitre.openid.connect.model.DefaultUserInfo
import org.mitre.openid.connect.model.UserInfo
import spock.lang.Specification

class UserInfoInterceptorSpec extends Specification {

	private def userInfoInterceptor = new UserInfoInterceptor()

	// CVE-2020-5497 -> https://github.com/mitreid-connect/OpenID-Connect-Java-Spring-Server/issues/1521
	def 'User Info is sanitised before making it back to the webpage'() {
		given: 'A user name with a malicious payload'

		UserInfo userInfo = new DefaultUserInfo()
		userInfo.setSub('12318767')
		userInfo.setName("Test</script><script>alert(1)</script> Test")
		userInfo.setPreferredUsername('Test')
		userInfo.setGivenName("Test</script><script>alert(1)</script>")
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
	}

}
