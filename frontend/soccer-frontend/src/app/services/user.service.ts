import {Inject, Injectable, PLATFORM_ID} from '@angular/core';
import {BehaviorSubject, map, Observable} from 'rxjs';
import {isPlatformBrowser} from '@angular/common';

export interface CurrentUser {
  id: number;
  name: string;
  email: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private isBrowser: boolean;
  private currentUserSubject = new BehaviorSubject<CurrentUser | null>(null);

  public currentUser$: Observable<CurrentUser | null> = this.currentUserSubject.asObservable();
  public isAdmin$: Observable<boolean> = this.currentUser$.pipe(
    map(u => !!u?.roles?.some(r => r === 'ROLE_ADMIN' || r === 'ROLE_ORGANIZER'))
  );
  public isLoggedIn$: Observable<boolean> = this.currentUser$.pipe(
    map(u => u !== null)
  );

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    if(this.isBrowser){
      const token = localStorage.getItem('auth_token');
      if(token){
        const user = this.decodeToken(token);
        this.currentUserSubject.next(user);
      }
    }
  }

  get currentUser(): CurrentUser | null {
    return this.currentUserSubject.getValue();
  }

  get isAdmin(): boolean {
    return !!this.currentUser?.roles?.some(
      r => r === 'ROLE_ADMIN' || r === 'ROLE_ORGANISER'
    );
  }

  get isLoggedIn(): boolean {
    return this.currentUser !== null;
  }

  setFromToken(token: string): void {
    const user = this.decodeToken(token);
    this.currentUserSubject.next(user);
  }

  clear(): void {
    this.currentUserSubject.next(null);
  }

  private decodeToken(token: string): CurrentUser | null {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));

      return {
        id:    decoded.id    ?? decoded.playerId ?? 0,
        name:  decoded.name  ?? decoded.sub      ?? 'Player',
        email: decoded.email ?? decoded.sub      ?? '',
        roles: decoded.roles ?? [],
      };
    } catch {
      console.warn('UserService: could not decode JWT');
      return null;
    }
  }
}
