import { Pieces, Player, GlobalParams } from "../constants";

export function getLinearIndex(r, c) {
  return r * 8 + c;
}

export function getPieceImage(squareVal) {
  if (squareVal === (Pieces.WHITE | Pieces.PAWN)) {
    return "https://upload.wikimedia.org/wikipedia/commons/4/45/Chess_plt45.svg";
  } else if (squareVal === (Pieces.WHITE | Pieces.KNIGHT)) {
    return "https://upload.wikimedia.org/wikipedia/commons/7/70/Chess_nlt45.svg";
  } else if (squareVal === (Pieces.WHITE | Pieces.ROOK)) {
    return "https://upload.wikimedia.org/wikipedia/commons/7/72/Chess_rlt45.svg";
  } else if (squareVal === (Pieces.WHITE | Pieces.BISHOP)) {
    return "https://upload.wikimedia.org/wikipedia/commons/b/b1/Chess_blt45.svg";
  } else if (squareVal === (Pieces.WHITE | Pieces.QUEEN)) {
    return "https://upload.wikimedia.org/wikipedia/commons/1/15/Chess_qlt45.svg";
  } else if (squareVal === (Pieces.WHITE | Pieces.KING)) {
    return "https://upload.wikimedia.org/wikipedia/commons/4/42/Chess_klt45.svg";
  } else if (squareVal === (Pieces.BLACK | Pieces.PAWN)) {
    return "https://upload.wikimedia.org/wikipedia/commons/c/c7/Chess_pdt45.svg";
  } else if (squareVal === (Pieces.BLACK | Pieces.KNIGHT)) {
    return "https://upload.wikimedia.org/wikipedia/commons/e/ef/Chess_ndt45.svg";
  } else if (squareVal === (Pieces.BLACK | Pieces.ROOK)) {
    return "https://upload.wikimedia.org/wikipedia/commons/f/ff/Chess_rdt45.svg";
  } else if (squareVal === (Pieces.BLACK | Pieces.BISHOP)) {
    return "https://upload.wikimedia.org/wikipedia/commons/9/98/Chess_bdt45.svg";
  } else if (squareVal === (Pieces.BLACK | Pieces.QUEEN)) {
    return "https://upload.wikimedia.org/wikipedia/commons/4/47/Chess_qdt45.svg";
  } else if (squareVal === (Pieces.BLACK | Pieces.KING)) {
    return "https://upload.wikimedia.org/wikipedia/commons/f/f0/Chess_kdt45.svg";
  } else if (squareVal === Pieces.NONE) {
    return null;
  } else {
    console.log("Error occured with getPieceImage.");
    console.log("squareVal = ", squareVal);
  }
}

export function rcToXy(r, c) {
  return {
    x: c * GlobalParams.TILE_SIZE,
    y: r * GlobalParams.TILE_SIZE,
  };
}

export function xyToRc(x, y) {
  return {
    r: Math.round(y / GlobalParams.TILE_SIZE),
    c: Math.round(x / GlobalParams.TILE_SIZE),
  };
}

export function getPieceColor(piece) {
  if (piece === Pieces.NONE) {
    return Pieces.NONE; // TODO: Better way to handle NONE type
  } else if (piece < 16) {
    return Pieces.WHITE;
  } else {
    return Pieces.BLACK;
  }
}

export function initialiseChessBoard() {
  // return parseFenBoardRep("P7/n7/8/8/8/8/8/8"); // For testing
  return parseFenBoardRep("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
}
export function parseFenString(fenString) {
  // https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
  //
  // Each rank is described, starting with rank 8 and ending with rank 1;
  // within each rank, the contents of each square are described from file "a"
  // through file "h". Following the Standard Algebraic Notation (SAN), each
  // piece is identified by a single letter taken from the standard English
  // names (pawn = "P", knight = "N", bishop = "B", rook = "R", queen = "Q" and
  // king = "K"). White pieces are designated using upper-case letters
  // ("PNBRQK") while black pieces use lowercase ("pnbrqk"). Empty squares are
  // noted using digits 1 through 8 (the number of empty squares), and "/"
  // separates ranks.
  //
  // Here's the FEN for the starting position:
  //
  // Here's the FEN for the starting position:
  //
  // rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
  // And after the move 1.e4:
  //
  // rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1
  // And then after 1...c5:
  //
  // rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2
  // And then after 2.Nf3:
  //
  // rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2

  const [
    boardRep,
    toMove,
    castleStatusStr,
    enPassantTarget,
    halfMoveClock,
    fullMoveCount,
  ] = fenString.split(" ");

  return {
    squares: parseFenBoardRep(boardRep),
    whoseMove: toMove === "w" ? Player.WHITE : Player.BLACK,
    castleStatus: getCastleStatus(castleStatusStr),
    enPassantTargetPos:
      enPassantTarget === "-" ? null : fileRankToPos(enPassantTarget),
    halfMoveClockVal: parseInt(halfMoveClock),
    fullMoveCountVal: parseInt(fullMoveCount),
  };
}

function parseFenBoardRep(boardRep) {
  return boardRep.split("/").map(processRowString).map(parseProcessedRowString);
}

function processRowString(rowString) {
  // Replace numbers with 1 repeated that number of times, e.g.,
  // the number 4 is replaces with 1111

  var newString = "";
  for (let i = 0; i < rowString.length; i++) {
    const c = rowString[i];
    const maybeNum = parseInt(c);

    if (!isNaN(maybeNum)) {
      newString = newString + "1".repeat(maybeNum);
    } else {
      newString = newString + c;
    }
  }

  return newString;
}

function parseProcessedRowString(processedRowString) {
  // Processes just the board part of the FEN string

  const rowSquares = Array(8).fill(Pieces.NONE);

  for (let c = 0; c < 8; c++) {
    switch (processedRowString[c]) {
      case "p":
        rowSquares[c] = Pieces.BLACK | Pieces.PAWN;
        break;
      case "r":
        rowSquares[c] = Pieces.BLACK | Pieces.ROOK;
        break;
      case "n":
        rowSquares[c] = Pieces.BLACK | Pieces.KNIGHT;
        break;
      case "b":
        rowSquares[c] = Pieces.BLACK | Pieces.BISHOP;
        break;
      case "q":
        rowSquares[c] = Pieces.BLACK | Pieces.QUEEN;
        break;
      case "k":
        rowSquares[c] = Pieces.BLACK | Pieces.KING;
        break;
      case "P":
        rowSquares[c] = Pieces.WHITE | Pieces.PAWN;
        break;
      case "R":
        rowSquares[c] = Pieces.WHITE | Pieces.ROOK;
        break;
      case "N":
        rowSquares[c] = Pieces.WHITE | Pieces.KNIGHT;
        break;
      case "B":
        rowSquares[c] = Pieces.WHITE | Pieces.BISHOP;
        break;
      case "Q":
        rowSquares[c] = Pieces.WHITE | Pieces.QUEEN;
        break;
      case "K":
        rowSquares[c] = Pieces.WHITE | Pieces.KING;
        break;
      case "1":
        rowSquares[c] = Pieces.NONE;
        break;
      default:
        throw "Incorrect FEN VALUE";
    }
  }

  return rowSquares;
}

function getCastleStatus(castleStatusStr) {
  return {
    whiteQueen: castleStatusStr.indexOf("Q") !== -1,
    whiteKing: castleStatusStr.indexOf("K") !== -1,
    blackQueen: castleStatusStr.indexOf("q") !== -1,
    blackKing: castleStatusStr.indexOf("k") !== -1,
  };
}

function fileRankToPos(fileRank) {
  return {
    r: "abcdefgh".indexOf(fileRank[0]),
    c: 8 - parseInt(fileRank[1]),
  };
}
