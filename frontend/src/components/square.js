import React from "react";
import Draggable from "react-draggable";

import "../index.css";
import { getPieceImage, rcToXy, xyToRc } from "../utils";
import { GlobalParams } from "../constants";

export default class Square extends React.Component {
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
      dragDeltaPosition: rcToXy(props.startPos.r, props.startPos.c),
    };
  }

  handleDrag = (e, ui) => {
    this.setState({
      dragDeltaPosition: {
        x: this.state.dragDeltaPosition.x + ui.deltaX,
        y: this.state.dragDeltaPosition.y + ui.deltaY,
      },
    });
  };

  handleStop = () => {
    const { boardPosition, dragDeltaPosition } = this.state;

    const { r: newR, c: newC } = xyToRc(
      dragDeltaPosition.x,
      dragDeltaPosition.y
    );

    // Pass state back to App for update and logic
    const { r: finalR, c: finalC } = this.props.handleStopFn(
      boardPosition.r,
      boardPosition.c,
      newR,
      newC
    );

    this.setState({
      boardPosition: { r: finalR, c: finalC },
      dragDeltaPosition: {
        x: finalC * GlobalParams.TILE_SIZE,
        y: finalR * GlobalParams.TILE_SIZE,
      },
    });
  };

  render() {
    return (
      <Draggable
        position={this.state.dragDeltaPosition}
        bounds="parent"
        onDrag={this.handleDrag}
        onStop={this.handleStop}
      >
        <div
          className={"square transparent-square"}
          style={this.style}
          key={this.props.keyVal}
        >
          <div>
            {this.state.dragDeltaPosition.x},{this.state.dragDeltaPosition.y}
          </div>
          <div>
            {this.state.boardPosition.r},{this.state.boardPosition.c}
          </div>
        </div>
      </Draggable>
    );
  }
}
