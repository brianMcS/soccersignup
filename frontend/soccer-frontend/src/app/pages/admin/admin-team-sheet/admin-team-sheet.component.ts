import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TeamSheetService } from '../../../services/team-sheet.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';
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
  selectedEntry: TeamSheetEntry | null = null;
  positionAnnouncement = '';

  // Track if we have unsaved changes
  isDirty = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private teamSheetService: TeamSheetService,
    private confirmDialog: ConfirmDialogService
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

  async autoSplit(): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Auto Split Teams',
      message: 'This will randomly reassign all players into two teams.',
      confirmText: 'Auto Split'
    });
    if (!confirmed) return;

    this.splitting = true;
    this.errorMessage = null;

    this.teamSheetService.autoSplit(this.gameId, this.teamSheet?.version).subscribe({
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

    this.positionEntryAtPointer(this.draggingEntry, event.clientX, event.clientY);

    this.draggingEntry = null;
    this.isDirty = true;
  }

  onPitchDragOver(event: DragEvent): void {
    // Must prevent default to allow drop
    event.preventDefault();
  }

  selectEntry(entry: TeamSheetEntry): void {
    this.selectedEntry = this.selectedEntry === entry ? null : entry;
    this.positionAnnouncement = this.selectedEntry
      ? `${entry.playerName} selected. Tap or click the pitch to place them, or use the arrow keys.`
      : `${entry.playerName} deselected.`;
  }

  onPitchClick(event: MouseEvent): void {
    if (!this.selectedEntry) return;
    this.positionEntryAtPointer(this.selectedEntry, event.clientX, event.clientY);
    this.isDirty = true;
  }

  onTokenKeydown(event: KeyboardEvent, entry: TeamSheetEntry): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.selectEntry(entry);
      return;
    }

    const movement = event.shiftKey ? 5 : 1;
    let nextX = entry.positionX;
    let nextY = entry.positionY;

    switch (event.key) {
      case 'ArrowLeft':
        nextX -= movement;
        break;
      case 'ArrowRight':
        nextX += movement;
        break;
      case 'ArrowUp':
        nextY -= movement;
        break;
      case 'ArrowDown':
        nextY += movement;
        break;
      default:
        return;
    }

    event.preventDefault();
    entry.positionX = this.clampX(entry.teamSide, nextX);
    entry.positionY = this.clamp(nextY, 3, 97);
    this.selectedEntry = entry;
    this.isDirty = true;
    this.announcePosition(entry);
  }

  // ─── SWITCH TEAM ──────────────────────────────────────────────────────────

  switchTeam(entry: TeamSheetEntry): void {
    entry.teamSide = entry.teamSide === 'HOME' ? 'AWAY' : 'HOME';

    // Mirror the X position to the other half of the pitch
    // so the player lands roughly in the same vertical position
    // but on the correct side
    entry.positionX = 100 - entry.positionX;

    this.selectedEntry = entry;
    this.isDirty = true;
    this.announcePosition(entry);
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

  async publish(): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Publish Teams',
      message: 'All confirmed players will be notified.',
      confirmText: 'Publish Teams'
    });
    if (!confirmed) return;

    this.publishing = true;
    this.errorMessage = null;

    // Save first to make sure latest positions are persisted, then publish
    const request = this.buildRequest();

    this.teamSheetService.saveTeamSheet(this.gameId, request).subscribe({
      next: (savedSheet) => {
        this.teamSheet = savedSheet;
        if (savedSheet.version === undefined) {
          this.publishing = false;
          this.errorMessage = 'Could not publish because the team sheet version is missing. Refresh and try again.';
          return;
        }
        this.teamSheetService.publishTeamSheet(this.gameId, savedSheet.version).subscribe({
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
    return { version: this.teamSheet?.version, entries };
  }

  private positionEntryAtPointer(
    entry: TeamSheetEntry,
    clientX: number,
    clientY: number
  ): void {
    const rect = this.pitchRef.nativeElement.getBoundingClientRect();
    const x = ((clientX - rect.left) / rect.width) * 100;
    const y = ((clientY - rect.top) / rect.height) * 100;

    entry.positionX = this.clampX(entry.teamSide, x);
    entry.positionY = this.clamp(y, 3, 97);
    this.announcePosition(entry);
  }

  private clampX(teamSide: TeamSide, value: number): number {
    return teamSide === 'HOME'
      ? this.clamp(value, 3, 50)
      : this.clamp(value, 50, 97);
  }

  private clamp(value: number, minimum: number, maximum: number): number {
    return Math.min(Math.max(value, minimum), maximum);
  }

  private announcePosition(entry: TeamSheetEntry): void {
    this.positionAnnouncement =
      `${entry.playerName} moved to ${Math.round(entry.positionX)} percent across `
      + `and ${Math.round(entry.positionY)} percent down the pitch.`;
  }

  private showSuccess(message: string): void {
    this.successMessage = message;
    setTimeout(() => this.successMessage = null, 3000);
  }

  goBack(): void {
    this.router.navigate(['/admin']);
  }
}
