import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {GameSlot} from '../models/game-slot.model';

export interface GameRequest{
  gameDate: string;
  kickOffTime: string;
  location: string;
  maxPlayers: number;
  feeAmount: number;
  revolutLink?: string;
}

export interface GameResponse {
  id: number;
  gameDate: string;
  kickOffTime: string;
  location: string;
  maxPlayers: number;
  feeAmount: number;
  revolutLink?: string;
  status: 'OPEN' | 'CLOSED' | 'CANCELLED' | 'COMPLETED';
  createdAt?: string;
}

export interface PlayerResponse {
  id: number;
  name: string;
  email: string;
  phone?: string;
  roles: string[];
  isActive: boolean;
  createdAt?: string;
}

export interface PlayerUpdateRequest {
  name: string;
  email: string;
  phone: string;
}

@Injectable({providedIn: 'root'})
export class AdminService {

  constructor(private http: HttpClient) {}

  // Games
  createGame(request: GameRequest) : Observable<GameResponse>{
    return this.http.post<GameResponse>('/api/games', request);
  }

  getAllGames(): Observable<GameResponse[]>{
    return this.http.get<GameResponse[]>('/api/games');
  }

  getGamesByStatus(status: string): Observable<GameResponse[]> {
    return this.http.get<GameResponse[]>(`/api/games?status=${status}`);
  }

  updateGame(id: number, request: GameRequest) :Observable<GameResponse>{
    return this.http.put<GameResponse>(`/api/games/${id}`, request);
  }

  closeGame(id: number): Observable<GameResponse>{
    return this.http.post<GameResponse>(`/api/games/${id}/close`, {});
  }

  // Players
  getAllPlayers(): Observable<PlayerResponse[]> {
    return this.http.get<PlayerResponse[]>('/api/players');
  }

  deactivatePlayer(id: number): Observable<void> {
    return this.http.patch<void>(`/api/players/${id}/deactivate`, {});
  }

  updatePlayer(id: number, request: PlayerUpdateRequest): Observable<PlayerResponse> {
    return this.http.put<PlayerResponse>(`/api/players/${id}`, request);
  }

  updatePlayerRoles(id: number, roles: string[]): Observable<PlayerResponse>{
    return this.http.put<PlayerResponse>(`/api/players/${id}/roles`, { roles });
  }

  //Signups
  getSignupsForGame(gameId: number): Observable<GameSlot[]> {
    return this.http.get<GameSlot[]>(`/api/gameslots/${gameId}`);
  }

  removeSignup(gameId: number, playerId: number): Observable<void> {
    return this.http.delete<void>(`/api/gameslots/${gameId}/players/${playerId}`);
  }

  confirmPayment(gameId: number, playerId: number): Observable<GameSlot> {
    return this.http.patch<GameSlot>(
      `/api/gameslots/${gameId}/players/${playerId}/confirm`,
      {}
    );
  }

  rejectPayment(gameId: number, playerId: number): Observable<GameSlot> {
    return this.http.patch<GameSlot>(
      `/api/gameslots/${gameId}/players/${playerId}/reject`,
      {}
    );
  }
}
