# Coinflow fraud protection: Android example

A minimal Android app that initializes Coinflow fraud protection and attaches the `x-device-id` header to
a request. It installs the SDK the way your app would: the `cash.coinflow.fraud.repositories` settings
plugin in `settings.gradle.kts`, and `cash.coinflow:coinflow-fraud` from Maven Central in
`build.gradle.kts`.

## Run

```bash
./gradlew installDebug -PcoinflowMerchantId=your-merchant-id -PcoinflowEnv=sandbox
```

- **Get x-device-id header** shows the header and the request headers it's merged into.
- `-PcoinflowChargeUrl=<url>` shows **Send request**, which POSTs there with the headers.
- `-PcoinflowFraudAutorun=1` collects 5s after launch and logs `COINFLOW_FRAUD_RESULT` under the
  `CoinflowFraudExample` tag, for scripted runs.

The integration is in `src/main/java/cash/coinflow/fraud/example/MainActivity.kt`: `initialize` in
`onCreate`, then `purchaseHeaders()` before each request.
