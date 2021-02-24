import { positionToFileRank } from ".";
import { Pieces } from "../constants";

// Winging it for now.  I will probably totally refactor this.
export default class ValidMoves {
  // TODO: Logic for en passant and initial move
  static JUMP_DELTAS = {
    pawn: [
      [-1, 0],
      [-2, 0],
    ],
    knight: [
      [-2, -1],
      [-2, 1],
      [-1, -2],
      [-1, 2],
      [1, -2],
      [1, 2],
      [2, -1],
      [2, 1],
    ],
    king: [
      [-1, -1],
      [-1, 0],
      [-1, 1],
      [0, -1],
      [0, 1],
      [1, -1],
      [1, 0],
      [1, 1],
    ],
  };

  static isJumpPiece(piece) {
    return (
      piece === Pieces.PAWN || piece === Pieces.KNIGHT || piece === Pieces.KING
    );
  }

  // ALL possible moves, regardless of blocking pieces
  static allPosibleJumpMoves(piece, r, c) {
    var deltas, moves;

    if (piece === Pieces.PAWN) {
      deltas = ValidMoves.JUMP_DELTAS.pawn;
    } else if (piece === Pieces.KNIGHT) {
      deltas = ValidMoves.JUMP_DELTAS.knight;
    } else if (piece === Pieces.KING) {
      deltas = ValidMoves.JUMP_DELTAS.KING;
    } else {
      throw "Piece is not a jumping piece";
    }

    for (let i = 0; i < deltas.length; i++) {
      const moveR = deltas[i][0] + r;
      const moveC = deltas[i][1] + c;

      if (moveR >= 0 && moveR < 8 && moveC >= 0 && moveC < 8) {
        moves.push([moveR, moveC]);
      }
    }

    return moves;
  }

  static allPossibleMoves(piece, r, c) {
    var moves = [];

    for (let i = 0; i < 8; i++) {
      for (let j = 0; j < 8; j++) {
        if (i !== r || j !== c) {
          moves.push(positionToFileRank(i, j));
        }
      }
    }

    return moves;
  }
}
