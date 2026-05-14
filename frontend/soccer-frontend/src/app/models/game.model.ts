export interface Game {
  id?: number;
  gameDate: string;
  kickOffTime?: string;
  location: string;
  maxPlayers: number;
  status?: 'OPEN' | 'CANCELLED' | 'COMPLETED';
  createdAt?: string;
}
