import {Inject, Injectable, NgZone, PLATFORM_ID} from '@angular/core';
import { Router } from '@angular/router';
import {isPlatformBrowser} from '@angular/common';
import {UserService} from './user.service';

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

@Injectable({ providedIn: 'root' })
export class AuthService {
  private isBrowser: boolean;
  private oauthPopup: Window | null = null;

  public isLoggedIn$!: typeof this.userService.isLoggedIn$;

  constructor(
    private router: Router,
    private ngZone: NgZone,
    private userService: UserService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isLoggedIn$ = this.userService.isLoggedIn$;
    this.isBrowser = isPlatformBrowser(this.platformId);
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

  isLoggedIn(): boolean {
    return !!this.userService.isLoggedIn$;
  }

  setToken(token: string): void {
    if (!this.isBrowser) return;
    localStorage.setItem('auth_token', token);
    this.userService.setFromToken(token);
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem('auth_token');
    }
    this.userService.clear();
    this.router.navigate(['/']);
  }

  loginWithGoogle(): void {
    if (!this.isBrowser) return;

    const width = 500;
    const height = 650;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;

    this.oauthPopup = window.open(
      // 'http://localhost:8080/oauth2/authorization/google',
      '/oauth2/authorization/google',   // relative — no hardcoded localhost
      'OAuth2Login',
      `width=${width},height=${height},left=${left},top=${top}`
    );

    if (!this.oauthPopup) {
      alert('Popup blocked. Please allow popups for this site.');
    }
  }

  handleOAuthCallback(token: string): void {
    if(this.isBrowser && token) {
      this.setToken(token);
      this.router.navigate(['/play']);
    }
  }

  private setupOAuthListener(): void {
    window.addEventListener('message', (event: MessageEvent) => {
      // In production, lock this to your real domain
      if (event.origin !== window.location.origin &&
        event.origin !== 'http://localhost:8080') return;

      try {
        const data = event.data as AuthResponse;
        if (data?.success && data.token) {
          this.ngZone.run(() => {
            this.setToken(data.token!);
            this.router.navigate(['/play']);
          });
        } else if (data && !data.success && data.error) {
          this.ngZone.run(() => alert('Authentication failed: ' + data.error));
        }
      } catch (e) {
        console.error('Error handling OAuth message', e);
      }
    }, false);
  }
}
