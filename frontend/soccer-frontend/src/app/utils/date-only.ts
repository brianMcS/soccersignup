export function parseDateOnly(value: string): Date {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (!match) {
    throw new Error(`Invalid date-only value: ${value}`);
  }

  const [, year, month, day] = match;
  const date = new Date(Number(year), Number(month) - 1, Number(day));

  if (
    date.getFullYear() !== Number(year)
    || date.getMonth() !== Number(month) - 1
    || date.getDate() !== Number(day)
  ) {
    throw new Error(`Invalid date-only value: ${value}`);
  }

  return date;
}

export function formatDateOnly(
  value: string,
  options: Intl.DateTimeFormatOptions
): string {
  if (!value) {
    return '';
  }

  try {
    return parseDateOnly(value).toLocaleDateString('en-IE', options);
  } catch {
    return value;
  }
}
