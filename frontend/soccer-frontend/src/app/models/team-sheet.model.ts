export type TeamSide = 'HOME' | 'AWAY';

export interface TeamSheetEntry {
  id?: number;
  playerId: number;
  playerName: string;
  teamSide: TeamSide;
  jerseyNumber: number;
  positionX: number;   // 0–100, percentage of pitch width
  positionY: number;   // 0–100, percentage of pitch height
}

export interface TeamSheet {
  id?: number;
  gameId: number;
  published: boolean;
  publishedAt?: string;
  updatedAt?: string;
  entries: TeamSheetEntry[];
}

// What the frontend sends when saving the team sheet
export interface TeamSheetRequest {
  entries: TeamSheetEntryRequest[];
}

export interface TeamSheetEntryRequest {
  playerId: number;
  teamSide: TeamSide;
  jerseyNumber: number;
  positionX: number;
  positionY: number;
}
