export const SESSION_DURATION = 30 * 60 * 1000; // 30 min

export const loadSession = () => {
  const token = localStorage.getItem('token');
  if (!token) return null;
  
  const sessionData = localStorage.getItem('session_data');
  if (!sessionData) return null;

  const session = JSON.parse(sessionData);
  const valid = Date.now() - session.timestamp < SESSION_DURATION;
  if (!valid) {
    clearSession();
    return null;
  }
  return session;
};

export const saveSession = (data) => {
  localStorage.setItem('session_data', JSON.stringify({
    ...data,
    timestamp: Date.now()
  }));
};

export const clearSession = () => {
  localStorage.removeItem('session_data');
};