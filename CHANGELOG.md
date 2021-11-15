## [3.4.1](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/compare/v3.4.0...v3.4.1) (2021-11-15)


### Bug Fixes

* 🐛 Added missing PostgreSQL dependency ([e12c164](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/commit/e12c164b46cbf9efb1a3516cb8c03e307e7049c2))

# [3.4.0](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/compare/v3.3.0...v3.4.0) (2021-11-12)


### Features

* 🎸 Forward client_id in AuthenticationContextClass ([6a6d1e3](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/commit/6a6d1e3ad92d3c6785f0e786aaf4c3fa5f04b806))

# [3.3.0](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/compare/v3.2.0...v3.3.0) (2021-11-11)


### Features

* 🎸 Extended list of internal referrers for sess. invalider ([9aa16ff](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/commit/9aa16ffe5cb1c1b045d9f1f71cd94751d9d876b4))
* 🎸 Make SAML identifier attribute configurable ([3949857](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/commit/39498573c3d62284298bae0df48fbbcf071e9caf))

# [3.2.0](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/compare/v3.1.0...v3.2.0) (2021-11-09)


### Features

* 🎸 Adderd e-INFRA CZ template ([5eb50f6](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/commit/5eb50f64414db6a42cff76003c5b41f4e8e03535))

# [3.1.0](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/compare/v3.0.1...v3.1.0) (2021-11-08)


### Features

* 🎸 Sign refresh tokens ([23a6354](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/commit/23a6354fc708bd89301bf2cac0619bbebb431f4f))

## [3.0.1](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/compare/v3.0.0...v3.0.1) (2021-11-05)


### Bug Fixes

* 🐛 fix loading JWKS ([371adc1](https://github.com/CESNET/OpenID-Connect-Java-Spring-Server/commit/371adc13fbff6150a32fcd8b5242ef03899c758b))

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
