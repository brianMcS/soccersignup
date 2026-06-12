import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { OAuthRedirectComponent } from './o-auth-redirect.component';
import { AuthService } from '../../services/auth.service';

describe('OauthRedirectComponent', () => {
  let component: OAuthRedirectComponent;
  let fixture: ComponentFixture<OAuthRedirectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OAuthRedirectComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}) }
        },
        {
          provide: AuthService,
          useValue: jasmine.createSpyObj<AuthService>('AuthService', ['setToken'])
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OAuthRedirectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
