import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { GamesService } from '../../services/games.service';
import { PlayerService } from '../../services/player.service';
import { Game } from '../../models/game.model';
import { GameSlot } from '../../models/game-slot.model';

@Component({
  selector: 'app-game-signup-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './game-signup-list.component.html',
  styleUrl: './game-signup-list.component.css'
})
export class GameSignupListComponent implements OnInit {

  game: Game | null = null;
  slots: GameSlot[] = [];
  allSlots: (GameSlot | null)[] = [];

  // Join form
  formVisible = false;
  playerEmail = '';
  joining = false;
  joinError = '';
  joinSuccess = '';

  constructor(
    private gamesService: GamesService,
    private playerService: PlayerService
  ) {}

  ngOnInit() {
    this.loadLatestGame();
  }

  loadLatestGame(): void {
    this.gamesService.getAllGames().subscribe({
      next: (games) =>{
        const open =games.find(g => g.status === 'OPEN');
        if(open && open.id){
          this.game = open;
          this.loadSignups(open.id);
        }
      },
      error: (err) => console.error('Could not load games, err')
    });
  }

  loadSignups(gameId: number): void {
    this.gamesService.getSignups(gameId).subscribe({
      next: (slots) => {
        this.slots = slots;
        const max = this.game?.maxPlayers ?? 18;
        this.allSlots = Array(max).fill(null)
          .map((_, i) => slots[i] ?? null);
      },
      error: (err) => console.error('Could not load signups, err')
    });
  }

  showJoinForm(): void {
    this.formVisible = true;
    this.joinError = '';
    this.joinSuccess = '';
  }

  cancelJoin() {
    this.formVisible = false;
    this.playerEmail = '';
    this.joinError = '';
  }

  submitJoin(): void{
    if (!this.game?.id || !this.playerEmail.trim()) return;

    this.joining = true;
    this.joinError = '';

    this.playerService.getPlayers().subscribe({
      next: (players) => {
        const player = players.find(
          p => p.email.toLowerCase() === this.playerEmail.toLowerCase().trim()
        );
        if(!player || !player.id) {
          this.joinError = 'No player found with that email. Please register first.';
          this.joining = false;
          return;
        }
        this.gamesService.joinGame(this.game!.id!, player.id!).subscribe({
          next: ()=> {
            this.joinSuccess = 'You are in! See you on the pitch.';
            this.formVisible = false;
            this.playerEmail= '';
            this.joining = false;
            this.loadSignups(this.game!.id!);
          },
          error: (err) => {
            this.joinError = err.error ?? 'Could not join. Try again.';
            this.joining = false;
          }
        });
      },
      error: () => {
        this.joinError = 'Could not verify player. Try again.';
        this.joining = false;
      }
    });
  }
}
