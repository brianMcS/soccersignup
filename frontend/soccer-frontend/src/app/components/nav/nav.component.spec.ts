import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
import { provideRouter } from '@angular/router';

import { NavComponent } from './nav.component';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../services/notification.service';

describe('NavComponent', () => {
  let component: NavComponent;
  let fixture: ComponentFixture<NavComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: jasmine.createSpyObj<AuthService>('AuthService', ['logout'])
        },
        {
          provide: UserService,
          useValue: {
            currentUser$: new BehaviorSubject(null),
            isOrganiser: false
          }
        },
        {
          provide: NotificationService,
          useValue: jasmine.createSpyObj<NotificationService>(
            'NotificationService',
            ['fetchUnreadCount'],
            { unreadCount: new BehaviorSubject(0).asObservable() })
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
