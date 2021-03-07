import axios from "axios";

const url = "http://localhost:9000";

export const getLegalMoves = (fenString, rankFile) =>
  axios.get(`${url}/rules/legal/${fenString}/${rankFile}`);
export const getAttackingSquares = (fenString) =>
  axios.get(`${url}/rules/attack/${fenString}`);
export const getCheckData = (fenString) =>
  axios.get(`${url}/rules/check/${fenString}`);
export const getTestData = () => axios.get(`${url}/rules/hello`);
