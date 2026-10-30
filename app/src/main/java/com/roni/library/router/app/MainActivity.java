package com.roni.library.router.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.roni.library.contracts.api.IApiCallback;
import com.roni.library.contracts.api.IApiHandler;
import com.roni.library.contracts.api.NativeApiError;
import com.roni.library.contracts.api.NativeApiRequest;
import com.roni.library.contracts.api.NativeApiResponse;
import com.roni.library.router.NativeRouter;
import com.roni.library.router.NativeRouterSDK;
import com.roni.library.router.NativeRouterSDKConfig;
import com.roni.library.router.interceptor.AuthInterceptor;
import com.roni.library.router.interceptor.LoggingInterceptor;
import com.roni.library.router.interceptor.PerformanceInterceptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Interactive Companion Demonstration Activity for NativeRouterSDK.
 * Provides controls for testing route registration, pattern matching,
 * URI schemes (app://), interceptor pipeline execution, and error handling.
 *
 * @author infoadmstudio2020-cyber
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private TextView tvConsole;
    private ScrollView scrollConsole;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    private NativeRouter router;
    private LoggingInterceptor loggingInterceptor;
    private PerformanceInterceptor perfInterceptor;
    private AuthInterceptor authInterceptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRouter();
        setupListeners();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvConsole = findViewById(R.id.tv_console_output);
        scrollConsole = findViewById(R.id.scroll_console);
    }

    private void setupRouter() {
        appendLog("[INIT] Initializing NativeRouterSDK...");

        NativeRouterSDKConfig config = new NativeRouterSDKConfig.Builder()
                .debugEnabled(true)
                .tag("NativeRouterApp")
                .timeout(15000)
                .build();

        NativeRouterSDK.getInstance().init(this, config);
        router = NativeRouter.getInstance();

        // Attach interceptors
        loggingInterceptor = new LoggingInterceptor();
        perfInterceptor = new PerformanceInterceptor();
        authInterceptor = new AuthInterceptor("secret_token_123");
        authInterceptor.addProtectedPrefix("/secure");
        authInterceptor.addProtectedPrefix("app://secure");

        router.addInterceptor(loggingInterceptor);
        router.addInterceptor(perfInterceptor);
        router.addInterceptor(authInterceptor);

        // Register custom test route for app:// scheme
        router.registerRoute("app://user/{id}", new IApiHandler() {
            @Override
            public String getPath() {
                return "app://user/{id}";
            }

            @Override
            public String getAction() {
                return "getUserProfile";
            }

            @Override
            public NativeApiResponse handleRequest(NativeApiRequest request) {
                Map<String, Object> data = new HashMap<>();
                data.put("userId", request.getParameter("id") != null ? request.getParameter("id") : "unknown");
                data.put("role", request.getParameter("role") != null ? request.getParameter("role") : "standard");
                data.put("email", "user@example.com");
                data.put("status", "ACTIVE");
                return NativeApiResponse.success(request.getRequestId(), data);
            }
        });

        // Register secure route
        router.registerRoute("/secure/vault", new IApiHandler() {
            @Override
            public String getPath() {
                return "/secure/vault";
            }

            @Override
            public String getAction() {
                return "accessVault";
            }

            @Override
            public NativeApiResponse handleRequest(NativeApiRequest request) {
                Map<String, Object> data = new HashMap<>();
                data.put("vaultData", "Top secret payload unlocked");
                data.put("accessGrantedAt", System.currentTimeMillis());
                return NativeApiResponse.success(request.getRequestId(), data);
            }
        });

        updateStatusBanner();
        appendLog("[READY] NativeRouter initialized. Default routes and interceptors active.");
    }

    private void updateStatusBanner() {
        if (router != null) {
            String text = "Router State: " + router.getState() +
                    " | Routes: " + router.getRoutes().size() +
                    " | Interceptors: " + router.getInterceptors().size() +
                    " | Latency: " + perfInterceptor.getLastExecutionDurationMs() + "ms";
            tvStatus.setText(text);
        }
    }

    private void setupListeners() {
        Button btnEcho = findViewById(R.id.btn_echo);
        Button btnDiagnostic = findViewById(R.id.btn_diagnostic);
        Button btnCustom = findViewById(R.id.btn_custom_route);
        Button btnAuthValid = findViewById(R.id.btn_auth_valid);
        Button btnAuthInvalid = findViewById(R.id.btn_auth_invalid);
        Button btnNotFound = findViewById(R.id.btn_not_found);
        Button btnClear = findViewById(R.id.btn_clear_logs);

        btnEcho.setOnClickListener(v -> dispatchEcho());
        btnDiagnostic.setOnClickListener(v -> dispatchDiagnostic());
        btnCustom.setOnClickListener(v -> dispatchAppScheme());
        btnAuthValid.setOnClickListener(v -> dispatchAuth(true));
        btnAuthInvalid.setOnClickListener(v -> dispatchAuth(false));
        btnNotFound.setOnClickListener(v -> dispatchNotFound());
        btnClear.setOnClickListener(v -> tvConsole.setText("Logs cleared.\n"));
    }

    private void dispatchEcho() {
        Map<String, Object> params = new HashMap<>();
        params.put("clientTime", System.currentTimeMillis());
        params.put("message", "Hello from NativeRouter Demo");
        dispatchRequest("/echo", "echo", params);
    }

    private void dispatchDiagnostic() {
        dispatchRequest("/system/diagnostic", "diagnostic", new HashMap<>());
    }

    private void dispatchAppScheme() {
        Map<String, Object> params = new HashMap<>();
        params.put("role", "administrator");
        params.put("id", "42");
        dispatchRequest("app://user/42?role=administrator", "getUserProfile", params);
    }

    private void dispatchAuth(boolean validToken) {
        Map<String, Object> params = new HashMap<>();
        if (validToken) {
            params.put("token", "secret_token_123");
        } else {
            params.put("token", "wrong_token_xyz");
        }
        dispatchRequest("/secure/vault", "accessVault", params);
    }

    private void dispatchNotFound() {
        dispatchRequest("/unknown/feature/path", "query", new HashMap<>());
    }

    private void dispatchRequest(String path, String action, Map<String, Object> params) {
        String reqId = "demo_" + UUID.randomUUID().toString().substring(0, 8);
        NativeApiRequest request = new NativeApiRequest(reqId, path, action, params, System.currentTimeMillis());

        appendLog("--> DISPATCH: [" + reqId + "] " + path + " (Action: " + action + ")");

        router.dispatch(request, new IApiCallback() {
            @Override
            public void onResponse(NativeApiResponse response) {
                mainHandler.post(() -> {
                    appendLog("<-- SUCCESS: [" + response.getRequestId() + "] Code=" + response.getStatusCode()
                            + ", Data=" + response.getData());
                    updateStatusBanner();
                });
            }

            @Override
            public void onError(NativeApiError error) {
                mainHandler.post(() -> {
                    appendLog("<-- ERROR: [" + reqId + "] Code=" + error.getCode()
                            + ", Msg=" + error.getMessage() + ", Details=" + error.getDetails());
                    updateStatusBanner();
                });
            }
        });
    }

    private void appendLog(String message) {
        String timestamp = dateFormat.format(new Date());
        String entry = "[" + timestamp + "] " + message + "\n";
        tvConsole.append(entry);
        scrollConsole.post(() -> scrollConsole.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (router != null) {
            router.release();
        }
    }
}
