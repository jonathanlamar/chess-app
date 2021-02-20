import Piece from "../pieces";

export function getLinearIndex(r, c) {
  return r * 8 + c;
}

export function getPieceImage(squareVal) {
  switch (squareVal) {
    case Piece.White | Piece.Pawn:
      return "https://upload.wikimedia.org/wikipedia/commons/4/45/Chess_plt45.svg";
    case Piece.White | Piece.Knight:
      return "https://upload.wikimedia.org/wikipedia/commons/7/70/Chess_nlt45.svg";
    case Piece.White | Piece.Rook:
      return "https://upload.wikimedia.org/wikipedia/commons/7/72/Chess_rlt45.svg";
    case Piece.White | Piece.Bishop:
      return "https://upload.wikimedia.org/wikipedia/commons/b/b1/Chess_blt45.svg";
    case Piece.White | Piece.Queen:
      return "https://upload.wikimedia.org/wikipedia/commons/1/15/Chess_qlt45.svg";
    case Piece.White | Piece.King:
      return "https://upload.wikimedia.org/wikipedia/commons/4/42/Chess_klt45.svg";
    case Piece.Black | Piece.Pawn:
      return "https://upload.wikimedia.org/wikipedia/commons/c/c7/Chess_pdt45.svg";
    case Piece.Black | Piece.Knight:
      return "https://upload.wikimedia.org/wikipedia/commons/e/ef/Chess_ndt45.svg";
    case Piece.Black | Piece.Rook:
      return "https://upload.wikimedia.org/wikipedia/commons/f/ff/Chess_rdt45.svg";
    case Piece.Black | Piece.Bishop:
      return "https://upload.wikimedia.org/wikipedia/commons/9/98/Chess_bdt45.svg";
    case Piece.Black | Piece.Queen:
      return "https://upload.wikimedia.org/wikipedia/commons/4/47/Chess_qdt45.svg";
    case Piece.Black | Piece.King:
      return "https://upload.wikimedia.org/wikipedia/commons/f/f0/Chess_kdt45.svg";
    default:
      return null;
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
    squares[r] = new Array(8).fill(Piece.None);
  }

  squares[0][0] = Piece.Back | Piece.Rook;
  squares[0][1] = Piece.Back | Piece.Knight;
  squares[0][2] = Piece.Back | Piece.Bishop;
  squares[0][3] = Piece.Back | Piece.Queen;
  squares[0][4] = Piece.Back | Piece.King;
  squares[0][5] = Piece.Back | Piece.Bishop;
  squares[0][6] = Piece.Back | Piece.Knight;
  squares[0][7] = Piece.Back | Piece.Rook;
  squares[1][0] = Piece.Back | Piece.Pawn;
  squares[1][1] = Piece.Back | Piece.Pawn;
  squares[1][2] = Piece.Back | Piece.Pawn;
  squares[1][3] = Piece.Back | Piece.Pawn;
  squares[1][4] = Piece.Back | Piece.Pawn;
  squares[1][5] = Piece.Back | Piece.Pawn;
  squares[1][6] = Piece.Back | Piece.Pawn;
  squares[1][7] = Piece.Back | Piece.Pawn;

  squares[6][0] = Piece.White | Piece.Rook;
  squares[6][1] = Piece.White | Piece.Knight;
  squares[6][2] = Piece.White | Piece.Bishop;
  squares[6][3] = Piece.White | Piece.Queen;
  squares[6][4] = Piece.White | Piece.King;
  squares[6][5] = Piece.White | Piece.Bishop;
  squares[6][6] = Piece.White | Piece.Knight;
  squares[6][7] = Piece.White | Piece.Rook;
  squares[7][0] = Piece.White | Piece.Pawn;
  squares[7][1] = Piece.White | Piece.Pawn;
  squares[7][2] = Piece.White | Piece.Pawn;
  squares[7][3] = Piece.White | Piece.Pawn;
  squares[7][4] = Piece.White | Piece.Pawn;
  squares[7][5] = Piece.White | Piece.Pawn;
  squares[7][6] = Piece.White | Piece.Pawn;
  squares[7][7] = Piece.White | Piece.Pawn;

  return squares;
}
