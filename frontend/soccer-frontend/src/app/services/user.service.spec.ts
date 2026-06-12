import { TestBed } from '@angular/core/testing';

import { UserService } from './user.service';

function tokenFor(payload: object): string {
  const encoded = btoa(JSON.stringify(payload))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
  return `header.${encoded}.signature`;
}

describe('UserService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
  });

  it('hydrates the current user from a valid stored token', () => {
    localStorage.setItem('auth_token', tokenFor({
      sub: '42',
      name: 'Alex Morgan',
      email: 'alex@example.com',
      roles: ['ROLE_PLAYER'],
      exp: Math.floor(Date.now() / 1000) + 3600
    }));

    const service = TestBed.inject(UserService);

    expect(service.currentUser).toEqual({
      id: 42,
      name: 'Alex Morgan',
      email: 'alex@example.com',
      roles: ['ROLE_PLAYER']
    });
    expect(service.isLoggedIn).toBeTrue();
  });

  it('removes an expired stored token', () => {
    localStorage.setItem('auth_token', tokenFor({
      sub: '42',
      exp: Math.floor(Date.now() / 1000) - 1
    }));

    const service = TestBed.inject(UserService);

    expect(service.currentUser).toBeNull();
    expect(localStorage.getItem('auth_token')).toBeNull();
  });

  it('clears an active user once the token expires', () => {
    const now = Date.now();
    const token = tokenFor({
      sub: '42',
      roles: ['ROLE_ADMIN'],
      exp: Math.floor(now / 1000) + 60
    });
    localStorage.setItem('auth_token', token);
    const service = TestBed.inject(UserService);
    spyOn(Date, 'now').and.returnValue(now + 61_000);

    expect(service.isAdmin).toBeFalse();
    expect(service.currentUser).toBeNull();
    expect(localStorage.getItem('auth_token')).toBeNull();
  });
});
