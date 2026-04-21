const resolveApiHost = (): string => {
  const host = window.location.hostname;
  if (!host || host === 'localhost' || host === '127.0.0.1') {
    return 'localhost';
  }
  return host;
};

export const API_BASE_URL = `${window.location.protocol}//${resolveApiHost()}:8080`;
