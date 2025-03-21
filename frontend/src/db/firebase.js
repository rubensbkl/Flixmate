import { initializeApp } from "firebase/app";
import { getDatabase } from "firebase/database";

const firebaseConfig = {
  apiKey: "AIzaSyALTsfva94eBqvCfGvkplvhAklskInwkcY",
  authDomain: "cinematch-8670b.firebaseapp.com",
  databaseURL: "https://cinematch-8670b-default-rtdb.firebaseio.com",
  projectId: "cinematch-8670b",
  storageBucket: "cinematch-8670b.firebasestorage.app",
  messagingSenderId: "431272927286",
  appId: "1:431272927286:web:cccccd6e2427386c897897",
  measurementId: "G-YNE319HXSN"
};

const app = initializeApp(firebaseConfig);
const db = getDatabase(app);
export { db }; 