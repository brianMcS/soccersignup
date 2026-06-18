import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of } from 'rxjs';
import { provideRouter } from '@angular/router';

import { NotificationBellComponent } from './notification-bell.component';
import { NotificationService } from '../../services/notification.service';

describe('NotificationBellComponent', () => {
  let component: NotificationBellComponent;
  let fixture: ComponentFixture<NotificationBellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificationBellComponent],
      providers: [
        provideRouter([]),
        {
          provide: NotificationService,
          useValue: {
            unreadCount: new BehaviorSubject(0).asObservable(),
            getNotifications: () => of([]),
            markAsRead: () => of(void 0),
            markAllAsRead: () => of(void 0)
          }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NotificationBellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders notifications as keyboard-operable buttons', () => {
    component.notifications = [{
      id: 1,
      message: 'Teams published',
      read: false,
      createdAt: new Date().toISOString()
    }];
    component.dropdownOpen = true;
    component.loading = false;
    fixture.detectChanges();

    const action: HTMLButtonElement =
      fixture.nativeElement.querySelector('.notif-action');

    expect(action.tagName).toBe('BUTTON');
    expect(action.getAttribute('aria-label')).toContain('Teams published');
  });

  it('closes on Escape and restores focus to the bell button', (done) => {
    const bellButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.bell-btn');
    component.dropdownOpen = true;
    fixture.detectChanges();

    component.onEscape();

    setTimeout(() => {
      expect(component.dropdownOpen).toBeFalse();
      expect(document.activeElement).toBe(bellButton);
      done();
    });
  });
});
