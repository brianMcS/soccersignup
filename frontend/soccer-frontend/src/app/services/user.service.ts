import {Inject, Injectable, PLATFORM_ID} from '@angular/core';
import {BehaviorSubject, map, Observable} from 'rxjs';
import {isPlatformBrowser} from '@angular/common';
import { decodeJwtPayload, isJwtUsable } from '../utils/jwt-token';

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
    map(u => !!u?.roles?.includes('ROLE_ADMIN'))
  );
  public isOrganiser$: Observable<boolean> = this.currentUser$.pipe(
    map(u => !!u?.roles?.some(r => r === 'ROLE_ADMIN' || r === 'ROLE_ORGANISER'))
  );
  public isLoggedIn$: Observable<boolean> = this.currentUser$.pipe(
    map(u => u !== null)
  );

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(this.platformId);
    if(this.isBrowser){
      const token = localStorage.getItem('auth_token');
      if(token){
        this.setFromToken(token);
      }
    }
  }

  get currentUser(): CurrentUser | null {
    this.clearExpiredSession();
    return this.currentUserSubject.getValue();
  }

  get isAdmin(): boolean {
    return !!this.currentUser?.roles?.includes('ROLE_ADMIN');
  }

  get isOrganiser(): boolean {
    return !!this.currentUser?.roles?.some(
      r => r === 'ROLE_ADMIN' || r === 'ROLE_ORGANISER');
  }

  get isLoggedIn(): boolean {
    return this.currentUser !== null;
  }

  setFromToken(token: string): boolean {
    const user = this.decodeToken(token);
    this.currentUserSubject.next(user);
    if (!user && this.isBrowser) {
      localStorage.removeItem('auth_token');
    }
    return user !== null;
  }

  clear(): void {
    this.currentUserSubject.next(null);
  }

  private decodeToken(token: string): CurrentUser | null {
    if (!isJwtUsable(token)) {
      return null;
    }

    const decoded = decodeJwtPayload(token);
    if (!decoded) {
      return null;
    }

    const subjectId = Number(decoded.sub);

    return {
      id: decoded.id ?? decoded.playerId
        ?? (Number.isFinite(subjectId) ? subjectId : 0),
      name: decoded.name ?? decoded.sub ?? 'Player',
      email: decoded.email ?? decoded.sub ?? '',
      roles: decoded.roles ?? [],
    };
  }

  private clearExpiredSession(): void {
    if (!this.isBrowser || !this.currentUserSubject.getValue()) {
      return;
    }

    const token = localStorage.getItem('auth_token');
    if (!token || !isJwtUsable(token)) {
      localStorage.removeItem('auth_token');
      this.currentUserSubject.next(null);
    }
  }
}
