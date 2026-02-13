# CI/CD Workflows

This repository has automated CI/CD workflows to build, test, and release the application.

## Continuous Integration (CI)

The CI workflow runs automatically on:
- **Push** to `main` or `dev` branches
- **Pull Requests** targeting `main` or `dev` branches

### What it does:
1. **Java Version Validation** - Checks Android build configuration for Java version compatibility
2. **Backend Tests** - Runs Maven tests for the Spring Boot backend
3. **Frontend Build** - Builds the React frontend with Vite
4. **UI Tests** - Runs Playwright end-to-end tests

All tests must pass before code can be merged.

### Local Build Validation

Before pushing code that involves Android builds, run the Java version check script:

```bash
./check-java-version.sh
```

This script will verify that your local Java version matches the Android build configuration and detect potential build failures early. The script is executable and ready to run.

## Release Workflow

The release workflow creates downloadable artifacts in two scenarios:

### 1. Automatic Artifacts on Branch Merges

When code is merged to `main` or `development` branches, the workflow automatically:
- Builds the backend JAR file
- Builds the frontend distribution
- Builds a **signed debug Android APK** (easy to install for testing)
- Creates a GitHub Release with downloadable files:
  - `mycontracts-backend-<branch>-<commit>.jar` - Backend executable JAR
  - `mycontracts-frontend-<branch>-<commit>.zip` - Frontend static files
  - `mycontracts-<branch>-<commit>-debug.apk` - **Signed debug Android APK**

**Debug APK vs Release APK:**
- **Debug APK** (branch builds): Automatically signed with Android's default debug keystore, can be installed immediately for testing
- **Release APK** (tag builds): Unsigned by default, requires manual signing for production deployment

**To download branch artifacts:**
1. Go to the [Releases page](https://github.com/felix-dieterle/mycontracts/releases)
2. Find the latest release for your branch (marked as "Pre-release")
3. Download the debug APK and other artifacts directly from the release

Alternatively, artifacts are also available in the Actions tab:
1. Go to the [Actions tab](https://github.com/felix-dieterle/mycontracts/actions)
2. Click on the latest workflow run for your branch
3. Scroll down to the "Artifacts" section
4. Download the artifacts you need (available for 30 days)

### 2. Tagged Releases

When you create a git tag, the workflow creates a permanent GitHub Release.

### Creating a Tagged Release

1. **Create and push a version tag:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **The workflow automatically:**
   - Builds the backend JAR file
   - Builds the frontend distribution
   - Builds an **unsigned release Android APK**
   - Creates a GitHub Release
   - Uploads artifacts:
     - `mycontracts-backend-1.0.0.jar` - Backend executable JAR
     - `mycontracts-frontend-1.0.0.zip` - Frontend static files
     - `mycontracts-1.0.0.apk` - **Unsigned release Android APK** (requires signing for production)

3. **Find your release:**
   - Go to the [Releases page](https://github.com/felix-dieterle/mycontracts/releases)
   - Download the artifacts you need

**Note:** For production Play Store releases, you'll need to sign the release APK. See [frontend/android/README.md](frontend/android/README.md) for signing instructions.

### Tag Naming Convention

Tags should follow semantic versioning with a `v` prefix:
- `v1.0.0` - Major release
- `v1.1.0` - Minor release with new features
- `v1.1.1` - Patch release with bug fixes
- `v2.0.0-beta.1` - Pre-release versions

### Deployment

After downloading the release artifacts:

**Backend:**
```bash
java -jar mycontracts-backend-1.0.0.jar
```

**Frontend:**
```bash
unzip mycontracts-frontend-1.0.0.zip -d /var/www/html
# Or serve with any static file server
```

**Android:**
```bash
# For debug builds (from branch merges)
# Install directly - already signed with debug keystore
adb install mycontracts-main-abc1234-debug.apk

# For release builds (from tags)
# Requires signing before installation - see frontend/android/README.md
# After signing:
adb install mycontracts-1.0.0-signed.apk
# Or transfer to your device and install manually
```

## Workflow Files

- `.github/workflows/ci.yml` - Continuous Integration tests
- `.github/workflows/release.yml` - Release and artifact creation
