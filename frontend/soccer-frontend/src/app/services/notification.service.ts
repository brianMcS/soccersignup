import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import { Notification as PlayerNotification, UnreadCount } from '../models/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private readonly baseUrl = '/api/notifications';

  // Shared state — any component can subscribe to this
  // The nav bell subscribes to show the badge count
  private unreadCount$ = new BehaviorSubject<number>(0);
  readonly unreadCount = this.unreadCount$.asObservable();

  constructor(private http: HttpClient) {}

  fetchUnreadCount(): void {
    this.http.get<UnreadCount>(`${this.baseUrl}/unread-count`).subscribe({
      next: (res) => this.unreadCount$.next(res.count),
      error: () => this.unreadCount$.next(0)
    });
  }

  getNotifications(): Observable<PlayerNotification[]> {
    return this.http.get<PlayerNotification[]>(this.baseUrl);
  }

  markAsRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/read`, {}).pipe(
      tap(() => {
        // Decrement the local count immediately
        // No need to re-fetch from server
        const current = this.unreadCount$.value;
        if (current > 0) this.unreadCount$.next(current - 1);
      })
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/read-all`, {}).pipe(
      tap(() => this.unreadCount$.next(0))
    );
  }
}
