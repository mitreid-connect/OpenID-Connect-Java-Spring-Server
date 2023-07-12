Unreleased:

*1.3.3*:
- Authorization codes are now longer
- Client/RS can parse the "sub" and "user_id" claims in introspection response
- Database-direct queries for fetching tokens by user (optimization)
- Device flow supports verification_uri_complete (must be turned on)
- Long scopes display properly and are still checkable
- Language system remebers when it can't find a file and stops throwing so many errors
- Index added for refresh tokens
- Updated to Spring Security 4.2.11
- Updated Spring to 4.3.22
- Change approve pages to use issuer instead of page context
- Updated oracle database scripts

*1.3.2*:
- Added changelog
- Set default redirect URI resolver strict matching to true
- Fixed XSS vulnerability on redirect URI display on approval page
- Removed MITRE from copyright
- Disallow unsigned JWTs on client authentication
- Upgraded Nimbus revision
- Added French translation
- Added hooks for custom JWT claims
- Removed "Not Yet Implemented" tag from post-logout redirect URI

*1.3.1*:
- Added End Session endpoint
- Fixed discovery endpoint
- Downgrade MySQL connector dependency version from developer preview to GA release

*1.3.0*:
- Added device flow support
- Added PKCE support
- Modularized UI to allow better overlay and extensions
- Modularized data import/export API
- Added software statements to dynamic client registration
- Added assertion processing framework
- Removed ID tokens from storage
- Removed structured scopes

*1.2.6*: 
- Added strict HEART compliance mode
