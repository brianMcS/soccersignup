import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-game-signup-list',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],

  templateUrl: './game-signup-list.component.html',
  styleUrl: './game-signup-list.component.css'
})
export class GameSignupListComponent implements OnInit {
  slots: { playerName?: string; email?: string; phone?: string }[] = [];
  formVisible = false;
  joiningSlotIndex: number | null = null;

  form = {
    playerName: '',
    email: '',
    phone: ''
  };

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchSlots();
  }

  fetchSlots() {
    this.http.get<any[]>('http://localhost:8080/gameslots').subscribe({
      next: (data) => {
        // Fill in the 18 slots, even if empty
        this.slots = Array(18).fill({}).map((_, i) => data[i] || {});
      },
      error: (err) => console.error('Error fetching game slots', err)
    });
  }

  showJoinForm(index: number) {
    this.joiningSlotIndex = index;
    this.formVisible = true;
  }

  cancelJoin() {
    this.formVisible = false;
    this.form = { playerName: '', email: '', phone: '' };
  }

  submitForm() {
    const today = new Date();
    const gameSlot = {
      ...this.form,
      date: this.getUpcomingThursday(today).toISOString().split('T')[0],
      timestamp: new Date().toISOString()
    };

    this.http.post('http://localhost:8080/gameslots', gameSlot).subscribe({
      next: () => {
        this.fetchSlots();
        this.cancelJoin();
      },
      error: () => alert('Failed to join. Try again.')
    });
  }

  getUpcomingThursday(date: Date): Date {
    const day = date.getDay();
    const diff = (4 - day + 7) % 7;
    date.setDate(date.getDate() + diff);
    return date;
  }
}
