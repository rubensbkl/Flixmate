import { SESSION_DURATION } from "./session";

export const movieCache = {
  data: {},
  timestamp: {},

  isValid(userId) {
    return this.data[userId] &&
      Date.now() - this.timestamp[userId] < SESSION_DURATION;
  },

  store(userId, movies) {
    this.data[userId] = [...movies];
    this.timestamp[userId] = Date.now();
  },

  get(userId) {
    return this.isValid(userId) ? [...this.data[userId]] : null;
  },

  clear(userId) {
    delete this.data[userId];
    delete this.timestamp[userId];
  }
};
