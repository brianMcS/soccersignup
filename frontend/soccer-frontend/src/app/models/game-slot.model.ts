export interface GameSlot {
  id?: number;
  playerName?: string;
  playerEmail?: string;
  status?: 'CONFIRMED' | 'WAITLISTED' | 'WITHDRAWN';
  signedUpAt?: string;
}

// Used when sending a signup request to POST /api/gameslots
export interface SignupRequest {
  gameId: number;
  playerId: number;
}
