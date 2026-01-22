# CI/CD Workflows

This repository has automated CI/CD workflows to build, test, and release the application.

## Continuous Integration (CI)

The CI workflow runs automatically on:
- **Push** to `main` or `dev` branches
- **Pull Requests** targeting `main` or `dev` branches

### What it does:
1. **Backend Tests** - Runs Maven tests for the Spring Boot backend
2. **Frontend Build** - Builds the React frontend with Vite
3. **UI Tests** - Runs Playwright end-to-end tests

All tests must pass before code can be merged.

## Release Workflow

The release workflow creates downloadable artifacts when you create a git tag.

### Creating a Release

1. **Create and push a version tag:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **The workflow automatically:**
   - Builds the backend JAR file
   - Builds the frontend distribution
   - Creates a GitHub Release
   - Uploads artifacts:
     - `mycontracts-backend-1.0.0.jar` - Backend executable JAR
     - `mycontracts-frontend-1.0.0.zip` - Frontend static files

3. **Find your release:**
   - Go to the [Releases page](https://github.com/felix-dieterle/mycontracts/releases)
   - Download the artifacts you need

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

## Workflow Files

- `.github/workflows/ci.yml` - Continuous Integration tests
- `.github/workflows/release.yml` - Release and artifact creation
