import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

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
}
