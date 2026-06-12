import { Component, OnInit } from '@angular/core';
import { CommonModule }  from '@angular/common';
import { FormsModule }   from '@angular/forms';
import { forkJoin, Observable } from 'rxjs';
import { AdminService, GameRequest, GameResponse } from '../../../services/admin.service';
import { Router } from '@angular/router';
import { GameSlot } from '../../../models/game-slot.model';
import { getApiErrorMessage } from '../../../utils/api-error';
import {
  formatDateOnly,
  parseDateOnly as parseCalendarDate
} from '../../../utils/date-only';

type ViewMode = 'list' | 'create' | 'edit';
type GameFilter = 'next4Weeks' | 'next3Months' | 'allUpcoming' | 'past';

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
  activeFilter: GameFilter = 'next4Weeks';
  errorMessage:   string | null = null;
  successMessage: string | null = null;

  // Game being edited (null = create mode)
  editingGame: GameResponse | null = null;

  // Form model
  form: GameRequest = {
    gameDate:   '',
    kickOffTime: '19:00',
    location:   '',
    maxPlayers: 14,
    feeAmount: 5,
    revolutLink: ''
  };
  recurring = false;
  recurringCount = 4;

  // Closing state per game id
  closingId: number | null = null;
  paymentGameId: number | null = null;
  paymentActionPlayerId: number | null = null;
  signupsByGame: Record<number, GameSlot[]> = {};

  constructor(private adminService: AdminService, private router: Router) {}

  ngOnInit(): void {
    this.loadGames();
  }

  // ─── Data ─────────────────────────────────────────────────────────────────
  loadGames(): void {
    this.loading = true;
    this.adminService.getAllGames().subscribe({
      next: (games) => {
        this.games = games;
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = getApiErrorMessage(error, 'Could not load games.');
        this.loading = false;
      }
    });
  }

  // ─── Views ────────────────────────────────────────────────────────────────
  openCreateForm(): void {
    this.editingGame = null;
    this.form = {
      gameDate: '',
      kickOffTime: '19:00',
      location: '',
      maxPlayers: 14,
      feeAmount: 5,
      revolutLink: ''
    };
    this.recurring = false;
    this.recurringCount = 4;
    this.errorMessage = null;
    this.viewMode = 'create';
  }

  openEditForm(game: GameResponse): void {
    this.editingGame = game;
    this.form = {
      gameDate:    game.gameDate,
      kickOffTime: game.kickOffTime,
      location:    game.location,
      maxPlayers:  game.maxPlayers,
      feeAmount:   game.feeAmount ?? 5,
      revolutLink: game.revolutLink ?? ''
    };
    this.recurring = false;
    this.recurringCount = 4;
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

    const request$: Observable<GameResponse | GameResponse[]> = this.editingGame
      ? this.adminService.updateGame(this.editingGame.id, this.form)
      : this.recurring
        ? forkJoin(this.buildRecurringRequests().map(game => this.adminService.createGame(game)))
        : this.adminService.createGame(this.form);

    request$.subscribe({
      next: () => {
        this.saving         = false;
        this.successMessage = this.editingGame
          ? 'Game updated.'
          : this.recurring
            ? `${this.recurringCount} weekly games created.`
            : 'Game created!';
        this.viewMode       = 'list';
        this.editingGame    = null;
        this.loadGames();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err: any) => {
        this.saving       = false;
        this.errorMessage = getApiErrorMessage(err, 'Could not save game.');
      }
    });
  }

  get filteredGames(): GameResponse[] {
    return this.games
      .filter(game => this.matchesActiveFilter(game))
      .sort((a, b) => this.sortForActiveFilter(a, b));
  }

  setFilter(filter: GameFilter): void {
    this.activeFilter = filter;
  }

  matchesActiveFilter(game: GameResponse): boolean {
    const gameDate = this.parseDateOnly(game.gameDate);
    const today = this.startOfToday();

    if (this.activeFilter === 'past') {
      return gameDate < today || game.status === 'COMPLETED' || game.status === 'CANCELLED';
    }

    if (gameDate < today || game.status === 'COMPLETED' || game.status === 'CANCELLED') {
      return false;
    }

    if (this.activeFilter === 'allUpcoming') {
      return true;
    }

    const endDate = new Date(today);
    if (this.activeFilter === 'next4Weeks') {
      endDate.setDate(endDate.getDate() + 28);
    } else {
      endDate.setMonth(endDate.getMonth() + 3);
    }

    return gameDate <= endDate;
  }

  sortForActiveFilter(a: GameResponse, b: GameResponse): number {
    const aTime = this.parseDateOnly(a.gameDate).getTime();
    const bTime = this.parseDateOnly(b.gameDate).getTime();
    return this.activeFilter === 'past' ? bTime - aTime : aTime - bTime;
  }

  buildRecurringRequests(): GameRequest[] {
    return Array.from({ length: this.recurringCount }, (_, index) => ({
      ...this.form,
      gameDate: this.addWeeks(this.form.gameDate, index)
    }));
  }

  addWeeks(dateStr: string, weeks: number): string {
    const [year, month, day] = dateStr.split('-').map(Number);
    const date = new Date(year, month - 1, day);
    date.setDate(date.getDate() + weeks * 7);
    return this.toDateInputValue(date);
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
        this.errorMessage = getApiErrorMessage(err, 'Could not close game.');
      }
    });
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────
  togglePayments(game: GameResponse): void {
    if (this.paymentGameId === game.id) {
      this.paymentGameId = null;
      return;
    }
    this.paymentGameId = game.id;
    this.loadPayments(game.id);
  }

  loadPayments(gameId: number): void {
    this.adminService.getSignupsForGame(gameId).subscribe({
      next: slots => this.signupsByGame[gameId] = slots,
      error: (error) => {
        this.errorMessage = getApiErrorMessage(
          error,
          'Could not load payment details.');
      }
    });
  }

  paymentSlots(gameId: number): GameSlot[] {
    return (this.signupsByGame[gameId] ?? [])
      .filter(slot => slot.status !== 'WAITLISTED');
  }

  paidCount(gameId: number): number {
    return this.paymentSlots(gameId)
      .filter(slot => slot.paymentStatus === 'SELF_REPORTED' || slot.paymentStatus === 'CONFIRMED')
      .length;
  }

  confirmPayment(gameId: number, slot: GameSlot): void {
    if (!slot.playerId) return;
    this.paymentActionPlayerId = slot.playerId;
    this.adminService.confirmPayment(gameId, slot.playerId).subscribe({
      next: updated => {
        this.replacePaymentSlot(gameId, updated);
        this.paymentActionPlayerId = null;
      },
      error: err => {
        this.paymentActionPlayerId = null;
        this.errorMessage = getApiErrorMessage(err, 'Could not confirm payment.');
      }
    });
  }

  resetPayment(gameId: number, slot: GameSlot): void {
    if (!slot.playerId) return;
    this.paymentActionPlayerId = slot.playerId;
    this.adminService.rejectPayment(gameId, slot.playerId).subscribe({
      next: updated => {
        this.replacePaymentSlot(gameId, updated);
        this.paymentActionPlayerId = null;
      },
      error: err => {
        this.paymentActionPlayerId = null;
        this.errorMessage = getApiErrorMessage(err, 'Could not reset payment.');
      }
    });
  }

  paymentLabel(slot: GameSlot): string {
    return ({
      UNPAID: 'Unpaid',
      SELF_REPORTED: 'Awaiting confirmation',
      CONFIRMED: 'Confirmed'
    } as const)[slot.paymentStatus ?? 'UNPAID'];
  }

  paymentBadgeClass(slot: GameSlot): string {
    return ({
      UNPAID: 'badge-completed',
      SELF_REPORTED: 'badge-waitlisted',
      CONFIRMED: 'badge-open'
    } as const)[slot.paymentStatus ?? 'UNPAID'];
  }

  private replacePaymentSlot(gameId: number, updated: GameSlot): void {
    this.signupsByGame[gameId] = (this.signupsByGame[gameId] ?? [])
      .map(slot => slot.id === updated.id ? updated : slot);
  }

  formatDate(dateStr: string): string {
    return formatDateOnly(dateStr, {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  }

  formatTime(timeStr: string): string {
    if (!timeStr) return '';
    try {
      const [h, m] = timeStr.split(':').map(Number);
      const d = new Date(); d.setHours(h, m);
      return d.toLocaleTimeString('en-IE', { hour: 'numeric', minute: '2-digit', hour12: true });
    } catch { return timeStr; }
  }

  toDateInputValue(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  parseDateOnly(dateStr: string): Date {
    return parseCalendarDate(dateStr);
  }

  startOfToday(): Date {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return today;
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
    return game.status === 'OPEN' || game.status === 'CLOSED';
  }

  manageTeams(game: GameResponse): void {
    this.router.navigate(['/admin/games', game.id, 'teamsheet']);
  }
}
