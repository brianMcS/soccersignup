import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    localStorage.clear();
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        { provide: Router, useValue: router }
      ]
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('opens Google sign-in using a relative URL', () => {
    const popup = {} as Window;
    const openSpy = spyOn(window, 'open').and.returnValue(popup);

    service.loginWithGoogle();

    expect(openSpy).toHaveBeenCalledWith(
      '/oauth2/authorization/google',
      'OAuth2Login',
      jasmine.any(String)
    );
  });

  it('accepts an OAuth token only from the popup on the current origin', () => {
    const popup = {} as Window;
    spyOn(window, 'open').and.returnValue(popup);
    service.loginWithGoogle();
    spyOn(service, 'setToken');

    window.dispatchEvent(new MessageEvent('message', {
      origin: window.location.origin,
      source: popup,
      data: { success: true, token: 'signed-token' }
    }));

    expect(service.setToken).toHaveBeenCalledWith('signed-token');
    expect(router.navigate).toHaveBeenCalledWith(['/play']);
  });

  it('ignores OAuth messages from a different window', () => {
    const popup = {} as Window;
    spyOn(window, 'open').and.returnValue(popup);
    service.loginWithGoogle();
    spyOn(service, 'setToken');

    window.dispatchEvent(new MessageEvent('message', {
      origin: window.location.origin,
      source: {} as Window,
      data: { success: true, token: 'stolen-token' }
    }));

    expect(service.setToken).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
