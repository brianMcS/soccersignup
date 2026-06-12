export type GameStatus = 'OPEN' | 'CLOSED' | 'CANCELLED' | 'COMPLETED';

export interface Game {
  id: number;
  gameDate: string;
  kickOffTime: string;
  location: string;
  maxPlayers: number;
  feeAmount: number;
  revolutLink?: string;
  confirmedCount?: number;
  waitlistedCount?: number;
  status: GameStatus;
  createdAt?: string;
}

export interface GameRequest {
  gameDate: string;
  kickOffTime: string;
  location: string;
  maxPlayers: number;
  feeAmount: number;
  revolutLink?: string;
}
