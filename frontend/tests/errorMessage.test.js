import test from 'node:test';
import assert from 'node:assert/strict';
import { errMsg } from '../src/services/errorMessage.js';

test('uses a backend validation message', () => {
  assert.equal(errMsg({ response: { status: 400, data: { message: 'Enter a valid phone number' } } }), 'Enter a valid phone number');
});

test('reads JSON returned as a string', () => {
  assert.equal(errMsg({ response: { status: 401, data: '{"message":"Invalid email or password"}' } }), 'Invalid email or password');
});

test('uses clear status guidance when response has no message', () => {
  assert.equal(errMsg({ response: { status: 401, data: '<html>Unauthorized</html>' } }), 'Your email or password is incorrect, or your session has expired.');
});

test('does not expose an HTML error page', () => {
  assert.equal(errMsg({ response: { status: 502, data: '<html>Bad Gateway</html>' } }), 'The server could not complete the request. Please try again shortly.');
});

test('distinguishes an unreachable backend', () => {
  assert.equal(errMsg({ code: 'ERR_NETWORK' }), 'Cannot reach the server. Is the backend running?');
});

test('reports request timeouts', () => {
  assert.equal(errMsg({ code: 'ECONNABORTED' }), 'The request timed out. Please try again.');
});
