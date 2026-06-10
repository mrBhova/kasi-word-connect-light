# Word Connect

An Android word puzzle game built with libGDX. Find and connect letters to form words across 1700+ levels!

## Development

### Prerequisites
- JDK 17
- Android SDK (API 29+)

### Build
```bash
./gradlew :android:assembleDebug
```

APK will be at `android/build/outputs/apk/debug/`

## Releases

New releases are auto-built by GitHub Actions. To create a release:
```bash
git tag v1.0.0
git push origin v1.0.0
```
