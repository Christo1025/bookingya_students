import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  expect: {
    timeout: 5_000,
  },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.BOOKINGYA_API_URL ?? 'http://localhost:8081/api/',
    extraHTTPHeaders: {
      Accept: 'application/json',
    },
  },
});
