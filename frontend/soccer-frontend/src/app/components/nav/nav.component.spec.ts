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

  it('only renders the mobile dialog while the menu is open', () => {
    expect(fixture.nativeElement.querySelector('[role="dialog"]')).toBeNull();

    component.toggleMenu();
    fixture.detectChanges();

    const dialog: HTMLElement =
      fixture.nativeElement.querySelector('[role="dialog"]');
    expect(dialog).not.toBeNull();
    expect(dialog.getAttribute('aria-modal')).toBe('true');
  });

  it('closes on Escape and restores focus to the menu button', (done) => {
    const menuButton: HTMLButtonElement =
      fixture.nativeElement.querySelector('.nav-hamburger');
    component.toggleMenu();
    fixture.detectChanges();

    component.onEscape();
    fixture.detectChanges();

    setTimeout(() => {
      expect(component.menuOpen).toBeFalse();
      expect(document.activeElement).toBe(menuButton);
      done();
    });
  });
});
