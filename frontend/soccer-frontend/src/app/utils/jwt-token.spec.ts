import { decodeJwtPayload, isJwtUsable } from './jwt-token';

function tokenFor(payload: object): string {
  const encoded = btoa(JSON.stringify(payload))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
  return `header.${encoded}.signature`;
}

describe('JWT token utilities', () => {
  it('decodes a base64url JWT payload', () => {
    const payload = decodeJwtPayload(tokenFor({
      sub: '42',
      email: 'player@example.com',
      exp: 200
    }));

    expect(payload?.sub).toBe('42');
    expect(payload?.email).toBe('player@example.com');
  });

  it('accepts only tokens with a future expiry', () => {
    expect(isJwtUsable(tokenFor({ exp: 101 }), 100_000)).toBeTrue();
    expect(isJwtUsable(tokenFor({ exp: 100 }), 100_000)).toBeFalse();
    expect(isJwtUsable(tokenFor({}), 100_000)).toBeFalse();
  });

  it('rejects malformed tokens', () => {
    expect(decodeJwtPayload('not-a-token')).toBeNull();
    expect(isJwtUsable('not-a-token')).toBeFalse();
  });
});
