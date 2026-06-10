import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Game} from '../models/game.model';
import {GameSlot, SignupRequest} from '../models/game-slot.model';

@Injectable({
  providedIn: 'root'
})
export class GamesService {

  private gamesUrl = '/api/games';
  private slotsUrl = '/api/gameslots';

  constructor(private http: HttpClient) {}

  // GET /api/games — all games
  getAllGames(): Observable<Game[]> {
    return this.http.get<Game[]>(this.gamesUrl);
  }

  getCompletedGames(): Observable<Game[]> {
    return this.http.get<Game[]>(`${this.gamesUrl}?status=COMPLETED`);
  }

  // POST /api/games — create a game (organiser use)
  createGame(game: Partial<Game>): Observable<Game> {
    return this.http.post<Game>(this.gamesUrl, game);
  }

  // GET /api/gameslots/{gameId} — who is signed up
  getSignups(gameId: number): Observable<GameSlot[]> {
    return this.http.get<GameSlot[]>(`${this.slotsUrl}/${gameId}`);
  }

  // POST /api/gameslots — sign a player up
  joinGame(gameId: number, playerId: number): Observable<GameSlot> {
    const body: SignupRequest = { gameId, playerId };
    return this.http.post<GameSlot>(this.slotsUrl, body);
  }

  // DELETE /api/gameslots/{gameId}/players/{playerId}
  leaveGame(gameId: number, playerId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.slotsUrl}/${gameId}/players/${playerId}`
    );
  }

  reportPayment(gameId: number, playerId: number): Observable<GameSlot> {
    return this.http.patch<GameSlot>(
      `${this.slotsUrl}/${gameId}/players/${playerId}/pay`,
      {}
    );
  }

  confirmPayment(gameId: number, playerId: number): Observable<GameSlot> {
    return this.http.patch<GameSlot>(
      `${this.slotsUrl}/${gameId}/players/${playerId}/confirm`,
      {}
    );
  }

  rejectPayment(gameId: number, playerId: number): Observable<GameSlot> {
    return this.http.patch<GameSlot>(
      `${this.slotsUrl}/${gameId}/players/${playerId}/reject`,
      {}
    );
  }
}
