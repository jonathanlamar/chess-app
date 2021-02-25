import { getPieceColor, getPieceType, rcToFileRank } from ".";
import { Pieces } from "../constants";

// Winging it for now.  I will probably totally refactor this.
export default class ValidMoves {
  static JUMP_DELTAS = {
    // TODO: En passant rule
    pawn: [[-1, 0]],
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
    const pieceType = getPieceType(piece);

    return (
      pieceType === Pieces.PAWN ||
      pieceType === Pieces.KNIGHT ||
      pieceType === Pieces.KING
    );
  }

  // ALL possible moves, regardless of blocking pieces
  static allPosibleJumpMoves(piece, r, c, squares) {
    const pieceType = getPieceType(piece);
    var deltas;

    // TODO:
    // En Passant
    // Check
    // Capturing (weird for pawns)
    if (pieceType === Pieces.PAWN) {
      if (this.pawnIsInitial(piece, r)) {
        deltas = ValidMoves.JUMP_DELTAS.pawn.concat([[-2, 0]]);
      } else {
        deltas = ValidMoves.JUMP_DELTAS.pawn;
      }

      if (getPieceColor(piece) === Pieces.BLACK) {
        deltas = deltas.map((pair) => [-pair[0], pair[1]]);
      }
    } else if (pieceType === Pieces.KNIGHT) {
      deltas = ValidMoves.JUMP_DELTAS.knight;
    } else if (pieceType === Pieces.KING) {
      deltas = ValidMoves.JUMP_DELTAS.king;
    } else {
      throw "Piece is not a jumping piece";
    }

    // Handle out of bounds jumps
    return deltas
      .map((pair) => [pair[0] + r, pair[1] + c])
      .filter(
        (pair) => pair[0] >= 0 && pair[0] < 8 && pair[1] >= 0 && pair[1] < 8
      )
      .map((pair) => rcToFileRank(pair[0], pair[1]));
  }

  static pawnIsInitial(piece, r) {
    const pieceType = getPieceType(piece);
    const pieceColor = getPieceColor(piece);

    if (pieceType !== Pieces.PAWN) throw "Piece is not pawn";

    return (
      (pieceColor === Pieces.BLACK && r === 1) ||
      (pieceColor === Pieces.WHITE && r === 6)
    );
  }

  static isSlidePiece(piece) {
    const pieceType = getPieceType(piece);

    return (
      pieceType === Pieces.ROOK ||
      pieceType === Pieces.BISHOP ||
      pieceType === Pieces.QUEEN
    );
  }

  static allPossibleSlideMoves(piece, r, c, squares) {
    const pieceType = getPieceType(piece);
    var rays;

    if (pieceType === Pieces.ROOK || pieceType === Pieces.QUEEN) {
      rays = rays.concat(ValidMoves.getRay(r, c, -1, 0));
      rays = rays.concat(ValidMoves.getRay(r, c, 1, 0));
      rays = rays.concat(ValidMoves.getRay(r, c, 0, -1));
      rays = rays.concat(ValidMoves.getRay(r, c, 0, 1));
    } else if (pieceType === Pieces.BISHOP || pieceType === Pieces.QUEEN) {
      rays = rays.concat(ValidMoves.getRay(r, c, -1, -1));
      rays = rays.concat(ValidMoves.getRay(r, c, -1, 1));
      rays = rays.concat(ValidMoves.getRay(r, c, 1, -1));
      rays = rays.concat(ValidMoves.getRay(r, c, 1, 1));
    }

    return rays;
  }

  static getRay(r, c, deltaR, deltaC) {
    var squares;
    var isBlocked = false;

    for (let i = 0; i < 8; i++) {
      isBlocked =
        isBlocked && squares[r + i * deltaR][c + i * deltaC] !== Pieces.NONE;
      if (!isBlocked) squares.push([r + i * deltaR, c + i * deltaC]);
    }

    return squares;
  }

  // TODO: Remove once unnecessary
  static allPossibleMoves(piece, r, c) {
    var moves = [];

    for (let i = 0; i < 8; i++) {
      for (let j = 0; j < 8; j++) {
        if (i !== r || j !== c) {
          moves.push(rcToFileRank(i, j));
        }
      }
    }

    return moves;
  }
}
