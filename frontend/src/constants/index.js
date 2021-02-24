export class Player {
  static WHITE = 8;
  static BLACK = 16;
}

export class Pieces {
  static NONE = 0;
  static KING = 1;
  static QUEEN = 2;
  static BISHOP = 3;
  static KNIGHT = 4;
  static ROOK = 5;
  static PAWN = 6;

  static WHITE = Player.WHITE;
  static BLACK = Player.BLACK;
}

export class GlobalParams {
  static TILE_SIZE = 75;
  static BOARD_SIZE = GlobalParams.TILE_SIZE * 8;
}
