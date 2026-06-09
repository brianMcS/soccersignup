import {Component, OnDestroy, OnInit} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { GamesService } from '../../services/games.service';
import { Game } from '../../models/game.model';
import { GameSlot } from '../../models/game-slot.model';
import {CurrentUser, UserService} from '../../services/user.service';
import {Subscription} from 'rxjs';
import { TeamSheetService } from '../../services/team-sheet.service';
import {RouterModule} from '@angular/router';
import {NotificationService} from '../../services/notification.service';

type PageState = 'loading' | 'no-game' | 'ready';

@Component({
  selector: 'app-game-signup-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './game-signup-list.component.html',
  styleUrl: './game-signup-list.component.css'
})
export class GameSignupListComponent implements OnInit, OnDestroy {

  // Page state
  pageState: PageState = 'loading';
  currentUser: CurrentUser | null = null;

  // Game data
  game: Game | null = null;
  slots: GameSlot[] = [];

  // Action state
  joining = false;
  leaving = false;
  actionError: string | null = null;
  actionSuccess: string | null = null;
  teamsPublished = false;

  private subs = new Subscription();

  constructor(
    private gamesService: GamesService,
    private userService: UserService,
    private teamSheetService: TeamSheetService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.subs.add(
      this.userService.currentUser$.subscribe(u => {
        this.currentUser = u;
      })
    );
    this.loadLatestGame();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // Data loading
  loadLatestGame(): void {
    this.pageState = 'loading';
    this.actionError = null;
    this.actionSuccess = null;

    this.gamesService.getAllGames().subscribe({
      next: (games) =>{
        const open = games
          .filter(g => g.status === 'OPEN')
          .sort((first, second) => first.gameDate.localeCompare(second.gameDate))[0];
        if(open?.id){
          this.game = open;
          this.loadSignups(open.id);
        } else {
          this.pageState = 'no-game';
        }
      },
      error: () => {
        this.pageState = 'no-game';
        this.actionError = 'Could not load games, err';
      }
    });
  }

  loadSignups(gameId: number): void {
    this.gamesService.getSignups(gameId).subscribe({
      next: (slots) => {
        this.slots = slots;
        this.pageState = 'ready';
        this.checkTeamSheet(gameId);
      },
      error: () => {
        this.slots = [];
        this.pageState = 'ready';
      }
    });
  }

  // Computed getters
  get allSlots(): (GameSlot | null)[] {
    const max = this.game?.maxPlayers ?? 18;
    return Array(max).fill(null).map((_, i) => this.confirmedSlots[i] ?? null);
  }

  get confirmedSlots(): GameSlot[] {
    return this.slots.filter(s => s.status === 'CONFIRMED' || !s.status);
  }

  get waitlistedSlots(): GameSlot[] {
    return this.slots.filter(s => s.status === 'WAITLISTED');
  }

  get confirmedCount(): number {
    return this.confirmedSlots.length;
  }

  get spotsRemaining(): number {
    return Math.max(0, (this.game?.maxPlayers ?? 18) - this.confirmedCount);
  }

  get isFull(): boolean{
    return this.spotsRemaining === 0;
  }

  get isUserSignedUp(): boolean {
    if (!this.currentUser) return false;
    return this.slots.some(
      s => s.playerId === this.currentUser!.id  ||
        s.playerEmail?.toLowerCase() === this.currentUser!.email?.toLowerCase()
    );
  }

  get isUserWaitlisted(): boolean {
    if(!this.currentUser) return false;
    return this.waitlistedSlots.some(
      s => s.playerId === this.currentUser!.id  ||
        s.playerEmail?.toLowerCase() === this.currentUser!.email?.toLowerCase()
    );
  }

  get userSlot(): GameSlot | undefined {
    if (!this.currentUser) return undefined;
    return this.slots.find(
      s => s.playerId === this.currentUser!.id ||
        s.playerEmail?.toLowerCase() === this.currentUser!.email?.toLowerCase()
    );
  }

  get progressPercent(): number {
    const max = this.game?.maxPlayers ?? 14;
    return  Math.round((this.confirmedCount / max) * 100);
  }

  // Actions
  join(): void{
    if (!this.game?.id || !this.currentUser?.id) return;
    this.joining = true;
    this.actionError = null;
    this.actionSuccess = null;

    this.gamesService.joinGame(this.game!.id!, this.currentUser!.id).subscribe({
      next: (slot) => {
        this.joining = false;
        this.actionSuccess = slot.status === 'WAITLISTED'
          ? 'You have joined the waitlist. If a player leaves, you will be automatically promoted into the game.'
          : `You are in! See you on the pitch ${this.formatDate(this.game!.gameDate)}.`;
        this.loadSignups(this.game!.id!);
      },
      error: (err) => {
        this.joining = false;
        this.actionError = err?.error?.message ?? err?.error ?? 'Could not join. Please try again.';
      }
    });
  }

  leave(): void {
    if (!this.game?.id || !this.currentUser) return;
    this.leaving = true;
    this.actionError = null;
    this.actionSuccess = null;

    this.gamesService.leaveGame(this.game.id, this.currentUser.id).subscribe({
      next: () => {
        this.leaving = false;
        this.actionSuccess = this.teamsPublished
          ? "You've left the game. The published teams have been updated."
          : "You've left the game. You can rejoin if spots are available.";
        this.loadSignups(this.game!.id!);
        this.notificationService.fetchUnreadCount();
      }, error: (err) => {
        this.leaving = false;
        this.actionError = err?.error?.message ?? err?.error ?? 'Could not leave. Please try again';
      }
    });
  }

  dismissAlert(): void {
    this.actionError = null;
    this.actionSuccess = null;
  }

  //Helpers
  getInitials(name: string): string {
    return (name ?? '?')
      .split(' ')
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  formatDate(dateStr: string): string {
    if(!dateStr) return '';
    try {
      return new Date(dateStr).toLocaleDateString('en-IE', {
        weekday: 'long', day: 'numeric', month: 'long'
      });
    } catch {
      return dateStr
    }
  }

  formatTime(timeStr: string): string {
    if (!timeStr) return '';
    try {
      const [h, m] = timeStr.split(':').map(Number);
      const d = new Date();
      d.setHours(h, m);
      return d.toLocaleTimeString('en-IE', { hour: 'numeric', minute: '2-digit', hour12: true });
    } catch {
      return timeStr;
    }
  }

  trackByIndex(index: number): number {
    return index;
  }

  checkTeamSheet(gameId: number): void {
    this.teamSheetService.getTeamSheet(gameId).subscribe({
      next: (sheet) => {
        this.teamsPublished = sheet.published;
      },
      error: () => {
        this.teamsPublished = false;
      }
    });
  }
}
