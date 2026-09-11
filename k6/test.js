import http from 'k6/http';
import { check, sleep } from 'k6';
import {authHeaders, login} from "./utils/auth.js";

const BASE_URL = 'http://host.docker.internal:8080';

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m',  target: 20 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    },
};

// 테스트 시작 전 한 번만 실행. 반환값이 모든 VU에 전달된다.
export function setup() {
    return { token: login() };
}

export default function (data) {
    const res = http.get(`${BASE_URL}/api/tickets`, {
        headers: authHeaders(data.token),
        tags: { name: 'TicketList'},
    });

    check(res, { 'list ok': (r) => r.status === 200 });
    sleep(0.1);
}