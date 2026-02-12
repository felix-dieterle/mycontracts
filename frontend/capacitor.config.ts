import type { CapacitorConfig } from '@capacitor/cli';

// Environment-based configuration
// For development: Set CAPACITOR_SERVER_URL environment variable
// For production: Leave empty to use bundled web files
const serverUrl = process.env.CAPACITOR_SERVER_URL;

const config: CapacitorConfig = {
  appId: 'com.mycontracts.app',
  appName: 'MyContracts',
  webDir: 'dist',
  server: serverUrl ? {
    url: serverUrl,
    cleartext: true, // Required for HTTP (not HTTPS) during development
  } : undefined,
  // Enable native plugins
  plugins: {
    SplashScreen: {
      launchShowDuration: 2000,
      backgroundColor: '#ffffff',
      showSpinner: false,
    },
  },
};

export default config;
