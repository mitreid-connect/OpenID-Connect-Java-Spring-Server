Unreleased:
- Updated JDK to Corretto 1.8.332
- Uprgaded Jackson Components to 2.13.3

*1.3.3-GRESHAM:
- Upgraded libraries with known vulnerabilities
- Added a Gresham specific Jenkinsfile
- Added a password encoder to the client entity service
- Fixes a bug by specifying the name of the scope columnn
- Removed functionality that passed the client secret down to the UI
- Updated JDK to Corretto 1.8.252

*1.3.2:
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
