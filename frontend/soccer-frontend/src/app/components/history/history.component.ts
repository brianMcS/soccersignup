import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {Game} from '../../models/game.model';
import {GamesService} from '../../services/games.service';
import { getApiErrorMessage } from '../../utils/api-error';
import { parseDateOnly } from '../../utils/date-only';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './history.component.html',
  styleUrl: './history.component.css'
})
export class HistoryComponent {

  loading = false;
  games: Game[] = [];
  errorMessage: string | null = null;

  constructor(private gamesService: GamesService) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    this.loading = true;
    this.errorMessage = null;

    this.gamesService.getCompletedGames().subscribe({
      next: (games) => {
        this.games = games.sort((a, b) =>
          parseDateOnly(b.gameDate).getTime() - parseDateOnly(a.gameDate).getTime()
        );
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = getApiErrorMessage(
          error,
          'Could not load game history. Please try again.');
        this.loading = false;
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    try {
      return parseDateOnly(dateStr).toLocaleDateString('en-IE', {
        weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
      });
    } catch { return dateStr; }
  }

  formatTime(timeStr: string): string {
    if (!timeStr) return '—';
    try {
      const [h, m] = timeStr.split(':').map(Number);
      const d = new Date();
      d.setHours(h, m);
      return d.toLocaleTimeString('en-IE', { hour: 'numeric', minute: '2-digit', hour12: true });
    } catch { return timeStr; }
  }

  attendancePercent(game: any): number {
    if (!game.maxPlayers) return 0;
    return Math.round(((game.confirmedCount ?? 0) / game.maxPlayers) * 100);
  }
}
