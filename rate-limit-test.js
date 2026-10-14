import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// Usage: k6 run -e API_KEY=<your key> [-e BASE_URL=http://localhost:8081] rate-limit-test.js
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const API_KEY = __ENV.API_KEY;
if (!API_KEY) {
  throw new Error('Set API_KEY, e.g. k6 run -e API_KEY=gwx_live_... rate-limit-test.js');
}

export const options = {
  vus: 5,
  duration: '30s',
};

const successCount = new Counter('success_200');
const rateLimitedCount = new Counter('rate_limited_429');

export default function () {
  const res = http.get(`${BASE_URL}/api/v1/demo/ping`, {
    headers: { 'X-API-Key': API_KEY },
  });

  if (res.status === 200) {
    successCount.add(1);
  } else if (res.status === 429) {
    rateLimitedCount.add(1);
  }

  check(res, {
    'status is 200 or 429': (r) => r.status === 200 || r.status === 429,
  });

  sleep(0.2);
}