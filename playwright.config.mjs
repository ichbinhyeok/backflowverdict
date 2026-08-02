import { defineConfig, devices } from '@playwright/test';

const isWindows = process.platform === 'win32';

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: false,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list']],
  use: {
    baseURL: 'http://127.0.0.1:18080',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } }
  ],
  webServer: {
    command: isWindows ? '.\\gradlew.bat bootRun --no-daemon --args="--server.port=18080"' : './gradlew bootRun --no-daemon --args="--server.port=18080"',
    url: 'http://127.0.0.1:18080/healthz',
    reuseExistingServer: !process.env.CI,
    timeout: 120000
  }
});
