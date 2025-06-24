import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Player } from '../models/player';
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

  // getPaginatedPlayers(page: number, size: number): Observable<Player[]> {
  //   return this.http.get<Player[]>(`${this.apiUrl}?page=${page}&size=${size}`);
  // }
}
