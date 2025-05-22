import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-game-signup-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],

  templateUrl: './game-signup-list.component.html',
  styleUrl: './game-signup-list.component.css'
})
export class GameSignupListComponent {
  form = {
    playerName: '',
    email: '',
    phone: ''
  };

  successMessage = '';
  errorMessage = '';

  constructor(private http: HttpClient) {}

  onSubmit() {
    const today = new Date();
    const currentThursday = this.getUpcomingThursday(today);
    const gameSlot = {
      ...this.form,
      date: currentThursday.toISOString().split('T')[0],
      timestamp: new Date().toISOString()
    };

    this.http.post('http://localhost:8080/gameslots', gameSlot).subscribe({
      next: () => {
        this.successMessage = 'Signed up successfully!';
        this.errorMessage = '';
        this.form = { playerName: '', email: '', phone: '' };
      },
      error: () => {
        this.errorMessage = 'Failed to sign up. Try again later.';
        this.successMessage = '';
      }
    });
  }

  getUpcomingThursday(date: Date): Date {
    const day = date.getDay(); // Sunday = 0 ... Saturday = 6
    const diff = (4 - day + 7) % 7; // 4 = Thursday
    date.setDate(date.getDate() + diff);
    return date;
  }
}