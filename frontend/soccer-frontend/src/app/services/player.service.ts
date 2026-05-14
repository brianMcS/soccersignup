import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Player } from '../models/player.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PlayerService {
  private apiUrl = '/api/players';

  constructor(private http: HttpClient) {}

  getPlayers(){
    return this.http.get<Player[]>(this.apiUrl);
  }

  getPlayerById(id: number): Observable<Player> {
    return this.http.get<Player>(`${this.apiUrl}/${id}`);
  }

  registerPlayer(player: { name: string; email: string; phone: string }): Observable<Player> {
    return this.http.post<Player>(this.apiUrl, player);
  }
}
