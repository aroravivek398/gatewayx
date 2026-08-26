import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

export const options = {
  vus: 5,
  duration: '30s',
};

const successCount = new Counter('success_200');
const rateLimitedCount = new Counter('rate_limited_429');

export default function () {
  const res = http.get('http://13.235.82.99:8081/api/v1/demo/ping', {
    headers: { 'X-API-Key': 'gwx_live_G6vNd6Zb-SzrPjv7vyYxf8MnCiApAo0_' },
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