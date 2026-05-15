import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {PlayerService} from '../../services/player.service';
import {HttpClientModule} from '@angular/common/http';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  form = {name: '', email: '', phone: ''};
  successMessage = '';
  errorMessage:  string | null = null;
  isLoggedIn = false;

  constructor(
    private playerService: PlayerService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    // Check if already logged in
    this.isLoggedIn = this.authService.hasToken();
    this.authService.isLoggedIn$.subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
    });
  }

  onGoogleLogin(){
    this.authService.loginWithGoogle();
  }

  onSubmit() {
    this.successMessage = '';
    this.errorMessage = null;

    this.playerService.registerPlayer(this.form).subscribe({
      next: () => {
        this.successMessage = 'Registration successful! You can now join a game.';
        this.form = { name: '', email: '', phone: '' };
    },
    error: (err) => {
        if (err.error && typeof  err.error === 'object'){
          const messages = Object.values(err.error) as string[];
          this.errorMessage = messages[0];
        } else {
          this.errorMessage = 'Registration failed. Please try again.'
        }
      }
    });
  }
}
