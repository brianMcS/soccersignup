import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Game, GameRequest} from '../models/game.model';
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

  createGame(request: GameRequest): Observable<Game> {
    return this.http.post<Game>(this.gamesUrl, request);
  }

  createGames(requests: GameRequest[]): Observable<Game[]> {
    return this.http.post<Game[]>(`${this.gamesUrl}/batch`, { games: requests });
  }

  updateGame(id: number, request: GameRequest): Observable<Game> {
    return this.http.put<Game>(`${this.gamesUrl}/${id}`, request);
  }

  closeGame(id: number): Observable<Game> {
    return this.http.post<Game>(`${this.gamesUrl}/${id}/close`, {});
  }

  // GET /api/gameslots/{gameId} — who is signed up
  getSignups(gameId: number): Observable<GameSlot[]> {
    return this.http.get<GameSlot[]>(`${this.slotsUrl}/${gameId}`);
  }

  // POST /api/gameslots — sign a player up
  joinGame(gameId: number): Observable<GameSlot> {
    const body: SignupRequest = { gameId };
    return this.http.post<GameSlot>(this.slotsUrl, body);
  }

  // DELETE /api/gameslots/{gameId}/players/{playerId}
  leaveGame(gameId: number, playerId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.slotsUrl}/${gameId}/players/${playerId}`
    );
  }

  reportPayment(gameId: number, playerId: number, version: number): Observable<GameSlot> {
    return this.http.patch<GameSlot>(
      `${this.slotsUrl}/${gameId}/players/${playerId}/pay`,
      { version }
    );
  }

  confirmPayment(gameId: number, playerId: number, version: number): Observable<GameSlot> {
    return this.http.patch<GameSlot>(
      `${this.slotsUrl}/${gameId}/players/${playerId}/confirm`,
      { version }
    );
  }

  rejectPayment(gameId: number, playerId: number, version: number): Observable<GameSlot> {
    return this.http.patch<GameSlot>(
      `${this.slotsUrl}/${gameId}/players/${playerId}/reject`,
      { version }
    );
  }

}
