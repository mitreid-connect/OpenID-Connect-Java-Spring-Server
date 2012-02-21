Changelog

Updated on 2/21/2012

OAuth2: 
* Renamed "Access Code Flow" to "Authorization Code Flow".

* Changed all references to "User" to "Resource Owner".

* Changed final "Response"s to "JSON respones object"s.

* Added initial "Authenticate Resource Owner" step to Authorization Code Flow

Connect:

* Changed final "Response"s to "JSON respones object"s.

Updated on 2/7/2012

OAuth2:
* Removed refresh_token from the Access Token response on the Client Credentials flow. 	
	Ref: http://tools.ietf.org/html/draft-ietf-oauth-v2-23#section-4.4.3 
	"A refresh token SHOULD NOT be included."

* Changed "Consumer" to "Client".

Connect:
* Changed "Consumer" to "Client". 

* Clarified required/optional wording. Parameters are REQUIRED unless otherwise stated.

* Implicit Flow: changed wording on redirect_uri requirement in the Authorization Request. Now reads "required IFF the client has pre-configured more than one value with the service provider". 

* Diagram 3 was renamed to "Optional Steps" (from "Additional Steps"), as these steps may or may not be taken and may be done in any order. Added "openid" to the schema parameter in the UserInfo Request.