import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {PlayerService} from '../../services/player.service';
import {HttpClientModule} from '@angular/common/http';

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

  constructor(private playerService: PlayerService) {}

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
