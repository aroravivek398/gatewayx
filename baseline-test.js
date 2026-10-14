import http from 'k6/http';
import { check, sleep } from 'k6';

// Usage: k6 run -e API_KEY=<your key> [-e BASE_URL=http://localhost:8081] baseline-test.js
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const API_KEY = __ENV.API_KEY;
if (!API_KEY) {
  throw new Error('Set API_KEY, e.g. k6 run -e API_KEY=gwx_live_... baseline-test.js');
}

export const options = {
  vus: 10,
  duration: '30s',
};

export default function () {
  const res = http.get(`${BASE_URL}/api/v1/demo/ping`, {
    headers: { 'X-API-Key': API_KEY },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(0.1);
}