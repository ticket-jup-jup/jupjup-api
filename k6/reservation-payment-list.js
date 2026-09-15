import http from 'k6/http';
import { check, sleep } from 'k6';
import { login, authHeaders } from './utils/auth.js';

const BASE_URL = 'http://host.docker.internal:8080';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m',  target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m',  target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    },
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const headers = authHeaders(data.token);

    // 예약 목록
    const resv = http.get(`${BASE_URL}/api/reservations/my?page=0&size=20`, {
        headers,
        tags: { name: 'ReservationList' },
    });
    check(resv, { 'reservation list ok': (r) => r.status === 200 });

    // 결제 목록
    const pay = http.get(`${BASE_URL}/api/payments?page=0&size=20`, {
        headers,
        tags: { name: 'PaymentList' },
    });
    check(pay, { 'payment list ok': (r) => r.status === 200 });

    sleep(0.1);
}