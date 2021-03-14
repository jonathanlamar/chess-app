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
    `${url}/rules/update/${encodeURIComponent(
      fenString
    )}/${movingPieceFileRank}/${destinationFileRank}`
  );
