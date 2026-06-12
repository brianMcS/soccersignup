import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { authInterceptor } from './auth.interceptor.service';
import { UserService } from './user.service';
import { HttpClient } from '@angular/common/http';

function tokenFor(payload: object): string {
  const encoded = btoa(JSON.stringify(payload))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
  return `header.${encoded}.signature`;
}

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let router: jasmine.SpyObj<Router>;
  let userService: jasmine.SpyObj<UserService>;

  beforeEach(() => {
    localStorage.clear();
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    userService = jasmine.createSpyObj<UserService>('UserService', ['clear']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
        { provide: UserService, useValue: userService }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('adds a valid token to API requests', () => {
    const token = tokenFor({
      exp: Math.floor(Date.now() / 1000) + 3600
    });
    localStorage.setItem('auth_token', token);

    http.get('/api/games').subscribe();

    const request = httpTesting.expectOne('/api/games');
    expect(request.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    request.flush([]);
  });

  it('removes an expired token instead of sending it', () => {
    localStorage.setItem('auth_token', tokenFor({
      exp: Math.floor(Date.now() / 1000) - 1
    }));

    http.get('/api/games').subscribe();

    const request = httpTesting.expectOne('/api/games');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(userService.clear).toHaveBeenCalled();
    request.flush([]);
  });

  it('clears storage and in-memory state after a 401 response', () => {
    const token = tokenFor({
      exp: Math.floor(Date.now() / 1000) + 3600
    });
    localStorage.setItem('auth_token', token);

    http.get('/api/games').subscribe({ error: () => undefined });

    httpTesting.expectOne('/api/games').flush(
      { message: 'Authentication is required' },
      { status: 401, statusText: 'Unauthorized' }
    );

    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(userService.clear).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/register']);
  });
});
