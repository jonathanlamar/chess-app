import { Pieces, Player, GlobalParams } from "../constants";

// TODO: Move these to a conversions class
export function rcToLinearIndex(r, c) {
  return r * 8 + c;
}

export function linearIndexToRc(i) {
  return {
    r: Math.floor(i / 8),
    c: i % 8,
  };
}

export function fileRankToRc(fileRank) {
  return {
    r: "abcdefgh".indexOf(fileRank[0]),
    c: 8 - parseInt(fileRank[1]),
  };
}

export function rcToFileRank(r, c) {
  return "abcdefgh"[r] + (8 - c).toString();
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
    throw "Error occured with getPieceImage.";
  }
}

// TODO: Move to Pieces
export function getPieceColor(piece) {
  if (piece === Pieces.NONE) {
    return Pieces.NONE; // TODO: Better way to handle NONE type
  } else if (piece < 16) {
    return Pieces.WHITE;
  } else {
    return Pieces.BLACK;
  }
}

export function getPieceType(piece) {
  return piece % 8;
}

export function initialiseChessBoard() {
  return parseFenString(
    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
  );
  // return parseFenString("N2qR3/bp6/8/8/8/8/8/2P4k1 b - - 0 1"); // For testing
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
    castleStatus: parseFenCastleStatus(castleStatusStr),
    enPassantTargetPos:
      enPassantTarget === "-" ? null : fileRankToRc(enPassantTarget),
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
    rowSquares[c] = parseFenPiece(processedRowString[c]);
  }

  return rowSquares;
}

function parseFenPiece(fenPiece) {
  switch (fenPiece) {
    case "p":
      return Pieces.BLACK | Pieces.PAWN;
    case "r":
      return Pieces.BLACK | Pieces.ROOK;
    case "n":
      return Pieces.BLACK | Pieces.KNIGHT;
    case "b":
      return Pieces.BLACK | Pieces.BISHOP;
    case "q":
      return Pieces.BLACK | Pieces.QUEEN;
    case "k":
      return Pieces.BLACK | Pieces.KING;
    case "P":
      return Pieces.WHITE | Pieces.PAWN;
    case "R":
      return Pieces.WHITE | Pieces.ROOK;
    case "N":
      return Pieces.WHITE | Pieces.KNIGHT;
    case "B":
      return Pieces.WHITE | Pieces.BISHOP;
    case "Q":
      return Pieces.WHITE | Pieces.QUEEN;
    case "K":
      return Pieces.WHITE | Pieces.KING;
    case "1":
      return Pieces.NONE;
    default:
      throw "Incorrect FEN VALUE";
  }
}

function parseFenCastleStatus(castleStatusStr) {
  return {
    blackQueen: castleStatusStr.indexOf("q") !== -1,
    blackKing: castleStatusStr.indexOf("k") !== -1,
    whiteQueen: castleStatusStr.indexOf("Q") !== -1,
    whiteKing: castleStatusStr.indexOf("K") !== -1,
  };
}

export function toFenString(gameState) {
  const fenBoard = toFenBoard(gameState.squares);
  const whoseMove = toFenWhoseMove(gameState.whoseMove);
  const castleStatus = toFenCastleStatus(gameState.castleStatus);
  const enPassantTarget = toFenEnPassantTarget(gameState.enPassantTargetPos);
  const halfMoveClock = gameState.halfMoveClockVal.toString();
  const fullMoveCount = gameState.fullMoveCountVal.toString();

  return [
    fenBoard,
    whoseMove,
    castleStatus,
    enPassantTarget,
    halfMoveClock,
    fullMoveCount,
  ].join(" ");
}

function toFenBoard(squares) {
  return squares.map(toProcessedFenRow).map(compressProcessedFenRow).join("/");
}

function toProcessedFenRow(row) {
  return row.map(toFenPiece).join("");
}

function toFenPiece(piece) {
  switch (piece) {
    case Pieces.BLACK | Pieces.PAWN:
      return "p";
    case Pieces.BLACK | Pieces.ROOK:
      return "r";
    case Pieces.BLACK | Pieces.KNIGHT:
      return "n";
    case Pieces.BLACK | Pieces.BISHOP:
      return "b";
    case Pieces.BLACK | Pieces.QUEEN:
      return "q";
    case Pieces.BLACK | Pieces.KING:
      return "k";
    case Pieces.WHITE | Pieces.PAWN:
      return "P";
    case Pieces.WHITE | Pieces.ROOK:
      return "R";
    case Pieces.WHITE | Pieces.KNIGHT:
      return "N";
    case Pieces.WHITE | Pieces.BISHOP:
      return "B";
    case Pieces.WHITE | Pieces.QUEEN:
      return "Q";
    case Pieces.WHITE | Pieces.KING:
      return "K";
    case Pieces.NONE:
      return "1";
  }
}

function compressProcessedFenRow(processedFenRow) {
  var newString = "";
  var i = 0;
  while (i < processedFenRow.length) {
    if (processedFenRow[i] === "1") {
      var j = i;
      while (j < processedFenRow.length && processedFenRow[j] === "1") {
        j++;
      }
      newString += (j - i).toString();
      i = j;
    } else {
      newString += processedFenRow[i];
      i++;
    }
  }

  return newString;
}

function toFenWhoseMove(whoseMove) {
  return whoseMove === Player.WHITE ? "w" : "b";
}

function toFenCastleStatus(castleStatus) {
  var status = "";

  if (castleStatus.whiteKing) status += "K";
  if (castleStatus.whiteQueen) status += "Q";
  if (castleStatus.blackKing) status += "k";
  if (castleStatus.blackQueen) status += "q";
  if (status === "") status = "-";

  return status;
}

function toFenEnPassantTarget(enPassantTargetPos) {
  if (enPassantTargetPos === null) return "-";
  else return rcToFileRank(enPassantTargetPos.r, enPassantTargetPos.c);
}
