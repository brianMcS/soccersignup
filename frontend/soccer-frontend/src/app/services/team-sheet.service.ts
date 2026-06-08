import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TeamSheet, TeamSheetRequest } from '../models/team-sheet.model';

@Injectable({ providedIn: 'root' })
export class TeamSheetService {

  constructor(private http: HttpClient) {}

  private url(gameId: number): string {
    return `/api/games/${gameId}/teamsheet`;
  }

  // Get the team sheet for a game
  // Returns 404 if not published and caller is not an organiser
  getTeamSheet(gameId: number): Observable<TeamSheet> {
    return this.http.get<TeamSheet>(this.url(gameId));
  }

  // Ask the backend to randomly split confirmed players into two teams
  autoSplit(gameId: number): Observable<TeamSheet> {
    return this.http.post<TeamSheet>(`${this.url(gameId)}/auto-split`, {});
  }

  // Save the full team sheet (organiser's drag-and-drop result)
  saveTeamSheet(gameId: number, request: TeamSheetRequest): Observable<TeamSheet> {
    return this.http.put<TeamSheet>(this.url(gameId), request);
  }

  // Publish the team sheet — notifies all confirmed players
  publishTeamSheet(gameId: number): Observable<TeamSheet> {
    return this.http.post<TeamSheet>(`${this.url(gameId)}/publish`, {});
  }
}
