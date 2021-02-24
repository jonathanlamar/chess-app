import { Piece } from "../constants";

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
      piece === Piece.PAWN || piece === Piece.KNIGHT || piece === Piece.KING
    );
  }

  // ALL possible moves, regardless of blocking pieces
  static allPosibleJumpMoves(piece, r, c) {
    var deltas, moves;

    if (piece === Piece.PAWN) {
      deltas = JUMP_DELTAS.pawn;
    } else if (piece === Piece.KNIGHT) {
      deltas = JUMP_DELTAS.knight;
    } else if (piece === Piece.KING) {
      deltas = JUMP_DELTAS.KING;
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
}
