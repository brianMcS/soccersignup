import {Inject, Injectable, NgZone, PLATFORM_ID} from '@angular/core';
import { Router } from '@angular/router';
import {BehaviorSubject} from 'rxjs';
import {isPlatformBrowser} from '@angular/common';

export interface AuthResponse {
  success: boolean;
  token?: string;
  player?: {
    id: number;
    email: string;
    name: string;
    roles: string[];
  };
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isBrowser: boolean;
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();

  private oauthPopup: Window | null = null;

  constructor(
    private router: Router,
    private ngZone: NgZone,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);

    // initialize BehaviorSubject safely (do not access localStorage on server)
    const initial = this.isBrowser ? !!localStorage.getItem('auth_token') : false;
    this.isLoggedInSubject = new BehaviorSubject<boolean>(initial);
    this.isLoggedIn$ = this.isLoggedInSubject.asObservable();

    // Only set up postMessage listener in browser
    if (this.isBrowser) {
      this.setupOAuthListener();
    }
  }

  getToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem('auth_token');
  }

  hasToken(): boolean {
    return !!this.getToken();
  }

  setToken(token: string) {
    if (this.isBrowser) {
      localStorage.setItem('auth_token', token);
      this.isLoggedInSubject.next(true);
    } else {
      // running on server - do nothing
    }
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout() {
    if (this.isBrowser) {
      localStorage.removeItem('auth_token');
    }
    this.isLoggedInSubject.next(false);
    if (this.isBrowser) {
      this.router.navigate(['/register']);
    }
  }

  loginWithGoogle(): void {
    if (!this.isBrowser) {
      console.warn('loginWithGoogle called on non-browser platform');
      return;
    }

    const width = 500;
    const height = 650;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;

    this.oauthPopup = window.open(
      'http://localhost:8080/oauth2/authorization/google',
      'OAuth2Login',
      `width=${width},height=${height},left=${left},top=${top}`
    );

    if (!this.oauthPopup) {
      alert('Popup blocked. Please allow popups for this site.');
    }
  }

  private setupOAuthListener(): void {
    // Add event listener in browser only
    if (!this.isBrowser) return;

    // Use NgZone to ensure Angular change detection runs when needed
    window.addEventListener('message', (event: MessageEvent) => {
      // IMPORTANT: verify origin in production — don't use '*' in production
      try {
        if (event.origin !== 'http://localhost:8080') {
          // ignore messages from other origins
          return;
        }

        const data = event.data as AuthResponse;
        if (data && data.success && data.token) {
          this.ngZone.run(() => {
            this.setToken(data.token!);
            this.router.navigate(['/play']);
          });
        } else if (data && !data.success && data.error) {
          this.ngZone.run(() => {
            alert('Authentication failed: ' + data.error);
          });
        }
      } catch (e) {
        console.error('Error handling OAuth message', e);
      }
    }, false);
  }

  // fallback for non-popup flows: called by OAuth redirect component
  handleOAuthCallback(token: string) {
    if (this.isBrowser && token) {
      this.setToken(token);
      this.router.navigate(['/play']);
    }
  }
}
