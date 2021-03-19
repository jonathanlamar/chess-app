import axios from "axios";

const url = "http://localhost:9000";

export const getLegalMoves = (fenString, fileRank) =>
  axios.get(`${url}/rules/legal/${encodeURIComponent(fenString)}/${fileRank}`);
export const getUpdatedBoard = (
  fenString,
  movingPieceFileRank,
  destinationFileRank
) =>
  axios.get(
    `${url}/actions/update/${encodeURIComponent(
      fenString
    )}/${movingPieceFileRank}/${destinationFileRank}`
  );
export const getCheckCondition = (fenString) =>
  axios.get(`${url}/rules/check/${encodeURIComponent(fenString)}`);
export const getRandomAiMove = (fenString) =>
  axios.get(`${url}/ai/random/${encodeURIComponent(fenString)}`);
