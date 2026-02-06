import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.mycontracts.app',
  appName: 'MyContracts',
  webDir: 'dist',
  server: {
    // For development, you can set the backend URL here
    // Example: url: 'http://10.0.2.2:8080' for Android emulator
    // Example: url: 'http://192.168.1.100:8080' for physical device
    // For production, the app will use the bundled web files
  }
};

export default config;
