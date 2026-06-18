import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, Subject } from 'rxjs';
import { provideRouter } from '@angular/router';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let oauthErrorSubject: Subject<string>;
  let authService: jasmine.SpyObj<AuthService> & { oauthError$: Subject<string> };

  beforeEach(async () => {
    oauthErrorSubject = new Subject<string>();
    const authServiceSpy = jasmine.createSpyObj<AuthService>(
      'AuthService',
      ['loginWithGoogle', 'logout']);
    authService = authServiceSpy as jasmine.SpyObj<AuthService> & { oauthError$: Subject<string> };
    authService.oauthError$ = oauthErrorSubject;

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: authService
        },
        {
          provide: UserService,
          useValue: { currentUser$: new BehaviorSubject(null) }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows OAuth errors inline and stops the Google loading state', () => {
    component.googleLoading = true;

    oauthErrorSubject.next('Popup blocked. Please allow popups for this site and try again.');
    fixture.detectChanges();

    expect(component.googleLoading).toBeFalse();
    expect(component.errorMessage).toBe('Popup blocked. Please allow popups for this site and try again.');
    expect(fixture.nativeElement.textContent).toContain('Popup blocked');
  });
});
