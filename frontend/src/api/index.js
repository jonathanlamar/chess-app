import axios from "axios";

const url = "http://localhost:9000";

export const getLegalMoves = (fenString, fileRank) =>
  axios.get(`${url}/rules/legal/${encodeURIComponent(fenString)}/${fileRank}`);
export const getAttackingSquares = (fenString) =>
  axios.get(`${url}/rules/attack/${encodeURIComponent(fenString)}`);
export const getCheckData = (fenString) =>
  axios.get(`${url}/rules/check/${encodeURIComponent(fenString)}`);
