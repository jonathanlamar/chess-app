import axios from "axios";

const url = "http://localhost:8000/rules";

export const getLegalMoves = (fenString, r, c) =>
  axios.get(`${url}/legal/${{ fenString, r, c }}`);
export const getAttackingSquares = (fenString) =>
  axios.get(`${url}/attack/${fenString}`);
export const getCheckData = (fenString) =>
  axios.get(`${url}/check/${fenString}`);
