import { formatDateOnly, parseDateOnly } from './date-only';

describe('date-only utilities', () => {
  it('parses a calendar date in local time', () => {
    const date = parseDateOnly('2026-06-12');

    expect(date.getFullYear()).toBe(2026);
    expect(date.getMonth()).toBe(5);
    expect(date.getDate()).toBe(12);
    expect(date.getHours()).toBe(0);
  });

  it('does not silently normalize invalid calendar dates', () => {
    expect(() => parseDateOnly('2026-02-31'))
      .toThrowError('Invalid date-only value: 2026-02-31');
  });

  it('returns the original value when formatting invalid input', () => {
    expect(formatDateOnly('not-a-date', { year: 'numeric' }))
      .toBe('not-a-date');
  });
});
