export interface ApiErrorResponse {
  message?: string;
  status?: number;
  fieldErrors?: Record<string, string>;
  error?: string;
}

export function getApiErrorMessage(error: unknown, fallback: string): string {
  const payload = (error as { error?: unknown } | null)?.error;

  if (typeof payload === 'string' && payload.trim()) {
    return payload;
  }

  if (!payload || typeof payload !== 'object') {
    return fallback;
  }

  const response = payload as ApiErrorResponse;
  const fieldMessage = Object.values(response.fieldErrors ?? {})
    .find(message => typeof message === 'string' && message.trim());

  if (fieldMessage) {
    return fieldMessage;
  }
  if (typeof response.message === 'string' && response.message.trim()) {
    return response.message;
  }
  if (typeof response.error === 'string' && response.error.trim()) {
    return response.error;
  }

  const legacyValidationMessage = Object.values(response)
    .find(value => typeof value === 'string' && value.trim());

  return typeof legacyValidationMessage === 'string'
    ? legacyValidationMessage
    : fallback;
}
