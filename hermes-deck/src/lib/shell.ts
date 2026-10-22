/** Quote a value for POSIX sh so it is always passed as one literal word. */
export function shq(value: string): string {
  return `'${value.replace(/'/g, `'\\''`)}'`;
}

/** Characters we accept inside `.env` values without quoting (covers every provider key format). */
const ENV_VALUE = /^[A-Za-z0-9._~+/=:@-]+$/;
const ENV_NAME = /^[A-Z][A-Z0-9_]*$/;

export function assertEnvEntry(name: string, value: string): void {
  if (!ENV_NAME.test(name)) throw new Error(`Invalid environment variable name: ${name}`);
  if (!ENV_VALUE.test(value)) {
    throw new Error(`${name} contains characters that are not allowed in a key (spaces, quotes or newlines).`);
  }
}

/** Random hex token from the platform CSPRNG. */
export function randomToken(bytes = 32): string {
  const buf = new Uint8Array(bytes);
  crypto.getRandomValues(buf);
  return Array.from(buf, (b) => b.toString(16).padStart(2, '0')).join('');
}
