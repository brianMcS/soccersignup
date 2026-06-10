export interface Game {
  id?: number;
  gameDate: string;
  kickOffTime?: string;
  location: string;
  maxPlayers: number;
  feeAmount: number;
  revolutLink?: string;
  confirmedCount?: number;
  waitlistedCount?: number;
  status?: 'OPEN' | 'CANCELLED' | 'COMPLETED';
  createdAt?: string;
}
