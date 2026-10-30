# Changelog

All notable changes to NativeRouterSDK are documented here.
Format based on [Keep a Changelog](https://keepachangelog.com).

## [1.0.0] — 2026-10-30

### Added
- Complete Layer 2 Domain Engine implementation for URL & RPC command routing.
- `IRouter` public interface and `NativeRouter` core state machine with thread-safe singleton lifecycle.
- `PatternRouteMatcher` with support for `app://`, `bridge://`, `native://` URI schemes, path variables `{id}`, and query parameters.
- Interceptor chain-of-responsibility pipeline:
  - `AuthInterceptor` with route prefix protection and token validation.
  - `LoggingInterceptor` integrated with Layer 1 `NativeLoggerSDK`.
  - `PerformanceInterceptor` tracking dispatch execution latencies.
- Built-in command handlers:
  - `EchoCommandHandler` (`/echo`).
  - `SystemDiagnosticHandler` (`/system/diagnostic`).
- Standard exceptions `RouteNotFoundException` and `RouterExecutionException`.
- Models `RouteRequest`, `RouteResponse`, `RouteMatchResult`, and `NativeRouterSDKResult<T>`.
- `NativeRouterService` integrating `NativeRouter` with Layer 1 `NativeServiceSDK` `ServiceRegistry`.
- Companion demonstration app in `app` module with live UI controls and visual telemetry output.
- Comprehensive automated JVM unit tests with 100% test scenario coverage.
- Updated GitHub Actions CI/CD workflows with write permissions and `softprops/action-gh-release@v2`.
- JitPack publication configuration.

