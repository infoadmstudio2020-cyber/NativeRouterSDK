# NativeRouterSDK

Pattern-based URL and RPC command routing engine with interceptor pipeline for Android.

[![JitPack](https://jitpack.io/v/infoadmstudio2020-cyber/NativeRouterSDK.svg)](https://jitpack.io/#infoadmstudio2020-cyber/NativeRouterSDK)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-Apache-2.0-blue.svg)](LICENSE)

## Architecture Overview

`NativeRouterSDK` operates at **Layer 2: Domain Engine Subsystems (Routing & Dispatch)** within the NativeRuntime ecosystem.
It provides:
- **Dynamic URI Scheme Routing**: Support for `app://`, `bridge://`, `native://`, and standard path patterns.
- **Path Variable Extraction**: Automatic extraction of parameterized route variables (e.g., `/user/{id}`, `app://order/{orderId}`).
- **Query Parameter Parsing**: Decoding and mapping query parameters.
- **Ordered Chain-of-Responsibility Interceptors**:
  - `AuthInterceptor`: Guards protected paths against unauthorized access with token verification.
  - `LoggingInterceptor`: Comprehensive routing diagnostic logs powered by `NativeLoggerSDK` (Layer 1).
  - `PerformanceInterceptor`: Execution duration and latency telemetry.
- **Built-in Handlers**:
  - `EchoCommandHandler`: Echoes back requests for verification (`/echo`).
  - `SystemDiagnosticHandler`: Hardware, memory, and runtime health telemetry (`/system/diagnostic`).
- **Service Integration**: Seamless registration with `NativeServiceSDK` (Layer 1) `ServiceRegistry` via `NativeRouterService`.

## Installation

### Step 1 — Add JitPack repository

In your root `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2 — Add dependency

In your module's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.infoadmstudio2020-cyber:NativeRouterSDK:1.0.0'

    // Ecosystem dependencies
    implementation 'com.github.infoadmstudio2020-cyber:NativeServiceSDK:1.0.1'
    api 'com.github.infoadmstudio2020-cyber:NativeRuntimeContracts:v1.0.1'
    implementation 'com.github.infoadmstudio2020-cyber:NativeLoggerSDK:1.0.1'
}
```

## Quick Start

### 1. Initialize SDK

```java
NativeRouterSDKConfig config = new NativeRouterSDKConfig.Builder()
    .debugEnabled(BuildConfig.DEBUG)
    .tag("AppRouter")
    .timeout(30000)
    .build();

NativeRouterSDK.getInstance().init(context, config);
```

### 2. Register Routes

```java
NativeRouter router = NativeRouter.getInstance();

// Register a route with path variables
router.registerRoute("app://user/{id}", new IApiHandler() {
    @Override
    public String getPath() {
        return "app://user/{id}";
    }

    @Override
    public String getAction() {
        return "getUser";
    }

    @Override
    public NativeApiResponse handleRequest(NativeApiRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", request.getParameter("id"));
        return NativeApiResponse.success(request.getRequestId(), data);
    }
});
```

### 3. Add Interceptors

```java
// Auth Interceptor
AuthInterceptor auth = new AuthInterceptor("secret_token")
    .addProtectedPrefix("/secure")
    .addProtectedPrefix("app://secure");
router.addInterceptor(auth);

// Telemetry Interceptors
router.addInterceptor(new LoggingInterceptor());
router.addInterceptor(new PerformanceInterceptor());
```

### 4. Dispatch Commands

```java
// Synchronous dispatch
NativeApiRequest req = new NativeApiRequest("req_1", "/echo", "echo", params, System.currentTimeMillis());
NativeApiResponse res = router.dispatchSync(req);

// Asynchronous dispatch
router.dispatch(req, new IApiCallback() {
    @Override
    public void onResponse(NativeApiResponse response) {
        // Handle response
    }

    @Override
    public void onError(NativeApiError error) {
        // Handle error
    }
});
```

## Requirements

- Android minSdk 21 (Android 5.0 Lollipop)
- compileSdk 34+
- Java 17

## Companion Demo App

The repository includes a companion demonstration application in the `app` module showcasing:
- Live route matching and dispatching
- Protected path authorization validation
- Real-time latency measurement and log console feedback

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.

