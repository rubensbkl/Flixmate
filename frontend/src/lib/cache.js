import { SESSION_DURATION } from "./session";

export const movieCache = {
  data: {},
  timestamp: {},

  isValid(key) {
    return this.data[key] &&
      Date.now() - this.timestamp[key] < SESSION_DURATION;
  },

  store(key, movies) {
    this.data[key] = [...movies];
    this.timestamp[key] = Date.now();
  },

  get(key) {
    return this.isValid(key) ? [...this.data[key]] : null;
  },

  clear(key) {
    delete this.data[key];
    delete this.timestamp[key];
  }
};