#!/usr/bin/env node

/**
 * EduConflux Render Keep-Alive Script
 * -----------------------------------
 * Periodically pings the Render backend server to prevent free-tier
 * instances from spinning down after 15 minutes of inactivity.
 *
 * Usage:
 *   node scripts/keep-alive.js
 *   node scripts/keep-alive.js https://your-app.onrender.com
 *   SERVER_URL=https://your-app.onrender.com PING_INTERVAL_MINUTES=10 node scripts/keep-alive.js
 */

const targetUrlArg = process.argv[2];
const baseUrl = (targetUrlArg || process.env.SERVER_URL || 'https://educonflux-backend.onrender.com').replace(/\/+$/, '');
const healthEndpoint = baseUrl.endsWith('/health') || baseUrl.endsWith('/api/health')
  ? baseUrl
  : `${baseUrl}/api/health`;

const intervalMinutes = parseInt(process.env.PING_INTERVAL_MINUTES || '10', 10);
const intervalMs = intervalMinutes * 60 * 1000;

let pingCount = 0;
let successCount = 0;
let failCount = 0;

function formatTimestamp() {
  return new Date().toISOString().replace('T', ' ').substring(0, 19);
}

async function pingServer() {
  pingCount++;
  const startTime = Date.now();
  console.log(`\n[${formatTimestamp()}] [PING #${pingCount}] Sending keep-alive request to: ${healthEndpoint}`);

  try {
    const controller = new AbortController();
    // Render free-tier cold start can take up to 60s
    const timeoutId = setTimeout(() => controller.abort(), 65000);

    const response = await fetch(healthEndpoint, {
      method: 'GET',
      headers: {
        'User-Agent': 'EduConflux-KeepAlive/1.0',
        'Accept': 'application/json',
      },
      signal: controller.signal,
    });

    clearTimeout(timeoutId);
    const durationMs = Date.now() - startTime;

    if (response.ok) {
      successCount++;
      let bodyText = '';
      try {
        const json = await response.json();
        bodyText = JSON.stringify(json);
      } catch {
        bodyText = await response.text();
      }

      console.log(`  Status: \x1b[32m${response.status} ${response.statusText}\x1b[0m (${durationMs}ms)`);
      console.log(`  Payload: ${bodyText}`);
      console.log(`  Stats: ${successCount} succeeded, ${failCount} failed`);
    } else {
      failCount++;
      console.warn(`  Status: \x1b[33m${response.status} ${response.statusText}\x1b[0m (${durationMs}ms)`);
    }
  } catch (error) {
    failCount++;
    const durationMs = Date.now() - startTime;
    if (error.name === 'AbortError') {
      console.error(`  \x1b[31mError: Request timed out after 65s (Server is likely waking up from cold sleep)\x1b[0m`);
    } else {
      console.error(`  \x1b[31mError: ${error.message}\x1b[0m (${durationMs}ms)`);
    }
  }

  const nextPingTime = new Date(Date.now() + intervalMs).toLocaleTimeString();
  console.log(`  Next ping scheduled at: ${nextPingTime} (in ${intervalMinutes} minutes)`);
}

console.log('====================================================');
console.log('       EduConflux Render Keep-Alive Daemon          ');
console.log('====================================================');
console.log(`Target:   ${healthEndpoint}`);
console.log(`Interval: Every ${intervalMinutes} minutes`);
console.log(`Press Ctrl+C to stop.`);
console.log('====================================================');

// Immediate first ping
pingServer();

// Periodic cron
const intervalHandle = setInterval(pingServer, intervalMs);

process.on('SIGINT', () => {
  clearInterval(intervalHandle);
  console.log(`\nKeep-alive stopped. Summary: ${successCount}/${pingCount} requests succeeded.`);
  process.exit(0);
});

