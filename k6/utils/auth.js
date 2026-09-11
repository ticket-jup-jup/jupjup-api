import http from 'k6/http';

const BASE_URL = 'http://host.docker.internal:8080';

export function login() {
    const res = http.post(`${BASE_URL}/api/auth/signin`, JSON.stringify({
        email: 'load-test@test.com',
        password: 'password123',
    }), { headers: { 'Content-Type': 'application/json' } });

    if (res.status !== 200) {
        throw new Error(`로그인 실패: ${res.status} ${res.body}`);
    }
    return res.json('data.0.accessToken');
}

export function authHeaders(token) {
    return {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
    };
}