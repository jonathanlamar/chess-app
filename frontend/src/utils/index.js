import { Piece } from "../constants";

export function getLinearIndex(r, c) {
  return r * 8 + c;
}

export function getPieceImage(squareVal) {
  if (squareVal === (Piece.WHITE | Piece.PAWN)) {
    return "https://upload.wikimedia.org/wikipedia/commons/4/45/Chess_plt45.svg";
  } else if (squareVal === (Piece.WHITE | Piece.KNIGHT)) {
    return "https://upload.wikimedia.org/wikipedia/commons/7/70/Chess_nlt45.svg";
  } else if (squareVal === (Piece.WHITE | Piece.ROOK)) {
    return "https://upload.wikimedia.org/wikipedia/commons/7/72/Chess_rlt45.svg";
  } else if (squareVal === (Piece.WHITE | Piece.BISHOP)) {
    return "https://upload.wikimedia.org/wikipedia/commons/b/b1/Chess_blt45.svg";
  } else if (squareVal === (Piece.WHITE | Piece.QUEEN)) {
    return "https://upload.wikimedia.org/wikipedia/commons/1/15/Chess_qlt45.svg";
  } else if (squareVal === (Piece.WHITE | Piece.KING)) {
    return "https://upload.wikimedia.org/wikipedia/commons/4/42/Chess_klt45.svg";
  } else if (squareVal === (Piece.BLACK | Piece.PAWN)) {
    return "https://upload.wikimedia.org/wikipedia/commons/c/c7/Chess_pdt45.svg";
  } else if (squareVal === (Piece.BLACK | Piece.KNIGHT)) {
    return "https://upload.wikimedia.org/wikipedia/commons/e/ef/Chess_ndt45.svg";
  } else if (squareVal === (Piece.BLACK | Piece.ROOK)) {
    return "https://upload.wikimedia.org/wikipedia/commons/f/ff/Chess_rdt45.svg";
  } else if (squareVal === (Piece.BLACK | Piece.BISHOP)) {
    return "https://upload.wikimedia.org/wikipedia/commons/9/98/Chess_bdt45.svg";
  } else if (squareVal === (Piece.BLACK | Piece.QUEEN)) {
    return "https://upload.wikimedia.org/wikipedia/commons/4/47/Chess_qdt45.svg";
  } else if (squareVal === (Piece.BLACK | Piece.KING)) {
    return "https://upload.wikimedia.org/wikipedia/commons/f/f0/Chess_kdt45.svg";
  } else if (squareVal === Piece.NONE) {
    return null;
  } else {
    console.log("Error occured with getPieceImage.");
    console.log("squareVal = ", squareVal);
  }
}

export function getSquareShade(r, c) {
  return r % 2 === c % 2 ? "light-square" : "dark-square";
}

export function initialiseChessBoard() {
  // TODO: Compact representations will be needed for loading and saving games.
  // return decodeFromWhatsIt();

  const squares = Array(8);
  for (let r = 0; r < 8; r++) {
    squares[r] = new Array(8).fill(Piece.NONE);
  }

  squares[0][0] = Piece.BLACK | Piece.ROOK;
  squares[0][1] = Piece.BLACK | Piece.KNIGHT;
  squares[0][2] = Piece.BLACK | Piece.BISHOP;
  squares[0][3] = Piece.BLACK | Piece.QUEEN;
  squares[0][4] = Piece.BLACK | Piece.KING;
  squares[0][5] = Piece.BLACK | Piece.BISHOP;
  squares[0][6] = Piece.BLACK | Piece.KNIGHT;
  squares[0][7] = Piece.BLACK | Piece.ROOK;
  squares[1][0] = Piece.BLACK | Piece.PAWN;
  squares[1][1] = Piece.BLACK | Piece.PAWN;
  squares[1][2] = Piece.BLACK | Piece.PAWN;
  squares[1][3] = Piece.BLACK | Piece.PAWN;
  squares[1][4] = Piece.BLACK | Piece.PAWN;
  squares[1][5] = Piece.BLACK | Piece.PAWN;
  squares[1][6] = Piece.BLACK | Piece.PAWN;
  squares[1][7] = Piece.BLACK | Piece.PAWN;

  squares[6][0] = Piece.WHITE | Piece.PAWN;
  squares[6][1] = Piece.WHITE | Piece.PAWN;
  squares[6][2] = Piece.WHITE | Piece.PAWN;
  squares[6][3] = Piece.WHITE | Piece.PAWN;
  squares[6][4] = Piece.WHITE | Piece.PAWN;
  squares[6][5] = Piece.WHITE | Piece.PAWN;
  squares[6][6] = Piece.WHITE | Piece.PAWN;
  squares[6][7] = Piece.WHITE | Piece.PAWN;
  squares[7][0] = Piece.WHITE | Piece.ROOK;
  squares[7][1] = Piece.WHITE | Piece.KNIGHT;
  squares[7][2] = Piece.WHITE | Piece.BISHOP;
  squares[7][3] = Piece.WHITE | Piece.QUEEN;
  squares[7][4] = Piece.WHITE | Piece.KING;
  squares[7][5] = Piece.WHITE | Piece.BISHOP;
  squares[7][6] = Piece.WHITE | Piece.KNIGHT;
  squares[7][7] = Piece.WHITE | Piece.ROOK;

  return squares;
}
