import {Component, OnDestroy, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {CurrentUser, UserService} from '../../services/user.service';
import {Subscription} from 'rxjs';
import {Router} from '@angular/router';

type View = 'landing' | 'register';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit, OnDestroy {

  // ─── State ────────────────────────────────────────────────────────────────
  currentUser: CurrentUser | null = null;
  view: View = 'landing';       // 'landing' = sign-in CTA, 'register' = show form
  googleLoading = false;
  passwordLoginLoading = false;
  devLoginLoading = false;
  selectedDevEmail = 'player1@test.local';
  showDevLogin = false;

  readonly devUsers = [
    { label: 'Player 1 - Sanju Baskaran', email: 'player1@test.local' },
    { label: 'Player 2 - Sylwester Swed', email: 'player2@test.local' },
    { label: 'Player 3 - Peter Parker', email: 'player3@test.local' },
    { label: 'Player 4 - John Doe', email: 'player4@test.local' },
    { label: 'Player 5 - Jane Smith', email: 'player5@test.local' },
    { label: 'Player 6 - Bruce Wayne', email: 'player6@test.local' },
    { label: 'Player 7 - Clark Kent', email: 'player7@test.local' },
    { label: 'Player 8 - Diana Prince', email: 'player8@test.local' },
    { label: 'Player 9 - Tony Stark', email: 'player9@test.local' },
    { label: 'Player 10 - Steve Rogers', email: 'player10@test.local' },
    { label: 'Player 11 - Natasha Romanoff', email: 'player11@test.local' },
    { label: 'Player 12 - Clint Barton', email: 'player12@test.local' },
    { label: 'Player 13 - Bruce Banner', email: 'player13@test.local' },
    { label: 'Player 14 - Thor Odinson', email: 'player14@test.local' },
    { label: 'Player 15 - Scott Lang', email: 'player15@test.local' },
    { label: 'Player 16 - Sam Wilson', email: 'player16@test.local' },
    { label: 'Player 17 - Bucky Barnes', email: 'player17@test.local' },
    { label: 'Player 18 - Stephen Strange', email: 'player18@test.local' },
    { label: 'Player 19 - Wanda Maximoff', email: 'player19@test.local' },
    { label: 'Player 20 - Vision', email: 'player20@test.local' },

    { label: 'Organiser - Test Organiser', email: 'organiser@test.local' },
    { label: 'Admin - Test Admin', email: 'admin@test.local' },
  ];

  loginForm = { email: '', password: '' };
  form = { name: '', email: '', phone: '', password: '', confirmPassword: '' };
  submitting  = false;
  errorMessage: string | null = null;

  private subs = new Subscription();

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.showDevLogin = typeof window !== 'undefined'
      && ['localhost', '127.0.0.1'].includes(window.location.hostname);

    this.subs.add(
      this.userService.currentUser$.subscribe(u => {
        this.currentUser = u;
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // ─── Auth ─────────────────────────────────────────────────────────────────
  onGoogleLogin(): void {
    this.googleLoading = true;
    this.authService.loginWithGoogle();
    setTimeout(() => this.googleLoading = false, 1500);
  }

  onPasswordLogin(): void {
    this.passwordLoginLoading = true;
    this.errorMessage = null;

    this.authService.loginWithPassword(
      this.loginForm.email,
      this.loginForm.password
    ).subscribe({
      next: (response) => {
        this.passwordLoginLoading = false;
        this.completeAuthentication(response);
      },
      error: (err) => {
        this.passwordLoginLoading = false;
        this.errorMessage = this.getErrorMessage(
          err,
          'Sign in failed. Check your email and password.');
      }
    });
  }

  onDevLogin(): void {
    this.devLoginLoading = true;
    this.errorMessage = null;

    this.authService.loginAsDevUser(this.selectedDevEmail).subscribe({
      next: (response) => {
        this.devLoginLoading = false;
        if (response.success && response.token) {
          this.authService.setToken(response.token);
          this.router.navigate(['/play']);
        } else {
          this.errorMessage = response.error || 'Dev login failed. Please try another test user.';
        }
      },
      error: () => {
        this.devLoginLoading = false;
        this.errorMessage = 'Dev login is only available when the backend is running with the dev or local profile.';
      }
    });
  }

  goToPlay(): void {
    this.router.navigate(['/play']);
  }

  logout(): void {
    this.authService.logout();
  }

  // ─── View switching ───────────────────────────────────────────────────────
  showRegisterForm(): void {
    this.view = 'register';
    this.errorMessage = null;
  }

  backToLanding(): void {
    this.view = 'landing';
    this.errorMessage = null;
    this.form = { name: '', email: '', phone: '', password: '', confirmPassword: '' };
  }

  // ─── Registration form ────────────────────────────────────────────────────
  onSubmit(): void {
    if (this.form.password !== this.form.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.submitting  = true;
    this.errorMessage = null;

    this.authService.register({
      name: this.form.name,
      email: this.form.email,
      phone: this.form.phone,
      password: this.form.password
    }).subscribe({
      next: (response) => {
        this.submitting = false;
        this.completeAuthentication(response);
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = this.getErrorMessage(
          err,
          'Registration failed. Please try again.');
      }
    });
  }

  private completeAuthentication(response: { success: boolean; token?: string; error?: string }): void {
    if (response.success && response.token) {
      this.authService.setToken(response.token);
      this.router.navigate(['/play']);
      return;
    }
    this.errorMessage = response.error ?? 'Authentication failed. Please try again.';
  }

  private getErrorMessage(error: any, fallback: string): string {
    if (typeof error?.error?.error === 'string') {
      return error.error.error;
    }
    if (error?.error && typeof error.error === 'object') {
      const validationMessages = Object.values(error.error)
        .filter(value => typeof value === 'string') as string[];
      if (validationMessages.length > 0) {
        return validationMessages[0];
      }
    }
    if (typeof error?.error === 'string') {
      return error.error;
    }
    return fallback;
  }

  // Re-used in template: check if a form field has a specific error
  hasError(field: any, error: string): boolean {
    return field?.invalid && field?.touched && field?.errors?.[error];
  }
}
