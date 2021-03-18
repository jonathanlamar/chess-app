import axios from "axios";

const url = "http://localhost:9000";

export const getLegalMoves = (fenString, fileRank, isInCheck) =>
  axios.get(
    `${url}/rules/legal/${encodeURIComponent(
      fenString
    )}/${fileRank}/${isInCheck}`
  );
export const getUpdatedBoard = (
  fenString,
  movingPieceFileRank,
  destinationFileRank
) =>
  axios.get(
    `${url}/rules/update/${encodeURIComponent(
      fenString
    )}/${movingPieceFileRank}/${destinationFileRank}`
  );
export const getCheckCondition = (fenString) =>
  axios.get(`${url}/rules/check/${encodeURIComponent(fenString)}`);
