import http from 'k6/http';
import { Counter } from 'k6/metrics';
import { login, authHeaders } from './utils/auth.js';

const BASE_URL = 'http://host.docker.internal:8080';
const TICKET_ID = 5001;

const success  = new Counter('reserve_success');
const conflict = new Counter('reserve_conflict');
const lockFail = new Counter('reserve_lock_fail');
const other    = new Counter('reserve_other');

export const options = {
    scenarios: {
        rush: {
            executor: 'per-vu-iterations',
            vus: 200,
            iterations: 1,
        },
    },
};

export function setup() {
    return { token: login() }
}

export default function (data) {
    const headers = authHeaders(data.token);

    const res = http.post(`${BASE_URL}/api/reservations`,
        JSON.stringify({ ticketId: TICKET_ID }), {
        headers: authHeaders(data.token),
        tags: { name: 'Reserve' }
    });

    if (res.status === 201) {
        success.add(1);
        return;
    }

    const code = res.json('error.code')
    if (code === 'TICKET_NOT_AVAILABLE') conflict.add(1);
    else if (code === 'LOCK_ACQUISITION_FAILED') lockFail.add(1);
    else {
        other.add(1);
        console.log(`unexpected: ${res.status} / ${res.body}`);
    }
}
