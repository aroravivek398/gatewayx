import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '30s',
};

export default function () {
  const res = http.get('http://13.235.82.99:8081/api/v1/demo/ping', {
    headers: { 'X-API-Key': 'gwx_live_G6vNd6Zb-SzrPjv7vyYxf8MnCiApAo0_' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(0.1);
}