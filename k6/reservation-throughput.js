import http from 'k6/http';
import { Counter } from 'k6/metrics';
import { login, authHeaders } from './utils/auth.js';

const BASE_URL = 'http://host.docker.internal:8080';

// SAME=1 → 전원 같은 티켓 (경합 최대)
// SAME=0 → vu마다 다른 티켓 (경합 없음)
const SAME_TICKET = __ENV.SAME === '1';
const BASE_TICKET_ID = 5001;

const success  = new Counter('reserve_success');
const conflict = new Counter('reserve_conflict');
const lockFail = new Counter('reserve_lock_fail');
const other    = new Counter('reserve_other');

export const options = {
    scenarios: {
        load: {
            executor: 'constant-vus',
            vus: 200,
            duration: '1m',
        },
    },
};

export function setup() {
    return { token: login() };
}

export default function (data) {
    const ticketId = SAME_TICKET ? BASE_TICKET_ID : BASE_TICKET_ID + __VU; // vu 마다 다른 티켓

    const res = http.post(`${BASE_URL}/api/reservations`,
        JSON.stringify({ ticketId: ticketId }), {
            headers: authHeaders(data.token),
            tags: { name: 'Reserve' }
        });

    if (res.status === 201) {
        success.add(1);
        return;
    }

    const code = res.json('error.code');
    if (code === 'TICKET_NOT_AVAILABLE') conflict.add(1);
    else if (code === 'LOCK_ACQUISITION_FAILED') lockFail.add(1);
    else other.add(1);
}