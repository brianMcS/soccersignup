import {Component, OnDestroy, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {PlayerService} from '../../services/player.service';
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

  form = { name: '', email: '', phone: '' };
  submitting  = false;
  submitted   = false;           // true after successful registration
  errorMessage: string | null = null;

  private subs = new Subscription();

  constructor(
    private playerService: PlayerService,
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
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
    this.form = { name: '', email: '', phone: '' };
  }

  // ─── Registration form ────────────────────────────────────────────────────
  onSubmit(): void {
    this.submitting  = true;
    this.errorMessage = null;

    this.playerService.registerPlayer(this.form).subscribe({
      next: () => {
        this.submitted  = true;
        this.submitting = false;
        this.form = { name: '', email: '', phone: '' };
    },
    error: (err) => {
        this.submitting = false;
        if (err.error && typeof  err.error === 'object'){
          const messages = Object.values(err.error) as string[];
          this.errorMessage = messages[0];
        } else if (typeof err.error === 'string') {
          this.errorMessage = err.error;
        } else {
          this.errorMessage = 'Registration failed. Please try again.'
        }
      }
    });
  }

  // Re-used in template: check if a form field has a specific error
  hasError(field: any, error: string): boolean {
    return field?.invalid && field?.touched && field?.errors?.[error];
  }
}
