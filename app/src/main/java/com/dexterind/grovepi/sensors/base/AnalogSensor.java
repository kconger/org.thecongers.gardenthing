package com.dexterind.grovepi.sensors.base;

import com.dexterind.grovepi.utils.Commands;

import java.io.IOException;

public class AnalogSensor extends Sensor {
  protected int pin = 0;
  private int length = 4;

  public AnalogSensor(int pin, int length) throws IOException, InterruptedException {
	super();
	this.pin = pin;
	this.length = length == -1 ? this.length : length;
  }
  
  public byte[] readBytes() throws IOException {
	this.board.writeI2c(Commands.AREAD, this.pin, Commands.UNUSED, Commands.UNUSED);
	this.board.readI2c(1);
	return this.board.readI2c(this.length);
  }
  
  public boolean write(int value) throws IOException {
	this.board.writeI2c(Commands.AWRITE, this.pin, value, Commands.UNUSED);
	return true;
  }
}