const resolveApiHost = (): string => {
  const host = window.location.hostname;
  if (!host || host === 'localhost' || host === '127.0.0.1') {
    return 'localhost';
  }
  return host;
};

const trimTrailingSlash = (value: string): string => value.replace(/\/+$/, '');

export const API_BASE_URL = trimTrailingSlash(`${window.location.protocol}//${resolveApiHost()}:8080`);

/**
 * Arma una URL de API tolerando bases configuradas con o sin sufijo `/api`.
 */
export const buildApiUrl = (path: string): string => {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const baseHasApiSuffix = /\/api$/i.test(API_BASE_URL);
  const pathHasApiPrefix = /^\/api(\/|$)/i.test(normalizedPath);

  if (baseHasApiSuffix && pathHasApiPrefix) {
    return `${API_BASE_URL.replace(/\/api$/i, '')}${normalizedPath}`;
  }

  return `${API_BASE_URL}${normalizedPath}`;
};
