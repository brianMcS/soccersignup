import {Component, OnInit} from '@angular/core';
import {AdminService, PlayerResponse} from '../../../services/admin.service';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-admin-players',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-players.component.html',
  styleUrl: './admin-players.component.css'
})
export class AdminPlayersComponent implements OnInit {
  // State
  loading = false;
  players: PlayerResponse[] = [];
  filtered: PlayerResponse[] = [];
  searchQuery = '';

  errorMessage: string | null = null;
  successMessage: string | null = null;

  // Per-player action state
  deactivatingId: number | null = null;
  editingRolesId: number | null = null;
  savingRolesId: number | null = null;

  // role editing - working copy
  editingRoles: string[] = [];

  readonly  availableRoles = ['ROLE_USER', 'ROLE_ORGANISER', 'ROLE_ADMIN'];

  constructor(private adminService: AdminService) { }

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

  // Search
  onSearch(): void {
    this.applyFilter();
  }

  applyFilter(): void {
    const q = this.searchQuery.trim().toLowerCase();
    this.filtered = q
      ? this.players.filter(p =>
        p.name.toLowerCase().includes(q) ||
        p.email.toLowerCase().includes(q)
      )
      : [...this.players];
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.applyFilter();
  }

  // Deactivate
  deactivate(player: PlayerResponse): void {
    if (!confirm(`Really ${player.name} fromt the squad? They will no longer be able to join games.`)) return;
    this.deactivatingId = player.id;
    this.errorMessage = null;

    this.adminService.deactivatePlayer(player.id).subscribe({
      next: () => {
        this.deactivatingId = null;
        this.successMessage = `${player.name} has been removed`;
        this.loadPlayers();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (err) => {
        this.deactivatingId = null;
        this.errorMessage = err?.error?.message ?? 'Could not remove player.';
      }
    });
  }

  // Role editing
  openRoleEditor(player: PlayerResponse): void {
    this.editingRolesId = player.id;
    this.editingRoles = [...(player.roles ?? [])];
  }

  cancelRoleEdit(): void {
    this.editingRolesId = null;
    this.editingRoles   = [];
  }

  toggleRole(role: string): void {
    const idx = this.editingRoles.indexOf(role);
    if (idx > -1){
      if (role === 'ROLE_PLAYER') return; // always keep ROLE_PLAYER
      this.editingRoles.splice(idx, 1);
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
        this.editingRolesId = null;
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

  // Helpers
  getInitials(name: string): string {
    return (name ?? '?')
      .split(' ')
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '—';
    try {
      return new Date(dateStr).toLocaleDateString('en-IE', {
        day: 'numeric', month: 'short', year: 'numeric'
      });
    } catch { return dateStr; }
  }

  roleLabel(role: string): string {
    return ({ ROLE_USER: 'Player', ROLE_ORGANISER: 'Organiser', ROLE_ADMIN: 'Admin' } as any)[role] ?? role;
  }

  isOrganiser(player: PlayerResponse): boolean {
    return player.roles?.includes('ROLE_ORGANISER') || player.roles?.includes('ROLE_ADMIN');
  }
}
