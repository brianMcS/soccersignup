import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TeamSheetService } from '../../../services/team-sheet.service';
import {
  TeamSheet,
  TeamSheetEntry,
  TeamSheetEntryRequest,
  TeamSide
} from '../../../models/team-sheet.model';
import { getApiErrorMessage } from '../../../utils/api-error';

@Component({
  selector: 'app-admin-team-sheet',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-team-sheet.component.html',
  styleUrl: './admin-team-sheet.component.css'
})
export class AdminTeamSheetComponent implements OnInit {

  @ViewChild('pitchRef') pitchRef!: ElementRef<HTMLDivElement>;

  gameId!: number;
  teamSheet: TeamSheet | null = null;

  // Loading / action states
  loading = false;
  splitting = false;
  saving = false;
  publishing = false;

  errorMessage: string | null = null;
  successMessage: string | null = null;

  // The entry currently being dragged
  draggingEntry: TeamSheetEntry | null = null;

  // Track if we have unsaved changes
  isDirty = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private teamSheetService: TeamSheetService
  ) {}

  ngOnInit(): void {
    this.gameId = Number(this.route.snapshot.paramMap.get('gameId'));
    if (!this.gameId) {
      this.router.navigate(['/admin']);
      return;
    }
    this.loadTeamSheet();
  }

  // ─── DATA LOADING ─────────────────────────────────────────────────────────

  loadTeamSheet(): void {
    this.loading = true;
    this.teamSheetService.getTeamSheet(this.gameId).subscribe({
      next: (sheet) => {
        this.teamSheet = sheet;
        this.loading = false;
      },
      error: (error) => {
        // 404 just means no sheet exists yet — that's fine
        if (error?.status === 404) {
          this.teamSheet = null;
        } else {
          this.errorMessage = getApiErrorMessage(
            error,
            'Could not load the team sheet.');
        }
        this.loading = false;
      }
    });
  }

  // ─── AUTO SPLIT ───────────────────────────────────────────────────────────

  autoSplit(): void {
    if (!confirm('This will randomly reassign all players into two teams. Continue?')) return;
    this.splitting = true;
    this.errorMessage = null;

    this.teamSheetService.autoSplit(this.gameId).subscribe({
      next: (sheet) => {
        this.teamSheet = sheet;
        this.splitting = false;
        this.isDirty = false;
        this.showSuccess('Players split into teams. Drag to adjust positions.');
      },
      error: (err) => {
        this.splitting = false;
        this.errorMessage = getApiErrorMessage(err, 'Could not auto-split players.');
      }
    });
  }

  // ─── DRAG AND DROP ────────────────────────────────────────────────────────

  onDragStart(entry: TeamSheetEntry): void {
    this.draggingEntry = entry;
  }

  onDragEnd(): void {
    this.draggingEntry = null;
  }

  onPitchDrop(event: DragEvent): void {
    event.preventDefault();
    if (!this.draggingEntry || !this.pitchRef) return;

    const pitch = this.pitchRef.nativeElement;
    const rect = pitch.getBoundingClientRect();

    // Calculate drop position as percentage of pitch dimensions
    const x = ((event.clientX - rect.left) / rect.width) * 100;
    const y = ((event.clientY - rect.top) / rect.height) * 100;

    // Keep each team on its own side of the halfway line.
    this.draggingEntry.positionX = this.draggingEntry.teamSide === 'HOME'
      ? Math.min(Math.max(x, 3), 50)
      : Math.min(Math.max(x, 50), 97);
    this.draggingEntry.positionY = Math.min(Math.max(y, 3), 97);

    this.draggingEntry = null;
    this.isDirty = true;
  }

  onPitchDragOver(event: DragEvent): void {
    // Must prevent default to allow drop
    event.preventDefault();
  }

  // ─── SWITCH TEAM ──────────────────────────────────────────────────────────

  switchTeam(entry: TeamSheetEntry): void {
    entry.teamSide = entry.teamSide === 'HOME' ? 'AWAY' : 'HOME';

    // Mirror the X position to the other half of the pitch
    // so the player lands roughly in the same vertical position
    // but on the correct side
    entry.positionX = 100 - entry.positionX;

    this.isDirty = true;
  }

  // ─── JERSEY NUMBER ────────────────────────────────────────────────────────

  updateJersey(entry: TeamSheetEntry, value: string): void {
    const num = parseInt(value, 10);
    if (!isNaN(num) && num >= 1 && num <= 99) {
      entry.jerseyNumber = num;
      this.isDirty = true;
    }
  }

  // ─── POSITION LABEL ───────────────────────────────────────────────────────

  // ─── SAVE DRAFT ───────────────────────────────────────────────────────────

  saveDraft(): void {
    if (!this.teamSheet) return;
    this.saving = true;
    this.errorMessage = null;

    const request = this.buildRequest();

    this.teamSheetService.saveTeamSheet(this.gameId, request).subscribe({
      next: (sheet) => {
        this.teamSheet = sheet;
        this.saving = false;
        this.isDirty = false;
        this.showSuccess('Draft saved.');
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = getApiErrorMessage(err, 'Could not save draft.');
      }
    });
  }

  // ─── PUBLISH ──────────────────────────────────────────────────────────────

  publish(): void {
    if (!confirm('Publish these teams? All confirmed players will be notified.')) return;
    this.publishing = true;
    this.errorMessage = null;

    // Save first to make sure latest positions are persisted, then publish
    const request = this.buildRequest();

    this.teamSheetService.saveTeamSheet(this.gameId, request).subscribe({
      next: () => {
        this.teamSheetService.publishTeamSheet(this.gameId).subscribe({
          next: (sheet) => {
            this.teamSheet = sheet;
            this.publishing = false;
            this.isDirty = false;
            this.showSuccess('Teams published! Players have been notified.');
          },
          error: (err) => {
            this.publishing = false;
            this.errorMessage = getApiErrorMessage(err, 'Could not publish.');
          }
        });
      },
      error: (err) => {
        this.publishing = false;
        this.errorMessage = getApiErrorMessage(err, 'Could not save before publishing.');
      }
    });
  }

  // ─── HELPERS ──────────────────────────────────────────────────────────────

  get homeEntries(): TeamSheetEntry[] {
    return this.teamSheet?.entries.filter(e => e.teamSide === 'HOME') ?? [];
  }

  get awayEntries(): TeamSheetEntry[] {
    return this.teamSheet?.entries.filter(e => e.teamSide === 'AWAY') ?? [];
  }

  getTokenStyle(entry: TeamSheetEntry): object {
    return {
      left: `${entry.positionX}%`,
      top: `${entry.positionY}%`,
      transform: 'translate(-50%, -50%)'
    };
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

  private buildRequest() {
    const entries: TeamSheetEntryRequest[] = this.teamSheet!.entries.map(e => ({
      playerId: e.playerId,
      teamSide: e.teamSide as TeamSide,
      jerseyNumber: e.jerseyNumber,
      positionX: e.positionX,
      positionY: e.positionY
    }));
    return { entries };
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => this.successMessage = null, 3000);
  }

  goBack(): void {
    this.router.navigate(['/admin']);
  }
}
