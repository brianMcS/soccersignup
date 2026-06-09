import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { TeamSheetService } from '../../services/team-sheet.service';
import { TeamSheet, TeamSheetEntry } from '../../models/team-sheet.model';

@Component({
  selector: 'app-team-sheet-view',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './team-sheet-view.component.html',
  styleUrl: './team-sheet-view.component.css'
})
export class TeamSheetViewComponent implements OnInit {

  teamSheet: TeamSheet | null = null;
  loading = true;
  error: string | null = null;

  // Derived from teamSheet.entries for easy template access
  homeEntries: TeamSheetEntry[] = [];
  awayEntries: TeamSheetEntry[] = [];

  constructor(
    private teamSheetService: TeamSheetService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Get gameId from the route — /games/:gameId/teams
    const gameId = Number(this.route.snapshot.paramMap.get('gameId'));
    if (!gameId) {
      this.error = 'Invalid game.';
      this.loading = false;
      return;
    }
    this.loadTeamSheet(gameId);
  }

  loadTeamSheet(gameId: number): void {
    this.loading = true;
    this.teamSheetService.getTeamSheet(gameId).subscribe({
      next: (sheet) => {
        this.teamSheet = sheet;
        this.homeEntries = sheet.entries.filter(e => e.teamSide === 'HOME');
        this.awayEntries = sheet.entries.filter(e => e.teamSide === 'AWAY');
        this.loading = false;
      },
      error: () => {
        this.error = 'Teams have not been announced yet. Check back closer to kick-off.';
        this.loading = false;
      }
    });
  }

  getInitials(name: string): string {
    return (name ?? '?')
      .split(' ')
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  getFirstName(name: string): string {
    return name.trim().split(/\s+/)[0] ?? '';
  }

  getLastName(name: string): string {
    return name.trim().split(/\s+/).slice(1).join(' ');
  }

  // Convert percentage position to CSS style
  // Called for each player token on the pitch
  getTokenStyle(entry: TeamSheetEntry): object {
    return {
      left: `${entry.positionX}%`,
      top: `${entry.positionY}%`,
      transform: 'translate(-50%, -50%)'
    };
  }
}
