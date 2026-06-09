import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  AdminService,
  PlayerResponse,
  PlayerUpdateRequest
} from '../../../services/admin.service';

@Component({
  selector: 'app-admin-players',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-players.component.html',
  styleUrl: './admin-players.component.css'
})
export class AdminPlayersComponent implements OnInit {
  loading = false;
  players: PlayerResponse[] = [];
  filtered: PlayerResponse[] = [];
  searchQuery = '';

  errorMessage: string | null = null;
  successMessage: string | null = null;

  deactivatingId: number | null = null;
  editingRolesId: number | null = null;
  savingRolesId: number | null = null;
  editingPlayerId: number | null = null;
  savingPlayerId: number | null = null;

  editingRoles: string[] = [];
  editingPlayer: PlayerUpdateRequest = this.emptyPlayerForm();

  readonly availableRoles = ['PLAYER', 'ORGANISER', 'ADMIN'];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadPlayers();
  }

  loadPlayers(): void {
    this.loading = true;
    this.adminService.getAllPlayers().subscribe({
      next: (players) => {
        this.players = players.sort((a, b) => a.name.localeCompare(b.name));
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load players.';
        this.loading = false;
      }
    });
  }

  onSearch(): void {
    this.applyFilter();
  }

  applyFilter(): void {
    const query = this.searchQuery.trim().toLowerCase();
    this.filtered = query
      ? this.players.filter(player =>
          player.name.toLowerCase().includes(query)
          || player.email.toLowerCase().includes(query)
          || (player.phone ?? '').toLowerCase().includes(query)
        )
      : [...this.players];
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.applyFilter();
  }

  deactivate(player: PlayerResponse): void {
    if (!confirm(`Remove ${player.name} from the squad? They will no longer be able to join games.`)) {
      return;
    }

    this.deactivatingId = player.id;
    this.errorMessage = null;

    this.adminService.deactivatePlayer(player.id).subscribe({
      next: () => {
        this.deactivatingId = null;
        this.successMessage = `${player.name} has been removed.`;
        this.loadPlayers();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.deactivatingId = null;
        this.errorMessage = err?.error?.message ?? 'Could not remove player.';
      }
    });
  }

  openPlayerEditor(player: PlayerResponse): void {
    this.cancelRoleEdit();
    this.editingPlayerId = player.id;
    this.editingPlayer = {
      name: player.name,
      email: player.email,
      phone: player.phone ?? ''
    };
  }

  cancelPlayerEdit(): void {
    this.editingPlayerId = null;
    this.editingPlayer = this.emptyPlayerForm();
  }

  savePlayer(player: PlayerResponse): void {
    if (!this.isPlayerFormValid()) return;

    const request: PlayerUpdateRequest = {
      name: this.editingPlayer.name.trim(),
      email: this.editingPlayer.email.trim(),
      phone: this.editingPlayer.phone.trim()
    };

    this.savingPlayerId = player.id;
    this.errorMessage = null;

    this.adminService.updatePlayer(player.id, request).subscribe({
      next: () => {
        this.savingPlayerId = null;
        this.cancelPlayerEdit();
        this.successMessage = `${request.name} has been updated.`;
        this.loadPlayers();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.savingPlayerId = null;
        this.errorMessage = err?.error?.message ?? 'Could not update player.';
      }
    });
  }

  isPlayerFormValid(): boolean {
    return this.editingPlayer.name.trim().length >= 2
      && this.editingPlayer.email.trim().length > 0;
  }

  openRoleEditor(player: PlayerResponse): void {
    this.cancelPlayerEdit();
    this.editingRolesId = player.id;
    this.editingRoles = [...new Set(['PLAYER', ...(player.roles ?? [])])];
  }

  cancelRoleEdit(): void {
    this.editingRolesId = null;
    this.editingRoles = [];
  }

  toggleRole(role: string): void {
    const index = this.editingRoles.indexOf(role);
    if (index > -1) {
      if (role === 'PLAYER') return;
      this.editingRoles.splice(index, 1);
    } else {
      this.editingRoles.push(role);
    }
  }

  hasRole(role: string): boolean {
    return this.editingRoles.includes(role);
  }

  saveRoles(player: PlayerResponse): void {
    this.savingRolesId = player.id;
    this.errorMessage = null;

    this.adminService.updatePlayerRoles(player.id, this.editingRoles).subscribe({
      next: () => {
        this.savingRolesId = null;
        this.cancelRoleEdit();
        this.successMessage = `Roles updated for ${player.name}.`;
        this.loadPlayers();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.savingRolesId = null;
        this.errorMessage = err?.error?.message ?? 'Could not update roles.';
      }
    });
  }

  roleLabel(role: string): string {
    const labels: Record<string, string> = {
      PLAYER: 'Player',
      ORGANISER: 'Organiser',
      ADMIN: 'Admin'
    };
    return labels[role] ?? role;
  }

  isOrganiser(player: PlayerResponse): boolean {
    return player.roles?.includes('ORGANISER') || player.roles?.includes('ADMIN');
  }

  private emptyPlayerForm(): PlayerUpdateRequest {
    return { name: '', email: '', phone: '' };
  }
}
