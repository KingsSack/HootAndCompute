// crypto.randomUUID() requires Chrome 92+ (Android 12+), which the REV Control Hub
// does not have. Fall back to a manual UUID v4 built from crypto.getRandomValues(),
// which is available all the way back to Chrome 11 / Android 4.4.
export function generateId(): string {
  if (typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  // Manual UUID v4 fallback
  const bytes = new Uint8Array(16);
  crypto.getRandomValues(bytes);
  bytes[6] = (bytes[6] & 0x0f) | 0x40; // version 4
  bytes[8] = (bytes[8] & 0x3f) | 0x80; // variant 10xx
  return [...bytes]
      .map((b, i) => {
        const hex = b.toString(16).padStart(2, '0');
        return [4, 6, 8, 10].includes(i) ? `-${hex}` : hex;
      })
      .join('');
}
