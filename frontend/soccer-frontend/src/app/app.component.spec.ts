import { BehaviorSubject } from 'rxjs';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AppComponent } from './app.component';
import { AuthService } from './services/auth.service';
import { NotificationService } from './services/notification.service';
import { UserService } from './services/user.service';

describe('AppComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let notificationService: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'isLoggedIn',
      'logout'
    ]);
    notificationService = jasmine.createSpyObj<NotificationService>(
      'NotificationService',
      ['fetchUnreadCount'],
      { unreadCount: new BehaviorSubject(0).asObservable() }
    );

    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: NotificationService, useValue: notificationService },
        {
          provide: UserService,
          useValue: {
            currentUser$: new BehaviorSubject(null),
            isOrganiser: false
          }
        }
      ]
    }).compileComponents();
  });

  it('creates the application shell', () => {
    const fixture = TestBed.createComponent(AppComponent);

    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.componentInstance.title).toBe('soccer-frontend');
  });

  it('fetches notifications when the user is authenticated', () => {
    authService.isLoggedIn.and.returnValue(true);
    const fixture = TestBed.createComponent(AppComponent);

    fixture.detectChanges();

    expect(notificationService.fetchUnreadCount).toHaveBeenCalled();
  });

  it('does not fetch notifications for a guest', () => {
    authService.isLoggedIn.and.returnValue(false);
    const fixture = TestBed.createComponent(AppComponent);

    fixture.detectChanges();

    expect(notificationService.fetchUnreadCount).not.toHaveBeenCalled();
  });
});
