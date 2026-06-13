export interface GameSlot {
  id?: number;
  gameId?: number;
  playerId?: number;
  playerName?: string;
  playerEmail?: string;
  status?: 'CONFIRMED' | 'WAITLISTED';
  signedUpAt?: string;
  paymentStatus?: 'UNPAID' | 'SELF_REPORTED' | 'CONFIRMED';
  feeAmount?: number;
  paidAt?: string;
  confirmedAt?: string;
}

// Used when sending a signup request to POST /api/gameslots
export interface SignupRequest {
  gameId: number;
}
