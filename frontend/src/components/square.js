import Draggable from "react-draggable";

import "../index.css";
import LoggyComponent from "../utils/loggyComponent";
import { getPieceImage } from "../utils";
import { Piece, GlobalParams } from "../constants";

export default class Square extends LoggyComponent {
  constructor(props) {
    super(props);
    const iconUrl = getPieceImage(props.squareVal);
    this.style = {
      backgroundImage: "url('" + iconUrl + "')",
      height: GlobalParams.TILE_SIZE,
      width: GlobalParams.TILE_SIZE,
    };
    this.state = {
      boardPosition: props.startPos,
      dragDeltaPosition: { x: 0, y: 0 },
      prevDragDeltaPosition: { x: 0, y: 0 },
    };
  }

  handleStart = () => {
    this.setState({ prevDragDeltaPosition: this.state.dragDeltaPosition });
  };

  handleDrag = (e, ui) => {
    const {
      boardPosition,
      dragDeltaPosition,
      prevDragDeltaPosition,
    } = this.state;

    this.setState({
      dragDeltaPosition: {
        x: dragDeltaPosition.x + ui.deltaX,
        y: dragDeltaPosition.y + ui.deltaY,
      },
    });

    const newR = boardPosition.r + dragDeltaPosition.y / GlobalParams.TILE_SIZE;
    const newC = boardPosition.c + dragDeltaPosition.x / GlobalParams.TILE_SIZE;

    console.log(this);
    console.log("newR = ", newR, "newC = ", newC);
  };

  handleStop = () => {
    const {
      boardPosition,
      dragDeltaPosition,
      prevDragDeltaPosition,
    } = this.state;

    const newR = boardPosition.r + dragDeltaPosition.y / GlobalParams.TILE_SIZE;
    const newC = boardPosition.c + dragDeltaPosition.x / GlobalParams.TILE_SIZE;

    // Pass state back to App for update and logic
    const { r: finalR, c: finalC } = this.props.handleStopFn(
      boardPosition.r,
      boardPosition.c,
      newR,
      newC
    );
    this.setState({ boardPosition: { r: finalR, c: finalC } });

    console.log("Handled stop:", this);
  };

  render() {
    if (this.props.squareVal === Piece.NONE) {
      return (
        <div
          className={"square " + this.props.shade}
          style={this.style}
          key={this.props.keyVal}
        />
      );
    } else {
      return (
        <div className={"square " + this.props.shade}>
          <Draggable
            // TODO: Set bounds
            grid={[GlobalParams.TILE_SIZE, GlobalParams.TILE_SIZE]}
            position={this.state.dragDeltaPosition}
            onStart={this.handleStart}
            onDrag={this.handleDrag}
            onStop={this.handleStop}
          >
            <div
              className={"square transparent-square"}
              style={this.style}
              key={this.props.keyVal}
            />
          </Draggable>
        </div>
      );
    }
  }
}
