export const SESSION_DURATION = 30 * 60 * 1000; // 30 min

export const loadSession = (userId) => {
  const saved = localStorage.getItem(`flixmate_user_${userId}`);
  if (!saved) return null;

  const session = JSON.parse(saved);
  const valid = Date.now() - session.timestamp < SESSION_DURATION;
  if (!valid) {
    clearSession(userId);
    return null;
  }
  return session;
};

export const saveSession = (userId, data) => {
  localStorage.setItem(`flixmate_user_${userId}`, JSON.stringify({
    ...data,
    timestamp: Date.now()
  }));
};

export const clearSession = (userId) => {
  localStorage.removeItem(`flixmate_user_${userId}`);
};
