export interface JwtPayload {
  sub?: string;
  id?: number;
  playerId?: number;
  name?: string;
  email?: string;
  roles?: string[];
  exp?: number;
}

export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3 || !parts[1]) {
      return null;
    }

    const base64 = parts[1]
      .replace(/-/g, '+')
      .replace(/_/g, '/')
      .padEnd(Math.ceil(parts[1].length / 4) * 4, '=');

    return JSON.parse(atob(base64)) as JwtPayload;
  } catch {
    return null;
  }
}

export function isJwtUsable(token: string, nowMs = Date.now()): boolean {
  const payload = decodeJwtPayload(token);
  return typeof payload?.exp === 'number'
    && Number.isFinite(payload.exp)
    && payload.exp * 1000 > nowMs;
}
