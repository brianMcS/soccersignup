import { Component, OnInit } from '@angular/core';
import { CommonModule }  from '@angular/common';
import { FormsModule }   from '@angular/forms';
import { AdminService, GameRequest, GameResponse } from '../../../services/admin.service';

type ViewMode = 'list' | 'create' | 'edit';

@Component({
  selector: 'app-admin-games',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-games.component.html',
  styleUrl: './admin-games.component.css'
})
export class AdminGamesComponent implements OnInit {
  // ─── State ────────────────────────────────────────────────────────────────
  viewMode: ViewMode = 'list';
  loading  = false;
  saving   = false;
  games:  GameResponse[] = [];
  errorMessage:   string | null = null;
  successMessage: string | null = null;

  // Game being edited (null = create mode)
  editingGame: GameResponse | null = null;

  // Form model
  form: GameRequest = {
    gameDate:   '',
    kickOffTime: '19:00',
    location:   '',
    maxPlayers: 14
  };

  // Closing state per game id
  closingId: number | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadGames();
  }

  // ─── Data ─────────────────────────────────────────────────────────────────
  loadGames(): void {
    this.loading = true;
    this.adminService.getAllGames().subscribe({
      next: (games) => {
        // Most recent first
        this.games   = games.sort((a, b) =>
          new Date(b.gameDate).getTime() - new Date(a.gameDate).getTime()
        );
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load games.';
        this.loading = false;
      }
    });
  }

  // ─── Views ────────────────────────────────────────────────────────────────
  openCreateForm(): void {
    this.editingGame = null;
    this.form = { gameDate: '', kickOffTime: '19:00', location: '', maxPlayers: 14 };
    this.errorMessage = null;
    this.viewMode = 'create';
  }

  openEditForm(game: GameResponse): void {
    this.editingGame = game;
    this.form = {
      gameDate:    game.gameDate,
      kickOffTime: game.kickOffTime,
      location:    game.location,
      maxPlayers:  game.maxPlayers
    };
    this.errorMessage = null;
    this.viewMode = 'edit';
  }

  cancel(): void {
    this.viewMode     = 'list';
    this.editingGame  = null;
    this.errorMessage = null;
  }

  // ─── Create / Edit ────────────────────────────────────────────────────────
  submitForm(): void {
    this.saving       = true;
    this.errorMessage = null;

    const request$ = this.editingGame
      ? this.adminService.updateGame(this.editingGame.id, this.form)
      : this.adminService.createGame(this.form);

    request$.subscribe({
      next: () => {
        this.saving         = false;
        this.successMessage = this.editingGame ? 'Game updated.' : 'Game created!';
        this.viewMode       = 'list';
        this.editingGame    = null;
        this.loadGames();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.saving       = false;
        this.errorMessage = err?.error?.message ?? err?.error ?? 'Could not save game.';
      }
    });
  }

  // ─── Close game ───────────────────────────────────────────────────────────
  closeGame(game: GameResponse): void {
    if (!confirm(`Close "${this.formatDate(game.gameDate)}"? This cannot be undone.`)) return;
    this.closingId    = game.id;
    this.errorMessage = null;

    this.adminService.closeGame(game.id).subscribe({
      next: () => {
        this.closingId      = null;
        this.successMessage = 'Game closed.';
        this.loadGames();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.closingId    = null;
        this.errorMessage = err?.error?.message ?? 'Could not close game.';
      }
    });
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────
  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    try {
      return new Date(dateStr).toLocaleDateString('en-IE', {
        weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
      });
    } catch { return dateStr; }
  }

  formatTime(timeStr: string): string {
    if (!timeStr) return '';
    try {
      const [h, m] = timeStr.split(':').map(Number);
      const d = new Date(); d.setHours(h, m);
      return d.toLocaleTimeString('en-IE', { hour: 'numeric', minute: '2-digit', hour12: true });
    } catch { return timeStr; }
  }

  statusClass(status: string): string {
    return ({
      OPEN:      'badge-open',
      CLOSED:    'badge-closed',
      CANCELLED: 'badge-cancelled',
      COMPLETED: 'badge-completed',
    } as any)[status] ?? 'badge-closed';
  }

  statusLabel(status: string): string {
    return ({
      OPEN:      '● Open',
      CLOSED:    'Closed',
      CANCELLED: 'Cancelled',
      COMPLETED: 'Completed',
    } as any)[status] ?? status;
  }

  canClose(game: GameResponse): boolean {
    return game.status === 'OPEN';
  }

  canEdit(game: GameResponse): boolean {
    return game.status === 'OPEN';
  }
}
