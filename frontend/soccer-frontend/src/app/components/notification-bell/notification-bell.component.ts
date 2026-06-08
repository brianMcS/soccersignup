import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import { Notification as PlayerNotification } from '../../models/notification.model';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.component.html',
  styleUrl: './notification-bell.component.css'
})
export class NotificationBellComponent implements OnInit, OnDestroy {

  unreadCount = 0;
  notifications: PlayerNotification[] = [];
  dropdownOpen = false;
  loading = false;

  private subs = new Subscription();

  constructor(
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.subs.add(
      this.notificationService.unreadCount.subscribe(
        count => this.unreadCount = count
      )
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  toggleDropdown(): void {
    if (this.dropdownOpen) {
      this.closeDropdown();
      return;
    }
    this.openDropdown();
  }

  openDropdown(): void {
    this.dropdownOpen = true;
    this.loading = true;

    this.notificationService.getNotifications().subscribe({
      next: (notifications: PlayerNotification[]) => {
        this.notifications = notifications;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  closeDropdown(): void {
    this.dropdownOpen = false;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.bell-wrap')) {
      this.dropdownOpen = false;
    }
  }

  // ← this was the remaining problem — Notification changed to PlayerNotification
  onNotificationClick(notification: PlayerNotification): void {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.read = true;
        }
      });
    }

    this.closeDropdown();

    if (notification.link) {
      this.router.navigate([notification.link]);
    }
  }

  markAllRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
      }
    });
  }

  formatTime(dateStr: string): string {
    try {
      const date = new Date(dateStr);
      const now = new Date();
      const diffMs = now.getTime() - date.getTime();
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMins / 60);
      const diffDays = Math.floor(diffHours / 24);

      if (diffMins < 1) return 'just now';
      if (diffMins < 60) return `${diffMins}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      return `${diffDays}d ago`;
    } catch {
      return '';
    }
  }
}
