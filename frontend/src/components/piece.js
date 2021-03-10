import React from "react";
import Draggable from "react-draggable";

import "../index.css";
import { rcToXy, xyToRc } from "../utils";
import { GlobalParams } from "../constants";

export default class Piece extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      boardPosition: props.startPos,
      dragDeltaPosition: rcToXy(props.startPos.r, props.startPos.c),
    };
    this.style = {
      height: GlobalParams.TILE_SIZE,
      width: GlobalParams.TILE_SIZE,
      position: "absolute",
      zIndex: props.keyVal, // Give each piece a unique z index
    };
  }

  handleStart = () => {
    this.props.handleStartFn(
      this.state.boardPosition.r,
      this.state.boardPosition.c
    );
  };

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
        onStart={this.handleStart}
        onDrag={this.handleDrag}
        onStop={this.handleStop}
        disabled={!this.props.clickable}
      >
        <div className={"square transparent-square"} key={this.props.keyVal}>
          <img src={this.props.iconUrl} style={this.style} draggable="false" />
        </div>
      </Draggable>
    );
  }
}
