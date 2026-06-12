import { getApiErrorMessage } from './api-error';

describe('getApiErrorMessage', () => {
  it('returns a canonical API message', () => {
    expect(getApiErrorMessage(
      { error: { message: 'Game is full', status: 409, fieldErrors: {} } },
      'Fallback'
    )).toBe('Game is full');
  });

  it('prefers a field validation message over a generic message', () => {
    expect(getApiErrorMessage(
      {
        error: {
          message: 'Validation failed',
          status: 400,
          fieldErrors: { email: 'Must be a valid email address' }
        }
      },
      'Fallback'
    )).toBe('Must be a valid email address');
  });

  it('uses the fallback for an unknown response', () => {
    expect(getApiErrorMessage({ error: {} }, 'Please try again'))
      .toBe('Please try again');
  });
});
