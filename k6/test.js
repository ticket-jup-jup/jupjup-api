import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://host.docker.internal:8080';

export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m',  target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m',  target: 100 },
        { duration: '30s', target: 200 },
        { duration: '1m',  target: 200 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    },
};

// 테스트 시작 전 한 번만 실행. 반환값이 모든 VU에 전달된다.
export function setup() {
    const res = http.post(`${BASE_URL}/api/auth/signin`, JSON.stringify({
        email: 'dookie289@gmail.com',
        password: 'password123',
    }), { headers: { 'Content-Type': 'application/json' } });

    if (res.status !== 200) {
        throw new Error(`로그인 실패: ${res.status} ${res.body}`);
    }

    return { token: res.json('data.0.accessToken') };
}

export default function (data) {
    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
            'Content-Type': 'application/json',
        },
    };

    const res = http.get(`${BASE_URL}/api/tickets`, {
        ...params,
        tags: { name: 'TicketList' },     // ← 중요 (3-1 참고)
    });

    check(res, { 'list ok': (r) => r.status === 200 });
    sleep(0.1);
}