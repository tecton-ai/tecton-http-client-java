<!--- [0.7.0-SNAPSHOT] --->
## [0.7.0] - 2023-08-18

### Changed
- Allow request context values to be null in `GetFeaturesRequestData`.

## [0.6.0] - 2023-08-10

### Added
- Custom Exceptions in the Java Client that map to specific HTTP status codes.

  * `BadRequestException` -  HTTP Status 400
  * `UnauthorizedException` - HTTP Status 401
  * `ForbiddenException` - HTTP Status 403
  * `ResourceNotFoundException` - HTTP Status 404
  * `ResourceExhaustedException` - HTTP Status 429
  * `InternalServerErrorException` - HTTP Status 500
  * `ServiceUnavailableException` - HTTP Status 503
  * `GatewayTimeoutException` - HTTP Status 504

- `InvalidRequestParameterException` for misconfigured requests such as missing workspace name, missing feature service name etc

### Changed
- Make getters in `GetFeaturesRequestData` public for better debuggability

### Fixed
- Fix a typo in one of the error messages

## [0.5.1] - 2023-06-15


### Fixed
- NullPointerException while fetching null feature values in Lists

## [0.5.0] - 2023-06-13
### Added
- Support for `equals()` and `hashCode()` for all Request and Response classes
- Builders for all Request and Response classes
- Ability to bring in a custom OkHttp client instead of using the Tecton Client default.

### Changed
- Allow Tecton API Key to be null during Tecton Client initialization so that it can be added to the header by request interceptors instead.

### Fixed
- Fixed parsing `effective_time` in `GetFeaturesResponse` and `GetFeaturesBatchResponse`

## [0.4.0] - 2023-03-23

### Added
- Add comments to clarify supported data types for feature values in Tecton

### Changed
* Allow join key values to be null in `GetFeaturesRequestData`, for consistency with the REST API.
