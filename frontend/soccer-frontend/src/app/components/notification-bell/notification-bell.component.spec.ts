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
});
